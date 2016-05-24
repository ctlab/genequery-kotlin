package gq.core.genes

import org.junit.Assert.*
import org.junit.Test

class FormatTest {
    @Test(expected = IllegalArgumentException::class)
    fun testGuessGeneFormatEmptyString() {
        guessGeneFormat("")
    }

    @Test
    fun testGuessGeneFormat() {
        assertEquals(GeneFormat.SYMBOL, guessGeneFormat("ENSA"))
        assertEquals(GeneFormat.SYMBOL, guessGeneFormat("Ensa"))
        assertEquals(GeneFormat.SYMBOL, guessGeneFormat("ENSAP3"))

        assertEquals(GeneFormat.ENSEMBL, guessGeneFormat("ENSG00000120907"))
        assertEquals(GeneFormat.ENSEMBL, guessGeneFormat("ENSG00000120907.1"))
        assertEquals(GeneFormat.ENSEMBL, guessGeneFormat("ENSMUSG00000036899"))
        assertEquals(GeneFormat.ENSEMBL, guessGeneFormat("ENSMUSG00000036899.2"))
        assertEquals(GeneFormat.ENSEMBL, guessGeneFormat("ENSRNOG00000008187"))
        assertEquals(GeneFormat.ENSEMBL, guessGeneFormat("ENSRNOG00000008187.3"))
        assertEquals(GeneFormat.SYMBOL, guessGeneFormat("ENSRNOG00000008187.3.3"))
        assertEquals(GeneFormat.SYMBOL, guessGeneFormat("ENSRNOG"))
        assertEquals(GeneFormat.SYMBOL, guessGeneFormat("ENSRNOG00DD"))
        assertEquals(GeneFormat.SYMBOL, guessGeneFormat("ENSRNOG00DD.2"))
        assertEquals(GeneFormat.SYMBOL, guessGeneFormat("ENSRNOG1e10"))
        assertEquals(GeneFormat.SYMBOL, guessGeneFormat("ENSRNOG0.1e-10"))

        assertEquals(GeneFormat.ENTREZ, guessGeneFormat("12345"))
        assertEquals(GeneFormat.SYMBOL, guessGeneFormat("s12345"))
        assertEquals(GeneFormat.SYMBOL, guessGeneFormat("1e10"))
        assertEquals(GeneFormat.SYMBOL, guessGeneFormat("1e-10"))
        assertEquals(GeneFormat.SYMBOL, guessGeneFormat("-1e10"))
        assertEquals(GeneFormat.SYMBOL, guessGeneFormat("-12345"))
        assertEquals(GeneFormat.SYMBOL, guessGeneFormat("1.2345"))
        assertEquals(GeneFormat.SYMBOL, guessGeneFormat("1,2345"))

        assertEquals(GeneFormat.REFSEQ, guessGeneFormat("NM_1234"))
        assertEquals(GeneFormat.REFSEQ, guessGeneFormat("NR_1234"))
        assertEquals(GeneFormat.REFSEQ, guessGeneFormat("XM_1234"))
        assertEquals(GeneFormat.REFSEQ, guessGeneFormat("XR_1234"))
        assertEquals(GeneFormat.REFSEQ, guessGeneFormat("XR_1234.1"))
        assertEquals(GeneFormat.SYMBOL, guessGeneFormat("XR__1234"))
        assertEquals(GeneFormat.SYMBOL, guessGeneFormat("XR1234"))
        assertEquals(GeneFormat.SYMBOL, guessGeneFormat("XR_s1234"))
        assertEquals(GeneFormat.SYMBOL, guessGeneFormat("XR_.1234"))
    }
}