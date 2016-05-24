package gq.core.genes

import gq.core.data.Species
import org.junit.Test

import org.junit.Assert.*
import java.io.File
import java.nio.file.Paths

class FromEntrezToSymbolConverterKtTest {

    fun getPath(fileName: String) = Paths.get(
            Thread.currentThread().contextClassLoader.getResource("converter").path.toString(),
            fileName).toString()

    fun getToSymbolConverter() = FromEntrezToSymbolConverter(File(getPath("symbol-to-entrez.txt")).readGeneMappings())

    @Test(expected = NullPointerException::class)
    fun testEntrezToSymbolNoSpecies() {
        val converter = FromEntrezToSymbolConverter(File(getPath("symbol-to-entrez.txt")).readGeneMappings())
        converter[Species.RAT, 9]
    }

    @Test
    fun testConvertEntrezToSymbol() {
        val converter = FromEntrezToSymbolConverter(File(getPath("symbol-to-entrez.txt")).readGeneMappings())
        assertEquals("A2M", converter[Species.HUMAN, 2L])
        assertEquals(converter.convert(Species.HUMAN, 2L), converter[Species.HUMAN, 2L])
        assertEquals("DUPLICATE", converter[Species.HUMAN, 26L])
        assertEquals("DUPLICATE", converter[Species.HUMAN, 27L])
        assertEquals("DUPLICATE", converter[Species.HUMAN, 28L])
        assertEquals("Aanat", converter[Species.MOUSE, 11298L])
    }

    @Test
    fun testEntrezToSymbolDetailed() {
        val INVALID_GENE = 1000L
        val NOT_IN_ARGUMENTS = 10001L
        val converter = getToSymbolConverter()
        val detailed = converter.convertDetailed(Species.HUMAN, listOf(1L, 1L, 2L, 26L, 27L, INVALID_GENE))
        assertEquals("A1BG", detailed[1L])
        assertEquals("A2M", detailed[2L])
        assertEquals("DUPLICATE", detailed[26L])
        assertEquals("DUPLICATE", detailed[27L])
        assertNull(detailed[INVALID_GENE])
        assertNull(detailed[NOT_IN_ARGUMENTS])
        assertTrue(converter.convertDetailed(Species.HUMAN, emptyList()).isEmpty())
    }

    @Test
    fun testEntrezToSymbol() {
        val INVALID_GENE = 1000L
        val converter = getToSymbolConverter()
        val set = converter.convert(Species.HUMAN, listOf(1L, 1L, 2L, 26L, 27L, INVALID_GENE))
        assertEquals(setOf("A1BG", "A2M", "DUPLICATE"), set)
        assertTrue(converter.convert(Species.HUMAN, emptyList()).isEmpty())
    }
}