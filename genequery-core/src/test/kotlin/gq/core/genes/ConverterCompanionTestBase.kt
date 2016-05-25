package gq.core.genes

import java.io.File
import java.nio.file.Paths

open class ConverterCompanionTestBase {
    fun listOfL(vararg elements: Int?) = elements.map { it?.toLong() }
    fun listOfL(vararg elements: Int) = elements.map { it.toLong() }

    fun getPath(fileName: String) = Paths.get(
            Thread.currentThread().contextClassLoader.getResource("converter").path.toString(),
            fileName).toString()

    fun readMappings(fileName: String, geneFormat: GeneFormat) = File(getPath(fileName)).readAndNormalizeGeneMappings(geneFormat)

    fun createFromEntrezToSymbolConverter() = FromEntrezToSymbolConverter(readMappings("symbol-to-entrez.txt", GeneFormat.SYMBOL))

    fun createToEntrezConverter() = ToEntrezConverter()
            .populate { ToEntrezNormalizeConverterKtTest.readMappings("refseq-to-entrez.txt", GeneFormat.REFSEQ) }
            .populate { ToEntrezNormalizeConverterKtTest.readMappings("symbol-to-entrez.txt", GeneFormat.SYMBOL) }
            .populate { ToEntrezNormalizeConverterKtTest.readMappings("ensembl-to-entrez.txt", GeneFormat.ENSEMBL) }
}

