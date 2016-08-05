package gq.core.gea

import gq.core.data.GQModule
import gq.core.data.GQNetworkCluster
import gq.core.data.GQNetworkClusterCollection
import java.util.*

class ScoredEnrichedItemsGroup(val groupId: Int,
                               val moduleNames: List<String>,
                               val score: Double,
                               val annotation: String? = null) {
    companion object {
        fun zeroGroup(moduleNames: List<String>, score: Double) =
                ScoredEnrichedItemsGroup(GQNetworkCluster.FREE_CLUSTER_ID, moduleNames, score)
    }
    constructor(cluster: GQNetworkCluster, moduleNames: List<String>, score: Double)
    : this(cluster.id, moduleNames, score, cluster.annotation)
}


fun groupEnrichedItemsByClusters(
        enrichedItems: List<EnrichmentResultItem>,
        clusterCollection: GQNetworkClusterCollection,
        scoreEvaluationStrategy: EnrichedGroupScoreEvaluationStrategy): Map<Int, ScoredEnrichedItemsGroup> {
    val nameToEnrichedItem = enrichedItems.associate { Pair(GQModule.joinFullName(it.gse, it.gpl, it.moduleNumber), it) }
    val groupedNames = mutableListOf<String>()

    val groups = clusterCollection.idToCluster.values.map {
        val commonNames = it.moduleNames.intersect(nameToEnrichedItem.keys)
        if (commonNames.isNotEmpty()) {
            val score = scoreEvaluationStrategy.calculateScore(commonNames.map { nameToEnrichedItem[it]!! }, it)
            groupedNames.addAll(commonNames)
            ScoredEnrichedItemsGroup(it, commonNames.toList(), score)
        } else {
            null
        }
    }.filterNotNull().toMutableList()

    if (groupedNames.size < nameToEnrichedItem.size) {
        val notGroupedNames = nameToEnrichedItem.keys.subtract(groupedNames).toList()
        val score = scoreEvaluationStrategy.calculateZeroGroupScore(notGroupedNames.map { nameToEnrichedItem[it]!! })
        groups.add(ScoredEnrichedItemsGroup.zeroGroup(notGroupedNames, score))
    }
    return groups.associate { Pair(it.groupId, it) }
}


interface EnrichedGroupScoreEvaluationStrategy {
    fun calculateScore(enrichedItems: Collection<EnrichmentResultItem>, cluster: GQNetworkCluster): Double
    fun calculateZeroGroupScore(enrichedItems: Collection<EnrichmentResultItem>): Double
    fun scoreComparator(): Comparator<Double>
}


class MinLogAdjPvalueScoreEvaluationStrategy : EnrichedGroupScoreEvaluationStrategy {
    override fun calculateScore(enrichedItems: Collection<EnrichmentResultItem>, cluster: GQNetworkCluster) =
            minOrException(enrichedItems)

    override fun calculateZeroGroupScore(enrichedItems: Collection<EnrichmentResultItem>) =
            minOrException(enrichedItems)

    override fun scoreComparator() = Comparator<Double> { scoreA, scoreB -> scoreB.compareTo(scoreA) }

    fun minOrException(enrichedItems: Collection<EnrichmentResultItem>) =
            enrichedItems.map { it.logAdjPvalue }.min() ?: throw Exception("No enriched items found")
}