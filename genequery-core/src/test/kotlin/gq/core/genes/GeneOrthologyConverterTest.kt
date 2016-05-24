package gq.core.genes

import gq.core.data.Species
import org.junit.Assert.*
import org.junit.Test

class GeneOrthologyConverterTest {
    companion object {
        val orthology = GeneOrthologyConverter { listOf(
                OrthologyMapping(1, Species.HUMAN, 1, "1", "r1"),
                OrthologyMapping(1, Species.MOUSE, 2, "2", "r2"),
                OrthologyMapping(1, Species.RAT, 3, "3", "r3"),

                OrthologyMapping(2, Species.HUMAN, 11, "A", "r11"),
                OrthologyMapping(2, Species.MOUSE, 22, "a", "r22"),
                OrthologyMapping(2, Species.RAT, 33, "a", "r33"),

                OrthologyMapping(3, Species.HUMAN, 111, "111", "r111"),
                OrthologyMapping(3, Species.MOUSE, 222, "222", "r222"))
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun testAmbiguousMapping() {
        GeneOrthologyConverter { listOf(
                OrthologyMapping(1, Species.HUMAN, 1, "1", "r1"),
                OrthologyMapping(1, Species.HUMAN, 2, "1", "r1")
        ) }
    }

    @Test
    fun testGet() {
        assertEquals(1L, orthology[1, Species.HUMAN]?.entrezId)
        assertEquals("1", orthology[1, Species.HUMAN]?.symbolId)
        assertEquals(2L, orthology[1, Species.MOUSE]?.entrezId)
        assertEquals("2", orthology[1, Species.MOUSE]?.symbolId)

        assertEquals(1L, orthology["1", Species.HUMAN]?.entrezId)
        assertEquals("1", orthology["1", Species.HUMAN]?.symbolId)
        assertEquals(2L, orthology["1", Species.MOUSE]?.entrezId)
        assertEquals("2", orthology["1", Species.MOUSE]?.symbolId)

        assertEquals(22L, orthology["a", Species.MOUSE]?.entrezId)
        assertEquals(33L, orthology["a", Species.RAT]?.entrezId)
        assertEquals(11L, orthology["a", Species.HUMAN]?.entrezId)
        assertEquals(11L, orthology["A", Species.HUMAN]?.entrezId)
        assertEquals(22L, orthology["A", Species.MOUSE]?.entrezId)

        assertNull(orthology[100, Species.HUMAN])
        assertNull(orthology[111, Species.RAT])
    }

    @Test
    fun testEntrezToEntrezDetailed() {
        assertEquals(
                mapOf(1L to 1L, 11L to 11L, 100L to null),
                orthology.entrezToEntrezDetailed(listOf<Long>(1, 11, 100), Species.HUMAN)
        )
        assertEquals(
                mapOf(1L to 2L, 11L to 22L, 100L to null),
                orthology.entrezToEntrezDetailed(listOf<Long>(1, 11, 100), Species.MOUSE)
        )
        assertEquals(
                mapOf(1L to 3L, 11L to 33L, 111L to null, 100L to null),
                orthology.entrezToEntrezDetailed(listOf<Long>(1, 11, 111, 100), Species.RAT)
        )
    }

    @Test
    fun testEntrezToSymbolDetailed() {
        assertEquals(
                mapOf(1L to "1", 11L to "A", 100L to null),
                orthology.entrezToSymbolDetailed(listOf<Long>(1, 11, 100), Species.HUMAN)
        )
        assertEquals(
                mapOf(1L to "2", 11L to "a", 100L to null),
                orthology.entrezToSymbolDetailed(listOf<Long>(1, 11, 100), Species.MOUSE)
        )
        assertEquals(
                mapOf(1L to "3", 11L to "a", 111L to null, 100L to null),
                orthology.entrezToSymbolDetailed(listOf<Long>(1, 11, 111, 100), Species.RAT)
        )
    }

    @Test
    fun testSymbolToEntrezDetailed() {
        assertEquals(
                mapOf("1" to 1L, "A" to 11L, "non" to null),
                orthology.symbolToEntrezDetailed(listOf("1", "A", "non"), Species.HUMAN)
        )
        assertEquals(
                mapOf("1" to 2L, "A" to 22L, "non" to null),
                orthology.symbolToEntrezDetailed(listOf("1", "A", "non"), Species.MOUSE)
        )
        assertEquals(
                mapOf("1" to 3L, "A" to 33L, "111" to null, "non" to null),
                orthology.symbolToEntrezDetailed(listOf("1", "A", "111", "non"), Species.RAT)
        )
    }

    @Test
    fun testSymbolToSymbolDetailed() {
        assertEquals(
                mapOf("1" to "1", "A" to "A", "non" to null),
                orthology.symbolToSymbolDetailed(listOf("1", "A", "non"), Species.HUMAN)
        )
        assertEquals(
                mapOf("1" to "2", "A" to "a", "non" to null),
                orthology.symbolToSymbolDetailed(listOf("1", "A", "non"), Species.MOUSE)
        )
        assertEquals(
                mapOf("1" to "3", "A" to "a", "111" to null, "non" to null),
                orthology.symbolToSymbolDetailed(listOf("1", "A", "111", "non"), Species.RAT)
        )
    }

    @Test
    fun testBulkConverting() {
        assertEquals(listOf<Long>(1, 11), orthology.bulkEntrezToEntrez(listOf<Long>(1, 11, 100), Species.HUMAN))
        assertEquals(listOf<Long>(2, 22), orthology.bulkEntrezToEntrez(listOf<Long>(1, 11, 100), Species.MOUSE))
        assertEquals(listOf<Long>(3, 33), orthology.bulkEntrezToEntrez(listOf<Long>(1, 11, 111), Species.RAT))

        assertEquals(listOf("1", "A"), orthology.bulkEntrezToSymbol(listOf<Long>(1, 11, 100), Species.HUMAN))
        assertEquals(listOf("2", "a"), orthology.bulkEntrezToSymbol(listOf<Long>(1, 11, 100), Species.MOUSE))
        assertEquals(listOf("3", "a"), orthology.bulkEntrezToSymbol(listOf<Long>(1, 11, 111), Species.RAT))

        assertEquals(listOf(1L, 11L), orthology.bulkSymbolToEntrez(listOf("1", "A", "non"), Species.HUMAN))
        assertEquals(listOf(2L, 22L), orthology.bulkSymbolToEntrez(listOf("1", "A", "non"), Species.MOUSE))
        assertEquals(listOf(3L, 33L), orthology.bulkSymbolToEntrez(listOf("1", "A", "111", "non"), Species.RAT))

        assertEquals(listOf("1", "A"), orthology.bulkSymbolToSymbol(listOf("1", "A", "non"), Species.HUMAN))
        assertEquals(listOf("2", "a"), orthology.bulkSymbolToSymbol(listOf("1", "A", "non"), Species.MOUSE))
        assertEquals(listOf("3", "a"), orthology.bulkSymbolToSymbol(listOf("1", "A", "111", "non"), Species.RAT))
    }
}