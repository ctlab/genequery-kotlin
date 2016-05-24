package gq.core.genes

import org.junit.Assert.assertEquals
import org.junit.Test

class DetectGenesFormatTest {

    @Test(expected = IllegalArgumentException::class)
    fun testDetectGenesFormatEmptyList() {
        detectGeneSetFormat(emptyList())
    }

    @Test(expected = IllegalArgumentException::class)
    fun testDetectGenesFormatAmbiguous1() {
        detectGeneSetFormat(listOf("111", "abc"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun testDetectGenesFormatAmbiguous2() {
        detectGeneSetFormat(listOf("abc", "111"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun testDetectGenesFormatAmbiguous3() {
        detectGeneSetFormat(listOf("ENSG00000198691", "111"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun testDetectGenesFormatAmbiguous4() {
        detectGeneSetFormat(listOf("111", "ENSG00000198691"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun testDetectGenesFormatAmbiguous5() {
        detectGeneSetFormat(listOf("Abc", "ENSG00000198691"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun testDetectGenesFormatAmbiguous6() {
        detectGeneSetFormat(listOf("ENSG00000198691", "abc"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun testDetectGenesFormatAmbiguous7() {
        detectGeneSetFormat(listOf("NM_001160175", "111"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun testDetectGenesFormatAmbiguous8() {
        detectGeneSetFormat(listOf("NM_001160175", "111"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun testDetectGenesFormatAmbiguous9() {
        detectGeneSetFormat(listOf("NM_001160175", "NM_001160176.1", "NM_001160177.22", "111"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun testDetectGenesFormatAmbiguous10() {
        detectGeneSetFormat(listOf("aaa", "NM_001160175", "NM_001160176.1", "NM_001160177.22"))
    }

    @Test
    fun testDetectGenesFormatEntrez() {
        assertEquals(GeneFormat.ENTREZ,
                detectGeneSetFormat(listOf("1", "2", "333333333323333")))
    }

    @Test
    fun testDetectGenesFormatSymbol() {
        assertEquals(GeneFormat.SYMBOL,
                detectGeneSetFormat(listOf("a", "A", "Abc", "ABC")))
    }

    @Test
    fun testDetectGenesFormatRefseq() {
        assertEquals(GeneFormat.REFSEQ,
                detectGeneSetFormat(listOf("NM_001160175", "NM_001160176.1", "NM_001160177.22")))
    }

    @Test
    fun testDetectGenesFormatEnsembl() {
        assertEquals(GeneFormat.ENSEMBL,
                detectGeneSetFormat(listOf("ENSG00000198691", "ENSG00000198691.1", "ENSG00000198691.22")))
    }

}
