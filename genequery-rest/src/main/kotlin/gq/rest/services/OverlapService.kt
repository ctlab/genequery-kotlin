package gq.rest.services

import gq.core.data.GQModule
import gq.core.data.Species
import gq.core.data.intersectWithSorted
import gq.rest.GQDataRepository
import gq.rest.exceptions.BadRequestException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

data class OverlapResponse(val overlapGenes: LongArray)

@Service
open class OverlapService @Autowired constructor(private val gqDataRepository: GQDataRepository) {

    open fun getModule(moduleName: String): GQModule {
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
        val conversionMap = convertGenes(rawGenes, speciesFrom, speciesTo, gqDataRepository).second
        val module = getModule(moduleName)
        val entrezIds = conversionMap.values.filterNotNull().toLongArray().sortedArray()
        val overlapGenes = entrezIds intersectWithSorted module.sortedEntrezIds
        return OverlapResponse(overlapGenes)
    }
}
