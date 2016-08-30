package gq.console.commands

import gq.console.DataHolder
import gq.core.data.GQGseInfo
import gq.core.data.Species
import gq.core.gea.EnrichmentResultItem
import gq.core.gea.SpecifiedEntrezGenes
import gq.core.gea.findBonferroniSignificant
import gq.core.genes.GeneFormat
import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.Option
import org.apache.commons.cli.Options
import java.io.PrintWriter
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.Executors


fun geneConversionMapToCSV(geneConversionMap: Map<String, Long?>,
                           geneFormat: GeneFormat,
                           speciesFrom: Species,
                           speciesTo: Species,
                           pathToFile: Path,
                           separator: String = "\t") {
    val header = listOf(
            "${geneFormat.formatName}(${speciesFrom.original})",
            "${GeneFormat.ENTREZ.formatName}(${speciesTo.original})"
    ).joinToString(separator)
    val resultLines = mutableListOf(header)
    resultLines.addAll(geneConversionMap.map { it.key + separator + (it.value ?: "-") })
    Files.write(pathToFile, resultLines)
}

fun enrichmentResultItemsToCSV(enrichmentResultItems: List<EnrichmentResultItem>,
                               gseToTitle: Map<Int, String>,
                               pathToFile: Path,
                               separator: String = "\t") {
    val header = listOf(
            "GSE", "GPL", "moduleNumber", "logPvalue", "logAdjPvalue", "intersectionSize", "moduleSize", "title"
    ).joinToString(separator)
    val resultLines = mutableListOf(header)
    resultLines.addAll(enrichmentResultItems.map {
        listOf("GSE${it.gse}",
                "GPL${it.gpl}",
                it.moduleNumber,
                it.logPvalue,
                it.logAdjPvalue,
                it.intersectionSize,
                it.moduleSize,
                gseToTitle[it.gse]
        ).joinToString(separator)
    })
    Files.write(pathToFile, resultLines)
}


data class SearchResult(val identifiedGeneFormat: GeneFormat,
                        val geneConversionMap: Map<String, Long?>,
                        val gseToTitle: Map<Int, String>,
                        val enrichmentResultItems: List<EnrichmentResultItem>)

fun findEnrichedModules(rawGenes: List<String>,
                        speciesFrom: Species,
                        speciesTo: Species,
                        dataHolder: DataHolder): SearchResult {
    val geneFormat = GeneFormat.guess(rawGenes)
    val conversionMap = dataHolder.smartConverter.toEntrez(rawGenes, geneFormat, speciesFrom, speciesTo)
    val entrezIds = conversionMap.values.filterNotNull()
    val enrichmentItems = findBonferroniSignificant(
            dataHolder.moduleCollection,
            SpecifiedEntrezGenes(speciesTo, entrezIds))
    val gseToTitle = enrichmentItems.associate {
        Pair(it.gse, dataHolder.gseInfoCollection[it.gse]?.title ?: GQGseInfo.DEFAULT_TITLE)
    }
    return SearchResult(geneFormat, conversionMap, gseToTitle, enrichmentItems)
}

fun processQuery(pathToQuery: Path, destPath: Path, dataHolder: DataHolder): Pair<Boolean, Path> {
    try {
        val tokenizer = StringTokenizer(Files.newBufferedReader(pathToQuery).readText())

        val speciesFrom = try {
            Species.fromOriginal(tokenizer.nextToken())
        } catch (e: Exception) {
            throw Exception("Can't read source species", e)
        }

        val speciesTo = try {
            Species.fromOriginal(tokenizer.nextToken())
        } catch (e: Exception) {
            throw Exception("Can't read target species", e)
        }

        val rawGenes = mutableListOf<String>()
        while (tokenizer.hasMoreTokens()) { rawGenes.add(tokenizer.nextToken()) }

        val searchResult = findEnrichedModules(rawGenes, speciesFrom, speciesTo, dataHolder)

        val geneConversionFilePath = destPath.resolve("${pathToQuery.fileName}.conversion.csv")
        geneConversionMapToCSV(searchResult.geneConversionMap, searchResult.identifiedGeneFormat,
                speciesFrom, speciesTo, geneConversionFilePath)

        val enrichmentResultFilePath = destPath.resolve("${pathToQuery.fileName}.result.csv")
        enrichmentResultItemsToCSV(searchResult.enrichmentResultItems, searchResult.gseToTitle, enrichmentResultFilePath)

        return Pair(true, pathToQuery)
    } catch (e: Throwable) {
        val errFilePath = destPath.resolve("${pathToQuery.fileName}.err")
        val pw = PrintWriter(errFilePath.toString())
        e.printStackTrace(pw)
        pw.close()
        return Pair(false, pathToQuery)
    }
}

class BulkQueryCommand : Command {
    companion object {
        val DEFAULT_MAX_THREADS_COUNT = 4
    }
    override fun execute(cmdLine: CommandLine) {
        val dataPath = Paths.get(cmdLine.getOptionValue("dp"))
        assert(Files.exists(dataPath), {"Path $dataPath doesn't exist"})
        println("Path to data: $dataPath")

        val queryPath = Paths.get(cmdLine.getOptionValue("q"))
        assert(Files.exists(queryPath), {"Path $queryPath doesn't exist"})
        println("Path to queries: $queryPath")

        val destPath = Paths.get(cmdLine.getOptionValue("d"))
        println("Destination folder: $destPath")
        if (!Files.exists(destPath)) {
            Files.createDirectory(destPath)
        } else {
            println("Destination folder already exists. Results will be rewritten.")
            Files.list(destPath).forEach { Files.delete(it) }
        }

        val threadsCount = cmdLine.getOptionValue("t", "$DEFAULT_MAX_THREADS_COUNT").toInt()
        assert(threadsCount > 0, {"Threads count must be a positive integer"})
        println("$threadsCount threads will be used")

        val dataHolder = DataHolder(dataPath)

        val executor = Executors.newFixedThreadPool(threadsCount)
        try {
            val callables = mutableListOf<Callable<Pair<Boolean, Path>>>()
            Files.list(queryPath).forEach {
                callables.add(Callable { processQuery(it, destPath, dataHolder) })
            }
            val futures = executor.invokeAll(callables)
            var oks = 0
            futures.forEach {
                val (status, path) = it.get()
                println("${if (status) "ok" else "fail"}\t$path")
                oks += if (status) 1 else 0
            }
            println("Successfully finished: $oks out of ${futures.size}")
        } finally {
            executor.shutdownNow()
        }
    }

    override fun options(): Options {
        val options = Options()

        options.addOption(Option.builder("dp")
                .longOpt("data-path")
                .argName("PATH")
                .required(true)
                .hasArg()
                .desc("Path to directory with required internal data")
                .build())

        options.addOption(Option.builder("q")
                .longOpt("queries")
                .argName("PATH")
                .required(true)
                .hasArg()
                .desc("Path to directory with query files")
                .build())

        options.addOption(Option.builder("d")
                .longOpt("dest")
                .argName("PATH")
                .required(true)
                .hasArg()
                .desc("Path to destination directory")
                .build())

        options.addOption(Option.builder("t")
                .longOpt("threads")
                .argName("NUM")
                .required(false)
                .hasArg()
                .desc("Max threads count to be used while calculating (default $DEFAULT_MAX_THREADS_COUNT)")
                .build())

        return options
    }

    override fun name() = "bulkquery"

    override fun description() = "Run set of queries in parallel"

}