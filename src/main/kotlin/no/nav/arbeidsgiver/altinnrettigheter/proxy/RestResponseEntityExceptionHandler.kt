package no.nav.arbeidsgiver.altinnrettigheter.proxy

import no.nav.arbeidsgiver.altinnrettigheter.proxy.altinn.AltinnException
import no.nav.arbeidsgiver.altinnrettigheter.proxy.altinn.ProxyHttpStatusCodeException
import no.nav.arbeidsgiver.altinnrettigheter.proxy.controller.UgyldigParameterException
import no.nav.arbeidsgiver.altinnrettigheter.proxy.tilgangskontroll.TilgangskontrollException
import no.nav.security.token.support.spring.validation.interceptor.JwtTokenUnauthorizedException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.context.request.WebRequest
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import java.nio.file.AccessDeniedException
import java.util.*


@ControllerAdvice
class RestResponseEntityExceptionHandler : ResponseEntityExceptionHandler() {

    @ExceptionHandler(value = [UgyldigParameterException::class])
    @ResponseBody
    protected fun handleBadRequestException(e: UgyldigParameterException, webRequest: WebRequest?): ResponseEntity<Any> {
        return getResponseEntity(
                "Parameter '${e.parameterNavn}' har en ugyldig verdi '${e.parameterValue}'",
                HttpStatus.BAD_REQUEST
        )
    }

    @ExceptionHandler(value = [TilgangskontrollException::class])
    @ResponseBody
    protected fun handleTilgangskontrollException(e: RuntimeException, webRequest: WebRequest?): ResponseEntity<Any> {
        return getResponseEntity(e, "You don't have access to this ressource", HttpStatus.FORBIDDEN)
    }

    @ExceptionHandler(value = [JwtTokenUnauthorizedException::class, AccessDeniedException::class])
    @ResponseBody
    protected fun handleUnauthorizedException(e: RuntimeException, webRequest: WebRequest?): ResponseEntity<Any> {
        return getResponseEntity(e, "You are not authorized to access this ressource", HttpStatus.UNAUTHORIZED)
    }

    @ExceptionHandler(value = [AltinnException::class])
    @ResponseBody
    protected fun handleAltinnException(e: RuntimeException, webRequest: WebRequest?): ResponseEntity<Any> {
        log.error("Feil ved Altinn integrasjon", e)
        return getResponseEntity(e, "Internal error", HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @ExceptionHandler(value = [ProxyHttpStatusCodeException::class])
    @ResponseBody
    protected fun handleProxyHttpStatusCodeException(
            e: ProxyHttpStatusCodeException,
            webRequest: WebRequest?
    ): ResponseEntity<Any> {
        log.warn("Feil ved Altinn integrasjon, " +
                "med status '${e.httpStatus}' " +
                ", statusText '${e.statusText}'" +
                " og responseBody '${e.responseBodyAsString}'",
                e
        )

        return ResponseEntity
                .status(e.httpStatus)
                .body(
                        mapOf(
                                "responseBody" to e.responseBodyAsString,
                                "statusText" to e.statusText)
                )
    }

    @ExceptionHandler(value = [ResponseStatusException::class])
    @ResponseBody
    protected fun handleResponseStatusException(e: ResponseStatusException, webRequest: WebRequest?): ResponseEntity<Any> {
        log.warn(e.message, e)
        return getResponseEntity(e, e.message, e.status)
    }

    @ExceptionHandler(value = [Exception::class])
    @ResponseBody
    protected fun handleGenerellException(e: RuntimeException, webRequest: WebRequest?): ResponseEntity<Any> {
        log.error("Uhåndtert feil", e)
        return getResponseEntity(e, "Internal error", HttpStatus.INTERNAL_SERVER_ERROR)
    }

    private fun getResponseEntity(e: RuntimeException, melding: String, status: HttpStatus): ResponseEntity<Any> {
        val body = HashMap<String, String>(1)
        body["message"] = melding
        log.info("Returnerer følgende HttpStatus '{}' med melding '{}' pga exception '{}'",
            status.toString(),
            melding,
            e.message,
            e
        )
        return ResponseEntity.status(status).contentType(MediaType.APPLICATION_JSON).body(body)
    }

    private fun getResponseEntity(melding: String, status: HttpStatus): ResponseEntity<Any> {
        val body = HashMap<String, String>(1)
        body["message"] = melding
        log.info("Returnerer følgende HttpStatus '{}' med melding '{}'",
            status.toString(),
            melding
        )
        return ResponseEntity.status(status).contentType(MediaType.APPLICATION_JSON).body(body)
    }


    companion object {
        private val log = LoggerFactory.getLogger(ResponseEntityExceptionHandler::class.java)
    }
}
