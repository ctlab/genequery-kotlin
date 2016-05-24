package gq.core.genes

import org.junit.Assert.assertEquals
import org.junit.Test

class DetectGenesFormatTest {

    @Test(expected = IllegalArgumentException::class)
    fun testDetectGenesFormatEmptyList() {
        detectGenesFormat(emptyList())
    }

    @Test(expected = IllegalArgumentException::class)
    fun testDetectGenesFormatAmbiguous1() {
        detectGenesFormat(listOf("111", "abc"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun testDetectGenesFormatAmbiguous2() {
        detectGenesFormat(listOf("abc", "111"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun testDetectGenesFormatAmbiguous3() {
        detectGenesFormat(listOf("ENSG00000198691", "111"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun testDetectGenesFormatAmbiguous4() {
        detectGenesFormat(listOf("111", "ENSG00000198691"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun testDetectGenesFormatAmbiguous5() {
        detectGenesFormat(listOf("Abc", "ENSG00000198691"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun testDetectGenesFormatAmbiguous6() {
        detectGenesFormat(listOf("ENSG00000198691", "abc"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun testDetectGenesFormatAmbiguous7() {
        detectGenesFormat(listOf("NM_001160175", "111"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun testDetectGenesFormatAmbiguous8() {
        detectGenesFormat(listOf("NM_001160175", "111"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun testDetectGenesFormatAmbiguous9() {
        detectGenesFormat(listOf("NM_001160175", "NM_001160176.1", "NM_001160177.22", "111"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun testDetectGenesFormatAmbiguous10() {
        detectGenesFormat(listOf("aaa", "NM_001160175", "NM_001160176.1", "NM_001160177.22"))
    }

    @Test
    fun testDetectGenesFormatEntrez() {
        assertEquals(GeneFormat.ENTREZ,
                detectGenesFormat(listOf("1", "2", "333333333323333")))
    }

    @Test
    fun testDetectGenesFormatSymbol() {
        assertEquals(GeneFormat.SYMBOL,
                detectGenesFormat(listOf("a", "A", "Abc", "ABC")))
    }

    @Test
    fun testDetectGenesFormatRefseq() {
        assertEquals(GeneFormat.REFSEQ,
                detectGenesFormat(listOf("NM_001160175", "NM_001160176.1", "NM_001160177.22")))
    }

    @Test
    fun testDetectGenesFormatEnsembl() {
        assertEquals(GeneFormat.ENSEMBL,
                detectGenesFormat(listOf("ENSG00000198691", "ENSG00000198691.1", "ENSG00000198691.22")))
    }

}
