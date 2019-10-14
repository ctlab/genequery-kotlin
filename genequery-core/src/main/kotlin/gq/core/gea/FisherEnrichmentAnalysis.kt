package gq.core.gea

import gq.core.data.*
import gq.core.math.FisherExact

data class EnrichmentResultItem(val datasetId: String,
                                val clusterId: String,
                                val pvalue: Double,
                                val logPvalue: Double,
                                val adjPvalue: Double,
                                val logAdjPvalue: Double,
                                val intersectionSize: Int,
                                val moduleSize: Int) : Comparable<EnrichmentResultItem> {

    companion object {
        const val MIN_LOG_P_VALUE = -325.0
    }

    constructor(module: GQModule, pvalue: Double, adjPvalue: Double, intersectionSize: Int) : this(
            module.datasetId,
            module.clusterId,
            pvalue,
            if (pvalue > 0) Math.log10(pvalue) else MIN_LOG_P_VALUE,
            adjPvalue,
            if (adjPvalue > 0) Math.log10(adjPvalue) else MIN_LOG_P_VALUE,
            intersectionSize,
            module.size)

    override fun compareTo(other: EnrichmentResultItem) = logPvalue.compareTo(other.logPvalue)
}

data class SpecifiedEntrezGenes(val species: Species, val entrezIds: List<Long>)


fun findBonferroniSignificant(
        moduleCollection: GQModuleCollection,
        query: SpecifiedEntrezGenes,
        bonferroniMaxPvalue: Double = 0.01): List<EnrichmentResultItem> {
    val experimentsForThisSpecies = moduleCollection.speciesToGseGpl[query.species] ?: return emptyList()
    val queryEntrezIds = if (query.entrezIds.isNotEmpty()) query.entrezIds.toSortedSet().toLongArray() else return emptyList()
    val moduleCount = moduleCollection.speciesToModules[query.species]!!.size

    return experimentsForThisSpecies.mapNotNull(
            fun(it: String): List<EnrichmentResultItem>? {
                val modules = moduleCollection.seriesToModules[it]!!
                val moduleIdToIntersectionSize = modules
                        .associate { it.clusterId to it.sortedEntrezIds.sizeOfIntersectionWithSorted(queryEntrezIds) }
                val queryUniverseOverlap = moduleIdToIntersectionSize.values.sum()

                if (queryUniverseOverlap == 0) return null

                val universeSize = modules.sumBy { it.size }
                return modules.filter { moduleIdToIntersectionSize[it.clusterId]!! > 0}.mapNotNull { module ->
                    val moduleAndQuery = moduleIdToIntersectionSize[module.clusterId]!!
                    val moduleAndNotQuery = module.size - moduleAndQuery
                    val queryAndNotModule = queryUniverseOverlap - moduleAndQuery
                    val restOfGenes = universeSize - moduleAndQuery - queryAndNotModule - moduleAndNotQuery

                    val pvalue = FisherExact.instance.rightTailPvalue(
                            moduleAndQuery, moduleAndNotQuery, queryAndNotModule, restOfGenes)
                    val adjustedPvalue = pvalue * moduleCount
                    if (adjustedPvalue <= bonferroniMaxPvalue) {
                        EnrichmentResultItem(module, pvalue, adjustedPvalue, moduleAndQuery)
                    } else {
                        null
                    }

                }
            }
        ).flatten().sorted()
}

