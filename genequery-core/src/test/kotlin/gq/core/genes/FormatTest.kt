package gq.core.genes

import org.junit.Assert.*
import org.junit.Test

class FormatTest {
    @Test(expected = IllegalArgumentException::class)
    fun testGeneFormatGuessEmptyString() {
        GeneFormat.guess("")
    }

    @Test
    fun testGeneFormat() {
        assertEquals(GeneFormat.SYMBOL, GeneFormat.guess("ENSA"))
        assertEquals(GeneFormat.SYMBOL, GeneFormat.guess("Ensa"))
        assertEquals(GeneFormat.SYMBOL, GeneFormat.guess("ENSAP3"))

        assertEquals(GeneFormat.ENSEMBL, GeneFormat.guess("ENSG00000120907"))
        assertEquals(GeneFormat.ENSEMBL, GeneFormat.guess("ENSG00000120907.1"))
        assertEquals(GeneFormat.ENSEMBL, GeneFormat.guess("ENSMUSG00000036899"))
        assertEquals(GeneFormat.ENSEMBL, GeneFormat.guess("ENSMUSG00000036899.2"))
        assertEquals(GeneFormat.ENSEMBL, GeneFormat.guess("ENSRNOG00000008187"))
        assertEquals(GeneFormat.ENSEMBL, GeneFormat.guess("ENSRNOG00000008187.3"))
        assertEquals(GeneFormat.SYMBOL, GeneFormat.guess("ENSRNOG00000008187.3.3"))
        assertEquals(GeneFormat.SYMBOL, GeneFormat.guess("ENSRNOG"))
        assertEquals(GeneFormat.SYMBOL, GeneFormat.guess("ENSRNOG00DD"))
        assertEquals(GeneFormat.SYMBOL, GeneFormat.guess("ENSRNOG00DD.2"))
        assertEquals(GeneFormat.SYMBOL, GeneFormat.guess("ENSRNOG1e10"))
        assertEquals(GeneFormat.SYMBOL, GeneFormat.guess("ENSRNOG0.1e-10"))

        assertEquals(GeneFormat.ENTREZ, GeneFormat.guess("12345"))
        assertEquals(GeneFormat.SYMBOL, GeneFormat.guess("s12345"))
        assertEquals(GeneFormat.SYMBOL, GeneFormat.guess("1e10"))
        assertEquals(GeneFormat.SYMBOL, GeneFormat.guess("1e-10"))
        assertEquals(GeneFormat.SYMBOL, GeneFormat.guess("-1e10"))
        assertEquals(GeneFormat.SYMBOL, GeneFormat.guess("-12345"))
        assertEquals(GeneFormat.SYMBOL, GeneFormat.guess("1.2345"))
        assertEquals(GeneFormat.SYMBOL, GeneFormat.guess("1,2345"))

        assertEquals(GeneFormat.REFSEQ, GeneFormat.guess("NM_1234"))
        assertEquals(GeneFormat.REFSEQ, GeneFormat.guess("NR_1234"))
        assertEquals(GeneFormat.REFSEQ, GeneFormat.guess("XM_1234"))
        assertEquals(GeneFormat.REFSEQ, GeneFormat.guess("XR_1234"))
        assertEquals(GeneFormat.REFSEQ, GeneFormat.guess("XR_1234.1"))
        assertEquals(GeneFormat.SYMBOL, GeneFormat.guess("XR__1234"))
        assertEquals(GeneFormat.SYMBOL, GeneFormat.guess("XR1234"))
        assertEquals(GeneFormat.SYMBOL, GeneFormat.guess("XR_s1234"))
        assertEquals(GeneFormat.SYMBOL, GeneFormat.guess("XR_.1234"))
    }
}