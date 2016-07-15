package gq.rest

import com.fasterxml.jackson.annotation.JsonInclude
import gq.rest.api.GeneSetEnrichmentController
import gq.rest.exceptions.BadRequestException
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.MessageSource
import org.springframework.core.MethodParameter
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice
import java.util.*

class ResponseEnvelope(@JsonInclude(JsonInclude.Include.NON_NULL) val result: Any?,
                       val success: Boolean = true,
                       @JsonInclude(JsonInclude.Include.NON_NULL) var errors: List<String>? = null)

class SuccessResponseEntity(body: Any?) : ResponseEntity<ResponseEnvelope>(ResponseEnvelope(body, true, null), HttpStatus.OK)
class ErrorResponseEntity(errors: List<String>?, status: HttpStatus) : ResponseEntity<ResponseEnvelope>(ResponseEnvelope(null, false, errors), status) {
    constructor(error: String, status: HttpStatus) : this(listOf(error), status)
}


@ControllerAdvice
class RestControllerAdvice : ResponseBodyAdvice<Any?> {

    @Autowired
    lateinit var messageSource: MessageSource

    companion object {
        val LOG = Logger.getLogger(GeneSetEnrichmentController::class.java)
        val LOCALE = Locale.ENGLISH
    }

    override fun supports(returnType: MethodParameter?,
                          converterType: Class<out HttpMessageConverter<*>>?) = true


    override fun beforeBodyWrite(body: Any?,
                                 returnType: MethodParameter?,
                                 selectedContentType: MediaType?,
                                 selectedConverterType: Class<out HttpMessageConverter<*>>?,
                                 request: ServerHttpRequest?,
                                 response: ServerHttpResponse?): ResponseEnvelope {
        return if (body is ResponseEnvelope) body else ResponseEnvelope(body);
    }

//    // https://github.com/arawn/kotlin-spring-example/blob/master/src/main/kotlin/org/ksug/forum/web/support/ErrorResponse.kt
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(BadRequestException::class)
    fun handleBadRequestException(e: BadRequestException): ErrorResponseEntity {
        LOG.warn(e)
        return ErrorResponseEntity(e.cause?.message ?: e.message ?: HttpStatus.BAD_REQUEST.reasonPhrase, HttpStatus.BAD_REQUEST)
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun onHandleHttpMessageNotReadable(e: HttpMessageNotReadableException): ErrorResponseEntity {
        LOG.warn(e)
        return ErrorResponseEntity(HttpStatus.BAD_REQUEST.reasonPhrase, HttpStatus.BAD_REQUEST)
    }


    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun onValidationException(e: MethodArgumentNotValidException): ErrorResponseEntity {
        val errors = e.bindingResult.allErrors.map { messageSource.getMessage(it, LOCALE) }.filterNotNull()
        errors.forEach { LOG.warn(it) }
        return ErrorResponseEntity(errors, HttpStatus.BAD_REQUEST)
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(RuntimeException::class)
    fun handleRuntimeExceptions(ex: Exception): ResponseEntity<ResponseEnvelope> {
        LOG.error(HttpStatus.INTERNAL_SERVER_ERROR.reasonPhrase, ex)
        return ErrorResponseEntity(
                messageSource.getMessage("error.internal", null, "Internal error", LOCALE),
                HttpStatus.INTERNAL_SERVER_ERROR)
    }
}

