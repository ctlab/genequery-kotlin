package gq.console.commands

import gq.console.DataHolder
import org.apache.commons.cli.*
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.stream.Collector
import java.util.stream.Collectors

fun processQuery(pathToQuery: Path, destPath: Path, dataHolder: DataHolder): Path {
    return pathToQuery
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
        }

        val threadsCount = cmdLine.getOptionValue("t", "$DEFAULT_MAX_THREADS_COUNT").toInt()
        assert(threadsCount > 0, {"Threads count must be a positive integer"})
        println("$threadsCount threads will be used")

        val dataHolder = DataHolder(dataPath)

        val executor = Executors.newFixedThreadPool(threadsCount)
        try {
            val callables = mutableListOf<Callable<Path>>()
            Files.list(queryPath).map {
                callables.add(Callable { processQuery(it, destPath, dataHolder) })
            }
            val futures = executor.invokeAll(callables)


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