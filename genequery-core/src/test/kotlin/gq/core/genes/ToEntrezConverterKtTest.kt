package gq.core.genes

import gq.core.data.Species
import org.junit.Test

import org.junit.Assert.*
import java.io.File
import java.nio.file.Paths

class ToEntrezConverterKtTest {

    fun getPath(fileName: String) = Paths.get(
            Thread.currentThread().contextClassLoader.getResource("converter").path.toString(),
            fileName).toString()

    fun getToEntrezConverter() = ToEntrezConverter()
            .populate { File(getPath("refseq-to-entrez.txt")).readGeneMappings() }
            .populate { File(getPath("symbol-to-entrez.txt")).readGeneMappings() }
            .populate { File(getPath("ensembl-to-entrez.txt")).readGeneMappings() }

    @Test
    fun testConvertOtherToEntrez() {
        val converter = ToEntrezConverter(File(getPath("refseq-to-entrez.txt")).readGeneMappings())
        assertEquals(9L, converter[Species.HUMAN, "NM_001160175"])
        assertEquals(converter.convert(Species.HUMAN, "NM_001160175"), converter[Species.HUMAN, "NM_001160175"])
        assertNull(converter[Species.HUMAN, "NM_00116017500000"])

        converter.populate(File(getPath("symbol-to-entrez.txt")).readGeneMappings())
        assertEquals(2L, converter[Species.HUMAN, "A2M"])
        assertEquals(26L, converter[Species.HUMAN, "DUPLICATE"])
        assertEquals(11287L, converter[Species.MOUSE, "Pzp"])
        assertNull(converter[Species.HUMAN, "Pzp"])

        converter.populate(File(getPath("ensembl-to-entrez.txt")).readGeneMappings())
        assertEquals(24L, converter[Species.HUMAN, "ENSG00000198691"])
        assertEquals(27L, converter[Species.HUMAN, "ENSG10000002726"])
        assertEquals(27L, converter[Species.HUMAN, "ENSG20000002726"])
    }

    @Test
    fun testConvertOtherToEntrez2() {
        val converter = ToEntrezConverter()
                .populate(File(getPath("refseq-to-entrez.txt")).readGeneMappings())
                .populate(File(getPath("symbol-to-entrez.txt")).readGeneMappings())
                .populate(File(getPath("ensembl-to-entrez.txt")).readGeneMappings())

        assertEquals(9L, converter[Species.HUMAN, "NM_001160175"])
        assertEquals(2L, converter[Species.HUMAN, "A2M"])
        assertEquals(26L, converter[Species.HUMAN, "DUPLICATE"])
        assertEquals(24L, converter[Species.HUMAN, "ENSG00000198691"])
        assertEquals(27L, converter[Species.HUMAN, "ENSG10000002726"])
        assertEquals(27L, converter[Species.HUMAN, "ENSG20000002726"])
    }

    @Test(expected = NullPointerException::class)
    fun testPopulateOtherToEntrezNoSpecies() {
        val converter = ToEntrezConverter(File(getPath("refseq-to-entrez.txt")).readGeneMappings())
        converter[Species.RAT, "NM_001160175"]
    }

    @Test
    fun testOtherToEntrezDetailed() {
        val converter = getToEntrezConverter()
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
        val converter = getToEntrezConverter()
        val set = converter.convert(Species.HUMAN, listOf("NM_001160175", "A2M", "ENSG00000175899", "not-a-gene"))
        assertEquals(setOf(2L, 9L), set)
        assertTrue(converter.convert(Species.HUMAN, emptyList()).isEmpty())
    }
}