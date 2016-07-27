package gq.rest.api

import gq.core.data.Species
import gq.rest.services.EnrichmentResponse
import gq.rest.services.GeneSetEnrichmentService
import gq.rest.validators.EnrichmentRequestFormValidator
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.WebDataBinder
import org.springframework.web.bind.annotation.*


@RestController(GeneSetEnrichmentController.URL)
open class GeneSetEnrichmentController @Autowired constructor(val geneSetEnrichmentService: GeneSetEnrichmentService) {

    companion object {
        val LOG = Logger.getLogger(GeneSetEnrichmentController::class.java)
        const val URL = "/perform-enrichment"

    }

    class EnrichmentRequestForm {
        var genes: List<String>? = null
        var speciesFrom: String? = null
        var speciesTo: String? = null
    }

    @InitBinder
    fun initBinder(binder: WebDataBinder) {
        binder.addValidators(EnrichmentRequestFormValidator())
    }

    @RequestMapping(value = URL,
            produces = arrayOf("application/json"),
            method = arrayOf(RequestMethod.POST))
    @ResponseStatus(HttpStatus.OK)
    fun performEnrichment(@Validated @RequestBody form: EnrichmentRequestForm): EnrichmentResponse {
        LOG.info("Request accepted: speciesTo=${form.speciesTo},speciesFrom=${form.speciesFrom},genes=${form.genes?.joinToString(" ")}")
        val enrichmentResponse = geneSetEnrichmentService.findEnrichedModules(
                form.genes!!,
                Species.fromOriginal(form.speciesFrom!!),
                Species.fromOriginal(form.speciesTo ?: form.speciesFrom!!))
        LOG.info("Enrichment result: ${enrichmentResponse.enrichmentResultItems.size}")
        return enrichmentResponse
    }
}