package gq.core.genes

import gq.core.data.Species
import org.junit.Test

import org.junit.Assert.*
import java.io.File
import java.nio.file.Paths

class GeneConverterTest {

    fun getPath(fileName: String) = Paths.get(
            Thread.currentThread().contextClassLoader.getResource("converter").path.toString(),
            fileName).toString()

    fun getConverter() = GeneConverter()
            .populateOtherToEntrez { File(getPath("refseq-to-entrez.txt")).readGeneMappings() }
            .populateOtherToEntrez { File(getPath("symbol-to-entrez.txt")).readGeneMappings() }
            .populateOtherToEntrez { File(getPath("ensembl-to-entrez.txt")).readGeneMappings() }
            .populateEntrezToSymbol { File(getPath("symbol-to-entrez.txt")).readGeneMappings() }

    @Test
    fun testConvertOtherToEntrez() {
        val converter = GeneConverter().populateOtherToEntrez { File(getPath("refseq-to-entrez.txt")).readGeneMappings() }
        assertEquals(9L, converter[Species.HUMAN, "NM_001160175"])
        assertNull(converter[Species.HUMAN, "NM_00116017500000"])

        converter.populateOtherToEntrez { File(getPath("symbol-to-entrez.txt")).readGeneMappings() }
        assertEquals(2L, converter[Species.HUMAN, "A2M"])
        assertEquals(26L, converter[Species.HUMAN, "DUPLICATE"])
        assertEquals(11287L, converter[Species.MOUSE, "Pzp"])
        assertNull(converter[Species.HUMAN, "Pzp"])

        converter.populateOtherToEntrez { File(getPath("ensembl-to-entrez.txt")).readGeneMappings() }
        assertEquals(24L, converter[Species.HUMAN, "ENSG00000198691"])
        assertEquals(27L, converter[Species.HUMAN, "ENSG10000002726"])
        assertEquals(27L, converter[Species.HUMAN, "ENSG20000002726"])
    }

    @Test
    fun testConvertOtherToEntrez2() {
        val converter = GeneConverter()
                .populateOtherToEntrez { File(getPath("refseq-to-entrez.txt")).readGeneMappings() }
                .populateOtherToEntrez { File(getPath("symbol-to-entrez.txt")).readGeneMappings() }
                .populateOtherToEntrez { File(getPath("ensembl-to-entrez.txt")).readGeneMappings() }

        assertEquals(9L, converter[Species.HUMAN, "NM_001160175"])
        assertEquals(2L, converter[Species.HUMAN, "A2M"])
        assertEquals(26L, converter[Species.HUMAN, "DUPLICATE"])
        assertEquals(24L, converter[Species.HUMAN, "ENSG00000198691"])
        assertEquals(27L, converter[Species.HUMAN, "ENSG10000002726"])
        assertEquals(27L, converter[Species.HUMAN, "ENSG20000002726"])
    }

    @Test(expected = NullPointerException::class)
    fun testPopulateOtherToEntrezNoSpecies() {
        val converter = GeneConverter().populateOtherToEntrez { File(getPath("refseq-to-entrez.txt")).readGeneMappings() }
        converter[Species.RAT, "NM_001160175"]
    }

    @Test(expected = NullPointerException::class)
    fun testEntrezToSymbolNoSpecies() {
        val converter = GeneConverter().populateOtherToEntrez { File(getPath("symbol-to-entrez.txt")).readGeneMappings() }
        converter[Species.RAT, 9]
    }

    @Test
    fun testConvertEntrezToSymbol() {
        val converter = GeneConverter().populateEntrezToSymbol { File(getPath("symbol-to-entrez.txt")).readGeneMappings() }
        assertEquals("A2M", converter[Species.HUMAN, 2L])
        assertEquals("DUPLICATE", converter[Species.HUMAN, 26L])
        assertEquals("DUPLICATE", converter[Species.HUMAN, 27L])
        assertEquals("DUPLICATE", converter[Species.HUMAN, 28L])
        assertEquals("Aanat", converter[Species.MOUSE, 11298L])
    }

    @Test
    fun testOtherToEntrezDetailed() {
        val converter = getConverter()
        val detailed = converter.otherToEntrezDetailed(
                Species.HUMAN,
                listOf("NM_001160175", "A2M", "A2M", "ENSG00000175899", "not-a-gene", "DUPLICATE"))
        assertEquals(9L, detailed["NM_001160175"])
        assertEquals(2L, detailed["A2M"])
        assertEquals(26L, detailed["DUPLICATE"])
        assertEquals(2L, detailed["ENSG00000175899"])
        assertNull(detailed["not-a-gene"])
        assertNull(detailed["not-in-arguments"])
        assertTrue(converter.otherToEntrezDetailed(Species.HUMAN, emptyList()).isEmpty())
    }

    @Test
    fun testOtherToEntrez() {
        val converter = getConverter()
        val set = converter.otherToEntrez(Species.HUMAN, listOf("NM_001160175", "A2M", "ENSG00000175899", "not-a-gene"))
        assertEquals(setOf(2L, 9L), set)
        assertTrue(converter.otherToEntrez(Species.HUMAN, emptyList()).isEmpty())
    }

    @Test
    fun testEntrezToSymbolDetailed() {
        val INVALID_GENE = 1000L
        val NOT_IN_ARGUMENTS = 10001L
        val converter = getConverter()
        val detailed = converter.entrezToSymbolDetailed(Species.HUMAN, listOf(1L, 1L, 2L, 26L, 27L, INVALID_GENE))
        assertEquals("A1BG", detailed[1L])
        assertEquals("A2M", detailed[2L])
        assertEquals("DUPLICATE", detailed[26L])
        assertEquals("DUPLICATE", detailed[27L])
        assertNull(detailed[INVALID_GENE])
        assertNull(detailed[NOT_IN_ARGUMENTS])
        assertTrue(converter.entrezToSymbolDetailed(Species.HUMAN, emptyList()).isEmpty())
    }

    @Test
    fun testEntrezToSymbol() {
        val INVALID_GENE = 1000L
        val converter = getConverter()
        val set = converter.entrezToSymbol(Species.HUMAN, listOf(1L, 1L, 2L, 26L, 27L, INVALID_GENE))
        assertEquals(setOf("A1BG", "A2M", "DUPLICATE"), set)
        assertTrue(converter.entrezToSymbol(Species.HUMAN, emptyList()).isEmpty())
    }

}