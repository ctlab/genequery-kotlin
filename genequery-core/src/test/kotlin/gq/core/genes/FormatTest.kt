package gq.core.genes

import org.junit.Assert.*
import org.junit.Test

class FormatTest {
    @Test
    fun testGuessGeneFormat() {
        assertEquals(GeneFormat.SYMBOL, "ENSA".guessGeneFormat())
        assertEquals(GeneFormat.SYMBOL, "ENSAP3".guessGeneFormat())

        assertEquals(GeneFormat.ENSEMBL, "ENSG00000120907".guessGeneFormat())
        assertEquals(GeneFormat.ENSEMBL, "ENSG00000120907.1".guessGeneFormat())
        assertEquals(GeneFormat.ENSEMBL, "ENSMUSG00000036899".guessGeneFormat())
        assertEquals(GeneFormat.ENSEMBL, "ENSMUSG00000036899.2".guessGeneFormat())
        assertEquals(GeneFormat.ENSEMBL, "ENSRNOG00000008187".guessGeneFormat())
        assertEquals(GeneFormat.ENSEMBL, "ENSRNOG00000008187.3".guessGeneFormat())
        assertEquals(GeneFormat.SYMBOL, "ENSRNOG".guessGeneFormat())
        assertEquals(GeneFormat.SYMBOL, "ENSRNOG00DD".guessGeneFormat())
        assertEquals(GeneFormat.SYMBOL, "ENSRNOG00DD.2".guessGeneFormat())
        assertEquals(GeneFormat.SYMBOL, "ENSRNOG1e10".guessGeneFormat())
        assertEquals(GeneFormat.SYMBOL, "ENSRNOG0.1e-10".guessGeneFormat())

        assertEquals(GeneFormat.ENTREZ, "12345".guessGeneFormat())
        assertEquals(GeneFormat.SYMBOL, "s12345".guessGeneFormat())
        assertEquals(GeneFormat.SYMBOL, "1e10".guessGeneFormat())
        assertEquals(GeneFormat.SYMBOL, "1e-10".guessGeneFormat())
        assertEquals(GeneFormat.SYMBOL, "-1e10".guessGeneFormat())
        assertEquals(GeneFormat.SYMBOL, "-12345".guessGeneFormat())
        assertEquals(GeneFormat.SYMBOL, "1.2345".guessGeneFormat())
        assertEquals(GeneFormat.SYMBOL, "1,2345".guessGeneFormat())

        assertEquals(GeneFormat.REFSEQ, "NM_1234".guessGeneFormat())
        assertEquals(GeneFormat.REFSEQ, "NR_1234".guessGeneFormat())
        assertEquals(GeneFormat.REFSEQ, "XM_1234".guessGeneFormat())
        assertEquals(GeneFormat.REFSEQ, "XR_1234".guessGeneFormat())
        assertEquals(GeneFormat.REFSEQ, "XR_1234.1".guessGeneFormat())
        assertEquals(GeneFormat.SYMBOL, "XR__1234".guessGeneFormat())
        assertEquals(GeneFormat.SYMBOL, "XR1234".guessGeneFormat())
        assertEquals(GeneFormat.SYMBOL, "XR_s1234".guessGeneFormat())
        assertEquals(GeneFormat.SYMBOL, "XR_.1234".guessGeneFormat())
    }
}