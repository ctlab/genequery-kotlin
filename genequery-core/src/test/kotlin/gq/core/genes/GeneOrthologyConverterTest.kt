package gq.core.genes

import gq.core.data.Species
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GeneOrthologyConverterTest {
    companion object {
        val orthology = GeneOrthologyConverter {
            listOf(
                    OrthologyMapping(1, Species.HUMAN, 1, "1", "r1"),
                    OrthologyMapping(1, Species.MOUSE, 2, "2", "r2"),
                    OrthologyMapping(1, Species.RAT, 3, "3", "r3"),

                    OrthologyMapping(2, Species.HUMAN, 11, "A", "r11"),
                    OrthologyMapping(2, Species.MOUSE, 22, "a", "r22"),
                    OrthologyMapping(2, Species.RAT, 33, "a", "r33"),

                    OrthologyMapping(3, Species.HUMAN, 111, "111", "r111"),
                    OrthologyMapping(3, Species.MOUSE, 223, "223", "r222"),
                    OrthologyMapping(3, Species.MOUSE, 222, "222", "r223"),
                    OrthologyMapping(3, Species.MOUSE, 224, "222", "r224"))
        }
    }

    @Test
    fun testAmbiguousMapping() {
        assertEquals(mapOf(111L to 222L, 224L to 222L, 100L to null),
                orthology.getOrthologyByEntrez(listOf<Long>(111, 224, 100), Species.MOUSE).mapValues { it.value?.entrezId })
        assertEquals(mapOf(222L to 111L, 223L to 111L, 100L to null),
                orthology.getOrthologyByEntrez(listOf<Long>(222, 223, 100), Species.HUMAN).mapValues { it.value?.entrezId })
        assertEquals(mapOf(111L to 222L, 224L to 222L, 100L to null),
                orthology.getOrthologyByEntrez(listOf<Long>(111, 224, 100), Species.MOUSE).mapValues { it.value?.entrezId })
        assertEquals(mapOf("r224" to 111L, "r223" to 111L, "r222" to 111L),
                orthology.getOrthologyByRefseq(listOf("r224", "r223", "r222"), Species.HUMAN).mapValues { it.value?.entrezId })
    }

    @Test
    fun testGet() {
        assertEquals(1L, orthology.getOrthologyByEntrez(1, Species.HUMAN)?.entrezId)
        assertEquals("1", orthology.getOrthologyByEntrez(1, Species.HUMAN)?.symbolId)
        assertEquals(2L, orthology.getOrthologyByEntrez(1, Species.MOUSE)?.entrezId)
        assertEquals("2", orthology.getOrthologyByEntrez(1, Species.MOUSE)?.symbolId)

        assertEquals(1L, orthology.getOrthologyBySymbol("1", Species.HUMAN)?.entrezId)
        assertEquals("1", orthology.getOrthologyBySymbol("1", Species.HUMAN)?.symbolId)
        assertEquals(2L, orthology.getOrthologyBySymbol("1", Species.MOUSE)?.entrezId)
        assertEquals("2", orthology.getOrthologyBySymbol("1", Species.MOUSE)?.symbolId)

        assertEquals(22L, orthology.getOrthologyBySymbol("a", Species.MOUSE)?.entrezId)
        assertEquals(33L, orthology.getOrthologyBySymbol("a", Species.RAT)?.entrezId)
        assertEquals(11L, orthology.getOrthologyBySymbol("a", Species.HUMAN)?.entrezId)
        assertEquals(11L, orthology.getOrthologyBySymbol("A", Species.HUMAN)?.entrezId)
        assertEquals(22L, orthology.getOrthologyBySymbol("A", Species.MOUSE)?.entrezId)

        assertEquals(2L, orthology.getOrthologyByRefseq("r1", Species.MOUSE)?.entrezId)

        assertNull(orthology.getOrthologyByEntrez(100, Species.HUMAN))
        assertNull(orthology.getOrthologyByEntrez(111, Species.RAT))
    }

    @Test
    fun testBulkConverting() {
        assertEquals(mapOf(1L to 1L, 11L to 11L, 100L to null),
                orthology.getOrthologyByEntrez(listOf<Long>(1, 11, 100), Species.HUMAN).mapValues { it.value?.entrezId })
        assertEquals(mapOf(1L to "2", 11L to "a", 100L to null),
                orthology.getOrthologyByEntrez(listOf<Long>(1, 11, 100), Species.MOUSE).mapValues { it.value?.symbolId })
        assertEquals(mapOf(1L to 3L, 11L to 33L, 111L to null),
                orthology.getOrthologyByEntrez(listOf<Long>(1, 11, 111), Species.RAT).mapValues { it.value?.entrezId })
        assertEquals(mapOf(1L to "3", 11L to "a", 111L to null),
                orthology.getOrthologyByEntrez(listOf<Long>(1, 11, 111), Species.RAT).mapValues { it.value?.symbolId })

        assertEquals(mapOf("1" to 1L, "A" to 11L, "non" to null),
                orthology.getOrthologyBySymbol(listOf("1", "A", "non"), Species.HUMAN).mapValues { it.value?.entrezId })
        assertEquals(mapOf("1" to "2", "A" to "a", "non" to null),
                orthology.getOrthologyBySymbol(listOf("1", "A", "non"), Species.MOUSE).mapValues { it.value?.symbolId })
        assertEquals(mapOf("1" to 3L, "A" to 33L, "111" to null, "non" to null),
                orthology.getOrthologyBySymbol(listOf("1", "A", "111", "non"), Species.RAT).mapValues { it.value?.entrezId })

        assertEquals(mapOf("r1" to 3L, "r11" to 33L, "r111" to null, "non" to null),
                orthology.getOrthologyByRefseq(listOf("r1", "r11", "r111", "non"), Species.RAT).mapValues { it.value?.entrezId })
    }

    @Test(expected = IllegalArgumentException::class)
    fun testGetOrthologyFail1() {
        orthology.getOrthology(listOf("ENSG00000165029"), Species.HUMAN)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testGetOrthologyFail2() {
        orthology.getOrthology(listOf("Abc"), Species.HUMAN, GeneFormat.ENSEMBL)
    }
}