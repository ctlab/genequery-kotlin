package gq.core.genes

import gq.core.data.Species
import org.junit.Test

import org.junit.Assert.*
import java.io.File
import java.nio.file.Paths

class ToEntrezNormalizeConverterKtTest {
    companion object : ConverterCompanionTestBase() {
        val converter = createToEntrezConverter()
        fun mapOfToLong(vararg pairs: Pair<String, Int?>) = mapOf(*pairs).mapValues { it.value?.toLong() ?: null }
    }

    @Test
    fun testFromEntrez() {
        assertEquals(
                mapOfToLong("1" to 1, "2" to 2, "300" to 300),
                converter.normalizeAndConvert(Species.HUMAN, listOf("1", "2", "300"), GeneFormat.ENTREZ))
        assertEquals(
                converter.normalizeAndConvert(Species.HUMAN, listOf("1", "2", "300")),
                converter.normalizeAndConvert(Species.HUMAN, listOf("1", "2", "300"), GeneFormat.ENTREZ))
    }

    @Test
    fun testFromSymbol() {
        assertEquals(
                mapOfToLong("A1BG" to 1, "A2m" to 2, "nat1" to 9, "not-a-gene" to null, "Duplicate" to 26, "DUPLICATE" to 26),
                converter.normalizeAndConvert(Species.HUMAN, listOf("A1BG", "A2m", "nat1", "not-a-gene", "Duplicate", "DUPLICATE")))

        assertEquals(
                mapOfToLong("A" to null, "a" to null),
                converter.normalizeAndConvert(Species.HUMAN, listOf("A", "a")))
    }

    @Test
    fun testFromEnsembl() {
        assertEquals(
                mapOfToLong("ENSG00000121410" to 1, "Ensg00000175899" to 2, "eNsG00000256069.2" to 3, "ENSG00000" to null),
                converter.normalizeAndConvert(
                        Species.HUMAN,
                        listOf("ENSG00000121410", "Ensg00000175899", "eNsG00000256069.2", "ENSG00000"),
                        GeneFormat.ENSEMBL))
    }

    @Test
    fun testFromRefseq() {
        assertEquals(
                mapOfToLong("NM_130786" to 1, "Nm_000014" to 2, "xm_006719056" to 2, "xR_011544358.1" to 10, "fail_0" to null),
                converter.normalizeAndConvert(
                        Species.HUMAN,
                        listOf("NM_130786", "Nm_000014", "xm_006719056", "xR_011544358.1", "fail_0"),
                        GeneFormat.REFSEQ))
    }

    @Test(expected = IllegalArgumentException::class)
    fun testFromEnsemblFail() {
        converter.normalizeAndConvert(Species.HUMAN, listOf("ENSG00000121410", "Ensg00000175899"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun testFromMixedFail() {
        converter.normalizeAndConvert(Species.HUMAN, listOf("A1BG", "1"))
    }

    @Test
    fun testFromMixed2() {
        assertEquals(
                mapOfToLong("A1BG" to 1, "1" to null),
                converter.normalizeAndConvert(Species.HUMAN, listOf("A1BG", "1"), GeneFormat.SYMBOL))
    }
}