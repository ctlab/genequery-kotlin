package gq.core.data

import org.junit.Test

import org.junit.Assert.*

class GQModuleKtTest {
    fun testWithStd(arr1: LongArray, arr2: LongArray) {
        val stdIntersection = arr1.intersect(arr2.toList()).toLongArray()
        assertArrayEquals(stdIntersection, arr1 intersectWithSorted arr2)
        assertEquals(stdIntersection.size, arr1.sizeOfIntersectionWithSorted(arr2))
    }

    @Test
    fun testIntersectionSymmetry() {
        val arr1 = longArrayOf(1, 3, 4, 5, 6)
        val arr2 = longArrayOf(2, 5, 6, 8, 10, 11)
        assertArrayEquals("Must be symmetric", arr1 intersectWithSorted arr2, arr2 intersectWithSorted arr1)
    }

    @Test
    fun testIntersectionSizeALongerB() {
        testWithStd(longArrayOf(1, 3, 4, 5, 6), longArrayOf(2, 5, 6, 8, 10, 11))
    }

    @Test
    fun testIntersectionSizeAShorterB() {
        testWithStd(longArrayOf(1, 3, 4, 5, 6), longArrayOf(2, 5))
    }

    @Test
    fun testIntersectionSizeNoInters1() {
        testWithStd(longArrayOf(1, 3, 4, 5, 6), longArrayOf(7))
    }

    @Test
    fun testIntersectionSizeNoInters2() {
        testWithStd(longArrayOf(1), longArrayOf(2, 3, 4))
    }

    @Test
    fun testIntersectionSizeEmptyQuery() {
        testWithStd(longArrayOf(1, 3, 4, 5, 6), longArrayOf())
    }

    @Test
    fun testParseFullModuleName() {
        val stringName = "GSE123_GPL123#12"
        val (datasetId, clusterId) = GQModule.parseFullModuleName(stringName)
        assertEquals("GSE123_GPL123", datasetId)
        assertEquals("12", clusterId)
    }

    @Test
    fun testBuildByFullName() {
        val stringName = "GSE123_GPL123#12"
        val module = GQModule.buildByFullName(stringName, Species.HUMAN, longArrayOf(1, 2, 3, 4))
        assertEquals("GSE123_GPL123", module.seriesName())
        assertEquals(Pair("GSE123_GPL123", "12"), module.fullName())
        assertEquals(stringName, module.joinFullName())
    }

    @Test(expected=IllegalArgumentException::class)
    fun testParseFullModuleNameBadNotEnoughParts1() {
        GQModule.parseFullModuleName("GSE123_GPL124")
    }


    @Test
    fun testParseFullModuleNameBadNumber() {
        val stringName = "GSE123_GPL124#asdf"
        val (datasetId, clusterId) = GQModule.parseFullModuleName(stringName)
        assertEquals("GSE123_GPL124", datasetId)
        assertEquals("asdf", clusterId)
    }
}
