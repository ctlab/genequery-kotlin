package gq.core.data

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

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
        val (gse, gpl, number) = GQModule.parseFullModuleName(stringName)
        assertEquals(123, gse)
        assertEquals(123, gpl)
        assertEquals(12, number)
    }

    @Test
    fun testBuildByFullName() {
        val stringName = "GSE123_GPL123#12"
        val module = GQModule.buildByFullName(stringName, Species.HUMAN, longArrayOf(1, 2, 3, 4))
        assertEquals(Pair(123, 123), module.seriesName())
        assertEquals(Triple(123, 123, 12), module.fullName())
        assertEquals(stringName, module.joinFullName())
    }

    @Test(expected = IllegalArgumentException::class)
    fun testParseFullModuleNameBadNotEnoughParts1() {
        GQModule.parseFullModuleName("GSE123_GPL124")
    }

    @Test(expected = IllegalArgumentException::class)
    fun testParseFullModuleNameBadNotEnoughParts2() {
        GQModule.parseFullModuleName("GSE123#3")
    }

    @Test(expected = IllegalArgumentException::class)
    fun testParseFullModuleNameBadNumber() {
        GQModule.parseFullModuleName("GSE123_GPL124#asdf")
    }
}
