package gq.core.search

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

class SearcherKtTest {

    companion object {
        var dataset = GQModuleCollection(readModulesFromFiles(
                Species.MOUSE to Thread.currentThread().contextClassLoader.getResource("search/mm.modules.gmt").path))
    }

    fun readEntrezIds(localPath: String) = String(Files.readAllBytes(
            Paths.get(Thread.currentThread().contextClassLoader.getResource("search/$localPath").toURI())))
            .trim()
            .split(",", " ", ", ")
            .map { it.toLong() }

    @Test
    fun testFindBonferroniSignificantHypoxia() {
        val query = GQRequest(Species.MOUSE, readEntrezIds("hypoxia.txt"))
        val result = findBonferroniSignificant(dataset, query)
        assertEquals(4, result.size)
        assertEquals(listOf(3296, 3318, 3196, 44762), result.map { it.gse })
        assertEquals(listOf(46, 46, 48, 14), result.map { it.intersectionSize })
        assertEquals(listOf(10, 7, 7, 14), result.map { it.moduleNumber })
        assertDoubleEquals(-41.077, result.first().logPvalue, 1e-3)
        assertDoubleEquals(-7.0, result.last().logPvalue, 1e-3)
    }

    @Test
    fun testFindBonferroniSignificantInfinity() {
        val query = GQRequest(Species.MOUSE, readEntrezIds("large.txt"))
        val result = findBonferroniSignificant(dataset, query)
        assertDoubleEquals(0.0, result.first().pvalue, 1e-3)
        assertEquals(Double.NEGATIVE_INFINITY, result.first().logPvalue)
        assertTrue(result.first().intersectionSize == result.first().moduleSize)
    }

    @Test
    fun testFindBonferroniSignificantNullModule() {
        val query = GQRequest(Species.MOUSE, readEntrezIds("null-module.txt"))
        val result = findBonferroniSignificant(dataset, query)
        assertFalse(result.any { it.moduleNumber == 0 })
    }

    @Test
    fun testFindBonferroniSignificantEmptyQuery() {
        val result = findBonferroniSignificant(dataset, GQRequest(Species.MOUSE, emptyList()))
        assertTrue(result.isEmpty())
    }

    @Test
    fun testFindBonferroniSignificantNoSuchSpecies() {
        val query = GQRequest(Species.HUMAN, readEntrezIds("hypoxia.txt"))
        val result = findBonferroniSignificant(dataset, query)
        assertTrue(result.isEmpty())
    }

    @Test
    fun testFindBonferroniSignificantNoSuchGenes() {
        val query = GQRequest(Species.MOUSE, LongRange(1, 500).toList())
        val result = findBonferroniSignificant(dataset, query)
        assertTrue(result.isEmpty())
    }

    @Test
    fun testNoisedVsOriginalQuery() {
        val query = GQRequest(Species.MOUSE, readEntrezIds("hypoxia.txt"))
        val queryWithNoise = GQRequest(Species.MOUSE, readEntrezIds("hypoxia-with-noise.txt"))
        val result = findBonferroniSignificant(dataset, query)
        val resultWithNoise = findBonferroniSignificant(dataset, queryWithNoise)
        assertEquals(result.size, resultWithNoise.size)
        assertEquals(result.map { it.gse }, resultWithNoise.map { it.gse })
        assertEquals(result.map { it.intersectionSize }, resultWithNoise.map { it.intersectionSize })
        assertEquals(result.map { it.moduleNumber }, resultWithNoise.map { it.moduleNumber })
        assertDoubleEquals(result.first().logPvalue, resultWithNoise.first().logPvalue, 1e-3)
        assertDoubleEquals(result.last().logPvalue, resultWithNoise.last().logPvalue, 1e-3)
    }
}