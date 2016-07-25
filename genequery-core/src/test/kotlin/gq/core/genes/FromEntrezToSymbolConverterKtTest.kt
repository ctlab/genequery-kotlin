package gq.core.genes

import gq.core.data.Species
import org.junit.Assert.*
import org.junit.Test

class FromEntrezToSymbolConverterKtTest {

    companion object CompanionTest : ConverterCompanionTestBase() {
        val fromEntrezConverter = createFromEntrezToSymbolConverter()
    }

    @Test(expected = NullPointerException::class)
    fun testEntrezToSymbolNoSpecies() {
        val converter = FromEntrezToSymbolConverter(readMappings("symbol-to-entrez.txt", GeneFormat.SYMBOL))
        converter[Species.RAT, 9]
    }

    @Test
    fun testConvertEntrezToSymbol() {
        val converter = FromEntrezToSymbolConverter(readMappings("symbol-to-entrez.txt", GeneFormat.SYMBOL))
        assertEquals("A2M", converter[Species.HUMAN, 2L])
        assertEquals(converter.convert(Species.HUMAN, 2L), converter[Species.HUMAN, 2L])
        assertEquals("DUPLICATE", converter[Species.HUMAN, 26L])
        assertEquals("DUPLICATE", converter[Species.HUMAN, 27L])
        assertEquals("DUPLICATE", converter[Species.HUMAN, 28L])
        assertEquals("AANAT", converter[Species.MOUSE, 11298L])
    }

    @Test
    fun testEntrezToSymbolDetailed() {
        val INVALID_GENE = 1000
        val NOT_IN_ARGUMENTS = 10001
        val detailed = fromEntrezConverter.convertDetailed(Species.HUMAN, listOfL(1, 1, 2, 26, 27, INVALID_GENE))
        assertEquals("A1BG", detailed[1L])
        assertEquals("A2M", detailed[2L])
        assertEquals("DUPLICATE", detailed[26L])
        assertEquals("DUPLICATE", detailed[27L])
        assertNull(detailed[INVALID_GENE.toLong()])
        assertNull(detailed[NOT_IN_ARGUMENTS.toLong()])
        assertTrue(fromEntrezConverter.convertDetailed(Species.HUMAN, emptyList()).isEmpty())
    }

    @Test
    fun testEntrezToSymbol() {
        val INVALID_GENE = 1000
        val set = fromEntrezConverter.convert(Species.HUMAN, listOfL(1, 1, 2, 26, 27, INVALID_GENE))
        assertEquals(setOf("A1BG", "A2M", "DUPLICATE"), set)
        assertTrue(fromEntrezConverter.convert(Species.HUMAN, emptyList()).isEmpty())
    }
}