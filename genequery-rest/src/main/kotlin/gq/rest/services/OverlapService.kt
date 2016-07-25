package gq.rest.services

import gq.core.data.GQModule
import gq.core.data.Species
import gq.core.data.intersectWithSorted
import gq.core.genes.GeneFormat
import gq.rest.GQDataRepository
import gq.rest.exceptions.BadRequestException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

data class OverlapResponse(val identifiedGeneFormat: String,
                           val geneConversionMap: Map<String, Long?>,
                           val overlapGenes: LongArray)

@Service
open class OverlapService @Autowired constructor(
        private val gqDataRepository: GQDataRepository) {

    open fun convertGenes(rawGenes: List<String>,
                          speciesFrom: Species,
                          speciesTo: Species = speciesFrom): Pair<GeneFormat, Map<String, Long?>> {
        try {
            val currentGeneFormat = GeneFormat.guess(rawGenes)
            return Pair(
                    currentGeneFormat,
                    gqDataRepository.smartConverter.toEntrez(rawGenes, currentGeneFormat, speciesFrom, speciesTo))
        } catch (ex: IllegalArgumentException) {
            throw BadRequestException(ex)
        }
    }

    open fun getModule(moduleName: String) : GQModule {
        val module = gqDataRepository.moduleCollection.fullNameToGQModule[GQModule.parseFullModuleName(moduleName)]
        if (module != null) {
            return module
        } else {
            throw BadRequestException("Module $moduleName not found.")
        }
    }

    open fun findOverlap(rawGenes: List<String>,
                         speciesFrom: Species,
                         speciesTo: Species = speciesFrom,
                         moduleName: String): OverlapResponse {
        val (identifiedGeneFormat, conversionMap) = convertGenes(rawGenes, speciesFrom, speciesTo)
        val module = getModule(moduleName)
        val entrezIds = conversionMap.values.filterNotNull().toLongArray().sortedArray()
        val overlapGenes = entrezIds intersectWithSorted module.sortedEntrezIds
        return OverlapResponse(identifiedGeneFormat.formatName, conversionMap, overlapGenes)
    }
}
