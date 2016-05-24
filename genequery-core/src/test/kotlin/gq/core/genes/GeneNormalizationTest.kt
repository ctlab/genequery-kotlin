package gq.core.genes

import org.junit.Test

import org.junit.Assert.*

class GeneNormalizationTest {

    @Test
    fun testMapGenesToNormalizedNoSpecifiedFormat() {
        assertEquals(mapGenesToNormalized(listOf("1")), mapGenesToNormalized(listOf("1"), GeneFormat.ENTREZ))
        assertEquals(mapGenesToNormalized(listOf("a")), mapGenesToNormalized(listOf("a"), GeneFormat.SYMBOL))
        assertEquals(mapGenesToNormalized(listOf("NM_1")), mapGenesToNormalized(listOf("NM_1"), GeneFormat.REFSEQ))
        assertEquals(mapGenesToNormalized(listOf("ENSG1")), mapGenesToNormalized(listOf("ENSG1"), GeneFormat.ENSEMBL))
    }

    @Test
    fun testMapGenesToNormalizedEntrez() {
        assertEquals(mapOf("1" to "1"), mapGenesToNormalized(listOf("1")))
        assertEquals(mapOf("111" to "111"), mapGenesToNormalized(listOf("111")))
    }

    @Test
    fun testMapGenesToNormalizedSymbol() {
        assertEquals(mapOf("a" to "A"), mapGenesToNormalized(listOf("a")))
        assertEquals(mapOf("A" to "A"), mapGenesToNormalized(listOf("A")))
        assertEquals(mapOf("Aa" to "AA"), mapGenesToNormalized(listOf("Aa")))
        assertEquals(mapOf("aa" to "AA"), mapGenesToNormalized(listOf("aa")))
        assertEquals(mapOf("aa.1" to "AA.1"), mapGenesToNormalized(listOf("aa.1")))
        assertEquals(mapOf("aa-1" to "AA-1"), mapGenesToNormalized(listOf("aa-1")))

        assertEquals(mapOf("aa" to "AA", "Bb" to "BB", "CC" to "CC"),
                mapGenesToNormalized(listOf("aa", "Bb", "CC")))
    }

    @Test
    fun testMapGenesToNormalizedEnsemblRefseq() {
        assertEquals(mapOf("ENSG1" to "ENSG1"), mapGenesToNormalized(listOf("ENSG1")))
        assertEquals(mapOf("ENSG1.1" to "ENSG1"), mapGenesToNormalized(listOf("ENSG1.1")))

        assertEquals(mapOf("NM_1" to "NM_1"), mapGenesToNormalized(listOf("NM_1"), GeneFormat.REFSEQ))
        assertEquals(mapOf("nm_1" to "NM_1"), mapGenesToNormalized(listOf("nm_1"), GeneFormat.REFSEQ))
        assertEquals(mapOf("nm_1.2" to "NM_1"), mapGenesToNormalized(listOf("nm_1.2"), GeneFormat.REFSEQ))

        assertEquals(mapOf("aa.1" to "AA"), mapGenesToNormalized(listOf("aa.1"), GeneFormat.REFSEQ))

    }
}