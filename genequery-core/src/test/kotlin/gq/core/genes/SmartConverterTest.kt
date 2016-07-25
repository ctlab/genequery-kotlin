package gq.core.genes

import gq.core.data.Species
import org.junit.Assert.assertEquals
import org.junit.Test

class SmartConverterTest {

    companion object : ConverterCompanionTestBase() {

        val toEntrezConverter = createToEntrezConverter()
        val fromEntrezConverter = createFromEntrezToSymbolConverter()
        val orthologyConverter = GeneOrthologyConverter(listOf(
                OrthologyMapping(1, Species.HUMAN, 1L, "A1", "NM_130786"),
                OrthologyMapping(1, Species.MOUSE, 11L, "a1", "XR_11"),
                OrthologyMapping(1, Species.RAT, 111L, "a1", "XR_111"),

                OrthologyMapping(2, Species.HUMAN, 2L, "A2", "NM_000014"),
                OrthologyMapping(2, Species.MOUSE, 22L, "a2", "XR_22"),

                OrthologyMapping(3, Species.HUMAN, 3L, "A3", "XR_3"),
                OrthologyMapping(3, Species.MOUSE, 33L, "a3", "XR_33"),
                OrthologyMapping(3, Species.RAT, 333L, "a3", "XR_333")
        ))

        val smartConverter = SmartConverter(toEntrezConverter, fromEntrezConverter, orthologyConverter)
    }

    @Test
    fun testEntrezToEntrez() {
        assertEquals(
                smartConverter.toEntrez(listOfL(1, 2, 3), Species.HUMAN).values.toList(),
                smartConverter.toEntrez("1 2 3".split(" "), GeneFormat.ENTREZ, Species.HUMAN).values.toList())
        assertEquals(
                mapOf(1L to 11L, 2L to 22L, 3L to 33L, 4L to null),
                smartConverter.toEntrez(listOfL(1, 2, 3, 3, 4), Species.HUMAN, Species.MOUSE))
        assertEquals(
                mapOf(111L to 1L, 333L to 3L, 101L to null),
                smartConverter.toEntrez(listOfL(111, 333, 101), Species.RAT, Species.HUMAN))
        assertEquals(
                mapOf("1" to 111L, "2" to null, "3" to 333L),
                smartConverter.toEntrez("1 2 2 3".split(" "), GeneFormat.ENTREZ, Species.HUMAN, Species.RAT))
    }

    @Test(expected = NullPointerException::class)
    fun testEntrezToSymbolNPENoSpecies() {
        smartConverter.toSymbol(listOf("1"), GeneFormat.ENTREZ, Species.RAT)
    }

    @Test
    fun testEntrezToSymbol() {
        assertEquals(
                smartConverter.toSymbol(listOfL(1, 2, 3), Species.HUMAN).values.toList(),
                smartConverter.toSymbol(listOf("1", "2", "3"), GeneFormat.ENTREZ, Species.HUMAN).values.toList())
        assertEquals(
                listOf("A1BG", "A2M", "A2MP1", null, "DUPLICATE"),
                smartConverter.toSymbol(listOf("1", "2", "3", "4", "27"), GeneFormat.ENTREZ, Species.HUMAN).values.toList())

        assertEquals(
                mapOf(1L to "a1", 2L to "a2", 3L to "a3", 4L to null),
                smartConverter.toSymbol(listOfL(1, 2, 3, 3, 4), Species.HUMAN, Species.MOUSE))
        assertEquals(
                mapOf(111L to "A1", 333L to "A3", 101L to null),
                smartConverter.toSymbol(listOfL(111, 333, 101), Species.RAT, Species.HUMAN))
        assertEquals(
                mapOf("1" to "a1", "2" to null, "3" to "a3", "4" to null),
                smartConverter.toSymbol(listOf("1", "2", "2", "3", "4"), GeneFormat.ENTREZ, Species.HUMAN, Species.RAT))
    }

    @Test
    fun testFromEnsembl() {
        val genes = "ENSG00000121410 ENSG00000175899 ENSG10000002726 ENSG20000002726 ENSG2000000".split(" ")
        val toEntrez = smartConverter.toEntrez(genes, GeneFormat.ENSEMBL, Species.HUMAN).values.toList()

        assertEquals(toEntrezConverter.convertDetailed(Species.HUMAN, genes).values.toList(), toEntrez)
        assertEquals(listOfL(1, 2, 27, 27, null), toEntrez)

        assertEquals(
                listOf("A1BG", "A2M", "DUPLICATE", "DUPLICATE", null),
                smartConverter.toSymbol(genes, GeneFormat.ENSEMBL, Species.HUMAN).values.toList())

        val genes2 = "ENSG00000121410 ENSG00000256069 ENSG00000175899 ENSG0000000".split(" ")
        assertEquals(
                listOfL(111, 333, null, null),
                smartConverter.toEntrez(genes2, GeneFormat.ENSEMBL, Species.HUMAN, Species.RAT).values.toList())
        assertEquals(
                listOf("a1", "a3", null, null),
                smartConverter.toSymbol(genes2, GeneFormat.ENSEMBL, Species.HUMAN, Species.RAT).values.toList())
    }

    @Test
    fun testFromRefseq() {
        val genes = "NM_130786 XM_006719056 NM_000662 NM_000".split(" ")
        val toEntrez = smartConverter.toEntrez(genes, GeneFormat.REFSEQ, Species.HUMAN).values.toList()

        assertEquals(toEntrezConverter.convertDetailed(Species.HUMAN, genes).values.toList(), toEntrez)
        assertEquals(listOfL(1, 2, 9, null), toEntrez)
        assertEquals(
                listOf("A1BG", "A2M", "NAT1", null),
                smartConverter.toSymbol(genes, GeneFormat.REFSEQ, Species.HUMAN).values.toList())

        assertEquals(
                listOfL(111, null, null, null),
                smartConverter.toEntrez(genes, GeneFormat.REFSEQ, Species.HUMAN, Species.RAT).values.toList()
        )

        assertEquals(
                listOf("a1", null, null, null),
                smartConverter.toSymbol(genes, GeneFormat.REFSEQ, Species.HUMAN, Species.RAT).values.toList()
        )
    }

    @Test
    fun testSymbolToSymbol() {
        assertEquals("A B C D".split(" "), smartConverter.toSymbol("a b c d".split(" "), GeneFormat.SYMBOL, Species.HUMAN).values.toList())
        assertEquals("A B C D".split(" "), smartConverter.toSymbol("a b c d".split(" "), GeneFormat.SYMBOL, Species.RAT).values.toList())
        assertEquals(
                listOf("A1", "A3", null),
                smartConverter.toSymbol("a1 a3 a4".split(" "), GeneFormat.SYMBOL, Species.RAT, Species.HUMAN).values.toList())
        assertEquals(
                listOf("a1", null, "a3"),
                smartConverter.toSymbol("a1 a2 a3".split(" "), GeneFormat.SYMBOL, Species.HUMAN, Species.RAT).values.toList())
    }

    @Test
    fun testSymbolToEntrez() {
        assertEquals(listOfL(1, 2, null),
                smartConverter.toEntrez("A1BG A2M NON".split(" "), GeneFormat.SYMBOL, Species.HUMAN).values.toList())

        assertEquals(
                listOfL(1, 3, null),
                smartConverter.toEntrez("a1 a3 a4".split(" "), GeneFormat.SYMBOL, Species.RAT, Species.HUMAN).values.toList())
        assertEquals(
                listOfL(111, null, null, 333),
                smartConverter.toEntrez("a1 a2 DUPLICATE a3".split(" "), GeneFormat.SYMBOL, Species.HUMAN, Species.RAT).values.toList())
    }
}