package gq.rest.services

import gq.core.data.GQModule
import gq.core.data.Species
import gq.rest.GQDataRepository
import gq.rest.exceptions.BadRequestException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

data class OverlapResponse(val overlapSymbolGenes: List<String>, val otherModuleSymbolGenes: List<String>)

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
        val module = getModule(moduleName)
        val inputEntrezIds = convertGenesToEntrez(rawGenes, speciesFrom, speciesTo, gqDataRepository)
                .second.values.filterNotNull().toSet()
        val entrezToSymbolModuleGenes = gqDataRepository.smartConverter.toSymbol(module.sortedEntrezIds.toList(), speciesFrom, speciesTo)
        val overlapSymbolGenes = mutableListOf<String?>()
        val otherModuleSymbolGenes = mutableListOf<String?>()
        module.sortedEntrezIds.forEach {
            if (it in inputEntrezIds) {
                overlapSymbolGenes.add(entrezToSymbolModuleGenes[it])
            } else {
                otherModuleSymbolGenes.add(entrezToSymbolModuleGenes[it])
            }
        }
        return OverlapResponse(overlapSymbolGenes.filterNotNull(), otherModuleSymbolGenes.filterNotNull())
    }
}
