package gq.core.gea

import gq.core.data.*
import org.junit.Assert
import org.junit.Test
import java.util.*
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.junit.Assert.assertEquals as assertDoubleEquals

class NetworkClusteringKtTest {

    @Test
    fun testGroupEnrichedItemsByClusters() {
        val enrichedItems = (10..110).map { EnrichmentResultItem(
                it / 10, it / 10, it % 10, .0, .0, .0, .0, 0, 0
        ) }.associate { Pair(GQModule.joinFullName(it.gse, it.gpl, it.moduleNumber), it) }
        val clusterCollection = GQNetworkClusterCollection(listOf(
                GQNetworkCluster(1, (10..29).map { GQModule.joinFullName(it / 10, it / 10, it % 10) }.toSet(), "ann_1"),
                GQNetworkCluster(2, (30..49).map { GQModule.joinFullName(it / 10, it / 10, it % 10) }.toSet(), "ann_2"),
                GQNetworkCluster(3, (50..69).map { GQModule.joinFullName(it / 10, it / 10, it % 10) }.toSet(), "ann_3"),
                GQNetworkCluster(4, (1000..1010).map { GQModule.joinFullName(it / 10, it / 10, it % 10) }.toSet(), "ann_4")
        ))

        val idToGroup = groupEnrichedItemsByClusters(
                enrichedItems.values.toList(),
                clusterCollection,
                MinLogAdjPvalueScoreEvaluationStrategy())

        assertTrue(GQNetworkCluster.ZERO_CLUSTER_ID in idToGroup)
        assertTrue(1 in idToGroup)
        assertTrue(2 in idToGroup)
        assertTrue(3 in idToGroup)
        assertFalse(4 in idToGroup)

        Assert.assertEquals(idToGroup[1]!!.moduleNames.size, 20)
        Assert.assertEquals(idToGroup[2]!!.moduleNames.size, 20)
        Assert.assertEquals(idToGroup[3]!!.moduleNames.size, 20)
        Assert.assertEquals(idToGroup[GQNetworkCluster.ZERO_CLUSTER_ID]!!.moduleNames.size, 41)

        assertTrue("GSE1_GPL1#1" in idToGroup[1]!!.moduleNames)
        assertTrue("GSE3_GPL3#5" in idToGroup[2]!!.moduleNames)
        assertTrue("GSE7_GPL7#1" in idToGroup[GQNetworkCluster.ZERO_CLUSTER_ID]!!.moduleNames)
    }

    @Test
    fun testMinAdjLogPvalue() {
        val random = Random(100000007)
        val enrichedItems = (10..39).map { EnrichmentResultItem(
                it / 10, it / 10, it % 10,
                1e-10, -10.0, 1e-20, -100 * random.nextDouble(), 10, 10
        ) }.associate { Pair(GQModule.joinFullName(it.gse, it.gpl, it.moduleNumber), it) }
        val clusterCollection = GQNetworkClusterCollection(listOf(
                GQNetworkCluster(1, (10..29).map { GQModule.joinFullName(it / 10, it / 10, it % 10) }.toSet(), "ann_1"),
                GQNetworkCluster(4, (1000..1010).map { GQModule.joinFullName(it / 10, it / 10, it % 10) }.toSet(), "ann_4")
        ))

        val idToGroup = groupEnrichedItemsByClusters(
                enrichedItems.values.toList(),
                clusterCollection,
                MinLogAdjPvalueScoreEvaluationStrategy())

        Assert.assertEquals(
                idToGroup[1]!!.moduleNames.map { enrichedItems[it]!! }.minBy { it.logAdjPvalue }!!.logAdjPvalue,
                idToGroup[1]!!.score, 1e-10)
        Assert.assertEquals(
                idToGroup[GQNetworkCluster.ZERO_CLUSTER_ID]!!
                        .moduleNames.map { enrichedItems[it]!! }.minBy { it.logAdjPvalue }!!.logAdjPvalue,
                idToGroup[GQNetworkCluster.ZERO_CLUSTER_ID]!!.score, 1e-10)
    }
}