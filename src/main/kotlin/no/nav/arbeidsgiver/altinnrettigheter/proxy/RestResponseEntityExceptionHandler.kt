package no.nav.arbeidsgiver.altinnrettigheter.proxy

import no.nav.arbeidsgiver.altinnrettigheter.proxy.altinn.AltinnException
import no.nav.arbeidsgiver.altinnrettigheter.proxy.altinn.ProxyHttpStatusCodeException
import no.nav.arbeidsgiver.altinnrettigheter.proxy.tilgangskontroll.TilgangskontrollException
import no.nav.security.spring.oidc.validation.interceptor.OIDCUnauthorizedException
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

    @ExceptionHandler(value = [TilgangskontrollException::class])
    @ResponseBody
    protected fun handleTilgangskontrollException(e: RuntimeException, webRequest: WebRequest?): ResponseEntity<Any> {
        return getResponseEntity(e, "You don't have access to this ressource", HttpStatus.FORBIDDEN)
    }

    @ExceptionHandler(value = [OIDCUnauthorizedException::class, AccessDeniedException::class])
    @ResponseBody
    protected fun handleUnauthorizedException(e: RuntimeException, webRequest: WebRequest?): ResponseEntity<Any> {
        return getResponseEntity(e, "You are not authorized to access this ressource", HttpStatus.UNAUTHORIZED)
    }

    @ExceptionHandler(value = [AltinnException::class])
    @ResponseBody
    protected fun handleAltinnException(e: RuntimeException, webRequest: WebRequest?): ResponseEntity<Any> {
        Companion.logger.error("Feil ved Altinn integrasjon", e)
        return getResponseEntity(e, "Internal error", HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @ExceptionHandler(value = [ProxyHttpStatusCodeException::class])
    @ResponseBody
    protected fun handleProxyHttpStatusCodeException(
            e: ProxyHttpStatusCodeException,
            webRequest: WebRequest?
    ): ResponseEntity<Any> {
        Companion.logger.warn("Feil ved Altinn integrasjon, " +
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
        Companion.logger.warn(e.message, e)
        return getResponseEntity(e, e.message, e.status)
    }

    @ExceptionHandler(value = [Exception::class])
    @ResponseBody
    protected fun handleGenerellException(e: RuntimeException, webRequest: WebRequest?): ResponseEntity<Any> {
        Companion.logger.error("Uhåndtert feil", e)
        return getResponseEntity(e, "Internal error", HttpStatus.INTERNAL_SERVER_ERROR)
    }

    private fun getResponseEntity(e: RuntimeException, melding: String, status: HttpStatus): ResponseEntity<Any> {
        val body = HashMap<String, String>(1)
        body["message"] = melding
        Companion.logger.info(String.format(
                "Returnerer følgende HttpStatus '%s' med melding '%s' pga exception '%s'",
                status.toString(),
                melding,
                e.message
        ))
        return ResponseEntity.status(status).contentType(MediaType.APPLICATION_JSON).body(body)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ResponseEntityExceptionHandler::class.java)
    }
}
