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
}