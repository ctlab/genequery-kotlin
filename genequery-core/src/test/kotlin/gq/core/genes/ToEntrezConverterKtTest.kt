package gq.core.genes

import gq.core.data.Species
import org.junit.Test

import org.junit.Assert.*
import java.io.File
import java.nio.file.Paths

class ToEntrezConverterKtTest {
    companion object {
        fun getPath(fileName: String) = Paths.get(
                Thread.currentThread().contextClassLoader.getResource("converter").path.toString(),
                fileName).toString()

        fun readMappings(fileName: String, geneFormat: GeneFormat) = File(getPath(fileName)).readAndNormalizeGeneMappings(geneFormat)

        val converter = ToEntrezConverter()
                .populate { readMappings("refseq-to-entrez.txt", GeneFormat.REFSEQ) }
                .populate { readMappings("symbol-to-entrez.txt", GeneFormat.SYMBOL) }
                .populate { readMappings("ensembl-to-entrez.txt", GeneFormat.ENSEMBL) }
    }

    @Test
    fun testConvertOtherToEntrez() {
        val converter = ToEntrezConverter(readMappings("refseq-to-entrez.txt", GeneFormat.REFSEQ))
        assertEquals(9L, converter[Species.HUMAN, "NM_001160175"])
        assertEquals(converter.convert(Species.HUMAN, "NM_001160175"), converter[Species.HUMAN, "NM_001160175"])
        assertNull(converter[Species.HUMAN, "NM_00116017500000"])

        converter.populate(readMappings("symbol-to-entrez.txt", GeneFormat.SYMBOL))
        assertEquals(2L, converter[Species.HUMAN, "A2M"])
        assertEquals(26L, converter[Species.HUMAN, "DUPLICATE"])
        assertEquals(11287L, converter[Species.MOUSE, "PZP"])
        assertNull(converter[Species.MOUSE, "Pzp"])
        assertNull(converter[Species.HUMAN, "PZP"])

        converter.populate(readMappings("ensembl-to-entrez.txt", GeneFormat.ENSEMBL))
        assertEquals(24L, converter[Species.HUMAN, "ENSG00000198691"])
        assertEquals(27L, converter[Species.HUMAN, "ENSG10000002726"])
        assertEquals(27L, converter[Species.HUMAN, "ENSG20000002726"])
    }

    @Test
    fun testConvertOtherToEntrez2() {
        assertEquals(9L, converter[Species.HUMAN, "NM_001160175"])
        assertEquals(2L, converter[Species.HUMAN, "A2M"])
        assertEquals(26L, converter[Species.HUMAN, "DUPLICATE"])
        assertEquals(24L, converter[Species.HUMAN, "ENSG00000198691"])
        assertEquals(27L, converter[Species.HUMAN, "ENSG10000002726"])
        assertEquals(27L, converter[Species.HUMAN, "ENSG20000002726"])
    }

    @Test(expected = NullPointerException::class)
    fun testPopulateOtherToEntrezNoSpecies() {
        val converter = ToEntrezConverter(readMappings("refseq-to-entrez.txt", GeneFormat.REFSEQ))
        converter[Species.RAT, "NM_001160175"]
    }

    @Test
    fun testOtherToEntrezDetailed() {
        val detailed = converter.convertDetailed(
                Species.HUMAN,
                listOf("NM_001160175", "A2M", "A2M", "ENSG00000175899", "not-a-gene", "DUPLICATE"))
        assertEquals(9L, detailed["NM_001160175"])
        assertEquals(2L, detailed["A2M"])
        assertEquals(26L, detailed["DUPLICATE"])
        assertEquals(2L, detailed["ENSG00000175899"])
        assertNull(detailed["not-a-gene"])
        assertNull(detailed["not-in-arguments"])
        assertTrue(converter.convertDetailed(Species.HUMAN, emptyList()).isEmpty())
    }

    @Test
    fun testOtherToEntrez() {
        val set = converter.convert(Species.HUMAN, listOf("NM_001160175", "A2M", "ENSG00000175899", "not-a-gene"))
        assertEquals(setOf(2L, 9L), set)
        assertTrue(converter.convert(Species.HUMAN, emptyList()).isEmpty())
    }
}