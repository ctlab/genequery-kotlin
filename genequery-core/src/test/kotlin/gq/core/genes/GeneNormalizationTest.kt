package gq.core.genes

import org.junit.Assert.assertEquals
import org.junit.Test

class GeneNormalizationTest {

    @Test
    fun testMapGenesToNormalizedNoSpecifiedFormat() {
        assertEquals(GeneFormat.ENTREZ.mapToNormalized(listOf("1")), GeneFormat.ENTREZ.mapToNormalized(listOf("1")))
        assertEquals(GeneFormat.SYMBOL.mapToNormalized(listOf("a")), GeneFormat.SYMBOL.mapToNormalized(listOf("a")))
        assertEquals(GeneFormat.REFSEQ.mapToNormalized(listOf("NM_1")), GeneFormat.REFSEQ.mapToNormalized(listOf("NM_1")))
        assertEquals(GeneFormat.ENSEMBL.mapToNormalized(listOf("ENSG1")), GeneFormat.ENSEMBL.mapToNormalized(listOf("ENSG1")))
    }

    @Test
    fun testMapGenesToNormalizedEntrez() {
        assertEquals(mapOf("1" to "1"), GeneFormat.ENTREZ.mapToNormalized(listOf("1")))
        assertEquals(mapOf("111" to "111"), GeneFormat.ENTREZ.mapToNormalized(listOf("111")))
    }

    @Test
    fun testMapGenesToNormalizedSymbol() {
        assertEquals(mapOf("a" to "A"), GeneFormat.SYMBOL.mapToNormalized(listOf("a")))
        assertEquals(mapOf("A" to "A"), GeneFormat.SYMBOL.mapToNormalized(listOf("A")))
        assertEquals(mapOf("Aa" to "AA"), GeneFormat.SYMBOL.mapToNormalized(listOf("Aa")))
        assertEquals(mapOf("aa" to "AA"), GeneFormat.SYMBOL.mapToNormalized(listOf("aa")))
        assertEquals(mapOf("aa.1" to "AA.1"), GeneFormat.SYMBOL.mapToNormalized(listOf("aa.1")))
        assertEquals(mapOf("aa-1" to "AA-1"), GeneFormat.SYMBOL.mapToNormalized(listOf("aa-1")))

        assertEquals(mapOf("aa" to "AA", "Bb" to "BB", "CC" to "CC"),
                GeneFormat.SYMBOL.mapToNormalized(listOf("aa", "Bb", "CC")))
    }

    @Test
    fun testMapGenesToNormalizedEnsemblRefseq() {
        assertEquals(mapOf("ENSG1" to "ENSG1"), GeneFormat.ENSEMBL.mapToNormalized(listOf("ENSG1")))
        assertEquals(mapOf("ENSG1.1" to "ENSG1"), GeneFormat.ENSEMBL.mapToNormalized(listOf("ENSG1.1")))

        assertEquals(mapOf("NM_1" to "NM_1"), GeneFormat.REFSEQ.mapToNormalized(listOf("NM_1")))
        assertEquals(mapOf("nm_1" to "NM_1"), GeneFormat.REFSEQ.mapToNormalized(listOf("nm_1")))
        assertEquals(mapOf("nm_1.2" to "NM_1"), GeneFormat.REFSEQ.mapToNormalized(listOf("nm_1.2")))

        assertEquals(mapOf("aa.1" to "AA"), GeneFormat.REFSEQ.mapToNormalized(listOf("aa.1")))

    }
}