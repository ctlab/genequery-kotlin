package gq.rest.validators

import gq.core.data.Species
import gq.rest.api.GeneSetEnrichmentController
import org.springframework.validation.Errors
import org.springframework.validation.ValidationUtils
import org.springframework.validation.Validator


fun validateIsNotNull(target: Any?, errors: Errors?, field: String): Boolean {
    if (target == null) {
        errors?.reject("field.is.required", arrayOf(field), "Field {0} is required")
        return false
    }
    return true
}


fun validateIsNotBlank(target: Any?, errors: Errors?, field: String): Boolean {
    if ((target is Collection<*> && target.isEmpty()) || (target is String && target.isEmpty())) {
        errors?.reject("field.is.blank", arrayOf(field), "Field {0} can not be blank")
        return false
    }
    return true
}

fun validateIsNotNullAndNotBlank(target: Any?, errors: Errors?, field: String) =
        validateIsNotNull(target, errors, field) && validateIsNotBlank(target, errors, field)


class SpeciesValidator : Validator {
    override fun validate(target: Any?, errors: Errors?) {
        if (target is Species) return
        if (target is String) {
            try {
                Species.fromOriginal(target)
            } catch(e: IllegalArgumentException) {
                errors?.reject("species.unknown", arrayOf(target.toString()), "Unknown species: {0}")
            }
        }
    }

    override fun supports(clazz: Class<*>?) = String::class.java.equals(clazz) || Species::class.java.equals(clazz)
}


class EnrichmentRequestFormValidator : Validator {
    override fun validate(target: Any?, errors: Errors?) {
        val enrichmentRequestForm = target as GeneSetEnrichmentController.EnrichmentRequestForm
        val speciesValidator = SpeciesValidator()

        if(validateIsNotNullAndNotBlank(enrichmentRequestForm.speciesFrom, errors, "speciesFrom")) {
            ValidationUtils.invokeValidator(speciesValidator, enrichmentRequestForm.speciesFrom, errors)
        }
        if (validateIsNotNullAndNotBlank(enrichmentRequestForm.speciesTo, errors, "speciesTo")) {
            ValidationUtils.invokeValidator(speciesValidator, enrichmentRequestForm.speciesTo, errors)
        }

        validateIsNotNullAndNotBlank(enrichmentRequestForm.genes, errors, "genes")
    }

    override fun supports(clazz: Class<*>?) = GeneSetEnrichmentController.EnrichmentRequestForm::class.java.equals(clazz)
}
