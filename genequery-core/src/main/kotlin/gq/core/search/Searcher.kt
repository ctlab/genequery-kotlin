package gq.core.search

import gq.core.data.GQModule
import gq.core.data.GQModuleCollection
import gq.core.data.Species
import gq.core.data.sizeOfIntersectionWithSorted
import gq.core.math.FisherExact

data class SearchResult(val gse: Int,
                        val gpl: Int,
                        val moduleNumber: Int,
                        val pvalue: Double,
                        val logPvalue: Double,
                        val adjPvalue: Double,
                        val logAdjPvalue: Double,
                        val intersectionSize: Int,
                        val moduleSize: Int) : Comparable<SearchResult> {

    constructor(module: GQModule, pvalue: Double, adjPvalue: Double, intersectionSize: Int) : this(
            module.gse, module.gpl, module.number,
            pvalue,
            if (pvalue > 0) Math.log10(pvalue) else Double.NEGATIVE_INFINITY,
            adjPvalue,
            if (adjPvalue > 0) Math.log10(adjPvalue) else Double.NEGATIVE_INFINITY,
            intersectionSize,
            module.size)

    override fun compareTo(other: SearchResult) = logPvalue.compareTo(other.logPvalue)
}

data class GQRequest(val species: Species, val entrezIds: List<Long>)


fun findBonferroniSignificant(
        dataset: GQModuleCollection,
        query: GQRequest,
        bonferroniMaxPvalue: Double = 0.01): List<SearchResult> {
    val experimentsForThisSpecies = dataset.speciesToGseGpl[query.species] ?: return emptyList()
    val queryEntrezIds = if (query.entrezIds.isNotEmpty()) query.entrezIds.toSortedSet().toLongArray() else return emptyList()
    val moduleCount = dataset.speciesToModules[query.species]!!.size

    return experimentsForThisSpecies.mapNotNull(
            fun(it: Pair<Int, Int>): List<SearchResult>? {
                val modules = dataset.seriesToModules[it]!!
                val moduleIdToIntersectionSize = modules
                        .associate { it.number to it.sortedEntrezIds.sizeOfIntersectionWithSorted(queryEntrezIds) }
                val queryUniverseOverlap = moduleIdToIntersectionSize.values.sum()

                if (queryUniverseOverlap == 0) return null

                val universeSize = modules.sumBy { it.size }
                return modules.filter { it.number > 0 && moduleIdToIntersectionSize[it.number]!! > 0}.mapNotNull { module ->
                    val moduleAndQuery = moduleIdToIntersectionSize[module.number]!!
                    val moduleAndNotQuery = module.size - moduleAndQuery
                    val queryAndNotModule = queryUniverseOverlap - moduleAndQuery
                    val restOfGenes = universeSize - moduleAndQuery - queryAndNotModule - moduleAndNotQuery

                    val pvalue = FisherExact.instance.rightTailPvalue(
                            moduleAndQuery, moduleAndNotQuery, queryAndNotModule, restOfGenes)
                    val adjustedPvalue = pvalue * moduleCount
                    if (adjustedPvalue <= bonferroniMaxPvalue) {
                        SearchResult(module, pvalue, adjustedPvalue, moduleAndQuery)
                    } else {
                        null
                    }

                }
            }
        ).flatMap { it }.sorted()
}

