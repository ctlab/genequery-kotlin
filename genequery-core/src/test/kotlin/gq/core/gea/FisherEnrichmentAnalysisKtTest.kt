package gq.core.gea

import gq.core.data.GQModuleCollection
import gq.core.data.Species
import gq.core.data.readModulesFromFiles
import org.junit.Test
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.junit.Assert.assertEquals as assertDoubleEquals

class FisherEnrichmentAnalysisKtTest {

    companion object {
        var dataset = GQModuleCollection(readModulesFromFiles(
                Species.MOUSE to Thread.currentThread().contextClassLoader.getResource("gea/mm.modules.gmt").path))
    }

    fun readEntrezIds(localPath: String) = String(Files.readAllBytes(
            Paths.get(Thread.currentThread().contextClassLoader.getResource("gea/$localPath").toURI())))
            .trim()
            .split(",", " ", ", ")
            .map { it.toLong() }

    @Test
    fun testFindBonferroniSignificantHypoxia() {
        val query = SpecifiedEntrezGenes(Species.MOUSE, readEntrezIds("hypoxia.txt"))
        val result = findBonferroniSignificant(dataset, query)
        assertEquals(4, result.size)
        assertEquals(listOf("GSE3296_GPL1261", "GSE3318_GPL1261", "GSE3196_GPL1261", "GSE44762_GPL6885"), result.map { it.datasetId })
        assertEquals(listOf(46, 46, 48, 14), result.map { it.intersectionSize })
        assertEquals(listOf("10", "7", "7", "14"), result.map { it.clusterId })
        assertDoubleEquals(-41.077, result.first().logPvalue, 1e-3)
        assertDoubleEquals(-7.0, result.last().logPvalue, 1e-3)
    }

    @Test
    fun testFindBonferroniSignificantMinLogPvalue() {
        val query = SpecifiedEntrezGenes(Species.MOUSE, readEntrezIds("large.txt"))
        val result = findBonferroniSignificant(dataset, query)
        assertDoubleEquals(0.0, result.first().pvalue, 1e-3)
        assertEquals(EnrichmentResultItem.MIN_LOG_P_VALUE, result.first().logPvalue)
        assertTrue(result.first().intersectionSize == result.first().moduleSize)
    }

    @Test
    fun testFindBonferroniSignificantEmptyQuery() {
        val result = findBonferroniSignificant(dataset, SpecifiedEntrezGenes(Species.MOUSE, emptyList()))
        assertTrue(result.isEmpty())
    }

    @Test
    fun testFindBonferroniSignificantNoSuchSpecies() {
        val query = SpecifiedEntrezGenes(Species.HUMAN, readEntrezIds("hypoxia.txt"))
        val result = findBonferroniSignificant(dataset, query)
        assertTrue(result.isEmpty())
    }

    @Test
    fun testFindBonferroniSignificantNoSuchGenes() {
        val query = SpecifiedEntrezGenes(Species.MOUSE, LongRange(1, 500).toList())
        val result = findBonferroniSignificant(dataset, query)
        assertTrue(result.isEmpty())
    }

    @Test
    fun testNoisedVsOriginalQuery() {
        val query = SpecifiedEntrezGenes(Species.MOUSE, readEntrezIds("hypoxia.txt"))
        val queryWithNoise = SpecifiedEntrezGenes(Species.MOUSE, readEntrezIds("hypoxia-with-noise.txt"))
        val result = findBonferroniSignificant(dataset, query)
        val resultWithNoise = findBonferroniSignificant(dataset, queryWithNoise)
        assertEquals(result.size, resultWithNoise.size)
        assertEquals(result.map { it.datasetId }, resultWithNoise.map { it.datasetId })
        assertEquals(result.map { it.intersectionSize }, resultWithNoise.map { it.intersectionSize })
        assertEquals(result.map { it.clusterId }, resultWithNoise.map { it.clusterId })
        assertDoubleEquals(result.first().logPvalue, resultWithNoise.first().logPvalue, 1e-3)
        assertDoubleEquals(result.last().logPvalue, resultWithNoise.last().logPvalue, 1e-3)
    }
}