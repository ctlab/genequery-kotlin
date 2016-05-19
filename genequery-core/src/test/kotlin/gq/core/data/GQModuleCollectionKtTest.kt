package gq.core.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GQModuleCollectionKtTest {

    companion object {
        var dataset = GQModuleCollection(readModulesFromFiles(
                Species.HUMAN to Thread.currentThread().contextClassLoader.getResource("collection/hs.modules.gmt").path,
                Species.MOUSE to Thread.currentThread().contextClassLoader.getResource("collection/mm.modules.gmt").path))
    }

    @Test
    fun testGetFullNameToGQModule() {
        assertEquals(23, dataset.fullNameToGQModule.size)
        assertEquals(13, dataset.fullNameToGQModule[Triple(1000, 96, 0)]!!.size)
        assertTrue(Triple(1000, 96, 4) in dataset.fullNameToGQModule)
    }

    @Test
    fun testGetSpeciesToModules() {
        assertTrue(Species.MOUSE in dataset.speciesToModules)
        assertTrue(Species.HUMAN in dataset.speciesToModules)
        assertEquals(2, dataset.speciesToModules.size)
        assertEquals(11, dataset.speciesToModules[Species.HUMAN]!!.size)
        assertEquals(12, dataset.speciesToModules[Species.MOUSE]!!.size)
    }

    @Test
    fun testGetGseGplToModules() {
        assertEquals(4, dataset.seriesToModules.size)
        assertTrue(Pair(1000, 96) in dataset.seriesToModules)
        assertTrue(Pair(10001, 1261) in dataset.seriesToModules)
        assertEquals(7, dataset.seriesToModules[Pair(10001, 1261)]!!.size)
        assertTrue(
            "modules are not sorted within gse",
            dataset.seriesToModules[Pair(10000, 1261)]!!
                    .mapIndexed { i, gqModule ->  i == gqModule.number}
                    .all { it })
        assertTrue(
                "modules are not sorted within gse",
                dataset.seriesToModules[Pair(1001, 96)]!!
                        .mapIndexed { i, gqModule ->  i == gqModule.number}
                        .all { it })
    }

    @Test
    fun testGetSpeciesToGseGpl() {
        assertEquals(listOf(Pair(1000, 96), Pair(1001, 96)), dataset.speciesToGseGpl[Species.HUMAN])
    }
}