package gq.core.genes

import org.junit.Assert.assertEquals
import org.junit.Test

class DetectGenesFormatTest {

    @Test(expected = IllegalArgumentException::class)
    fun testDetectGenesFormatEmptyList() {
        GeneFormat.guess(emptyList())
    }

    @Test(expected = IllegalArgumentException::class)
    fun testDetectGenesFormatAmbiguous1() {
        GeneFormat.guess(listOf("111", "abc"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun testDetectGenesFormatAmbiguous2() {
        GeneFormat.guess(listOf("abc", "111"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun testDetectGenesFormatAmbiguous3() {
        GeneFormat.guess(listOf("ENSG00000198691", "111"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun testDetectGenesFormatAmbiguous4() {
        GeneFormat.guess(listOf("111", "ENSG00000198691"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun testDetectGenesFormatAmbiguous5() {
        GeneFormat.guess(listOf("Abc", "ENSG00000198691"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun testDetectGenesFormatAmbiguous6() {
        GeneFormat.guess(listOf("ENSG00000198691", "abc"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun testDetectGenesFormatAmbiguous7() {
        GeneFormat.guess(listOf("NM_001160175", "111"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun testDetectGenesFormatAmbiguous8() {
        GeneFormat.guess(listOf("NM_001160175", "111"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun testDetectGenesFormatAmbiguous9() {
        GeneFormat.guess(listOf("NM_001160175", "NM_001160176.1", "NM_001160177.22", "111"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun testDetectGenesFormatAmbiguous10() {
        GeneFormat.guess(listOf("aaa", "NM_001160175", "NM_001160176.1", "NM_001160177.22"))
    }

    @Test
    fun testDetectGenesFormatEntrez() {
        assertEquals(GeneFormat.ENTREZ,
                GeneFormat.guess(listOf("1", "2", "333333333323333")))
    }

    @Test
    fun testDetectGenesFormatSymbol() {
        assertEquals(GeneFormat.SYMBOL,
                GeneFormat.guess(listOf("a", "A", "Abc", "ABC")))
    }

    @Test
    fun testDetectGenesFormatRefseq() {
        assertEquals(GeneFormat.REFSEQ,
                GeneFormat.guess(listOf("NM_001160175", "NM_001160176.1", "NM_001160177.22")))
    }

    @Test
    fun testDetectGenesFormatEnsembl() {
        assertEquals(GeneFormat.ENSEMBL,
                GeneFormat.guess(listOf("ENSG00000198691", "ENSG00000198691.1", "ENSG00000198691.22")))
    }

}
