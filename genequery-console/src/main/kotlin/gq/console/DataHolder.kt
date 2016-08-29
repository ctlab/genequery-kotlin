package gq.console

import gq.core.data.*
import gq.core.genes.*
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class DataHolder(val dataPath: String) {
    constructor(dataPath: Path) : this(dataPath.toString())

    fun pathToOrthology() = Paths.get(dataPath, "orthology.tsv").toString()
    fun pathRefseqToEntrez() = Paths.get(dataPath, "refseq-to-entrez.tsv").toString()
    fun pathEnsemblToEntrez() = Paths.get(dataPath, "ensembl-to-entrez.tsv").toString()
    fun pathSymbolToEntrez() = Paths.get(dataPath, "symbol-to-entrez.tsv").toString()
    fun pathGseInfo() = Paths.get(dataPath, "gse-info.txt").toString()

    fun pathToGMT(species: Species) = Paths.get(dataPath, "$species.modules.gmt").toString()

    val toEntrezConverter = ToEntrezConverter().populate {
        println("Populate gene converter to-entrez from ${pathEnsemblToEntrez()}")
        File(pathEnsemblToEntrez()).readAndNormalizeGeneMappings(GeneFormat.ENSEMBL)
    }.populate {
        println("Populate gene converter to-entrez from ${pathRefseqToEntrez()}")
        File(pathRefseqToEntrez()).readAndNormalizeGeneMappings(GeneFormat.REFSEQ)
    }.populate {
        println("Populate gene converter to-entrez from ${pathSymbolToEntrez()}")
        File(pathSymbolToEntrez()).readAndNormalizeGeneMappings(GeneFormat.SYMBOL)
    }

    val fromEntrezToSymbolConverter = FromEntrezToSymbolConverter().populate {
        println("Populate gene converter from entrez to symbol from ${pathSymbolToEntrez()}")
        File(pathSymbolToEntrez()).readAndNormalizeGeneMappings(GeneFormat.SYMBOL)
    }


    val orthologyConverter = GeneOrthologyConverter {
        println("Initialize orthology converter from ${pathToOrthology()}")
        File(pathToOrthology()).readAndNormalizeGeneOrthologyMappings()
    }

    val smartConverter = SmartConverter(toEntrezConverter, fromEntrezToSymbolConverter, orthologyConverter)

    val moduleCollection = GQModuleCollection {
        val availableGmtFiles = Species.values()
                .map { Pair(it, pathToGMT(it)) }
                .filter { Files.exists(Paths.get(it.second)) }
        println("Initialize module collection from ${availableGmtFiles.joinToString(",")}")
        readModulesFromFiles(availableGmtFiles)
    }

    val gseInfoCollection = GQGseInfoCollection {
        println("Populate GSE info from ${pathGseInfo()}")
        readGseInfoFromFile(pathGseInfo())
    }
}


