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
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import java.nio.file.AccessDeniedException


@ControllerAdvice
class RestResponseEntityExceptionHandler : ResponseEntityExceptionHandler() {

    @ExceptionHandler(value = [UgyldigParameterException::class])
    @ResponseBody
    protected fun handleUgyldigParameterException(
        e: UgyldigParameterException,
        webRequest: WebRequest?
    ): ResponseEntity<FeilRespons> {
        return getResponseEntity(
            e,
            "Parameter '${e.parameterNavn}' har en ugyldig verdi '${e.parameterValue}'",
            HttpStatus.BAD_REQUEST
        )
    }

    @ExceptionHandler(value = [TilgangskontrollException::class])
    @ResponseBody
    protected fun handleTilgangskontrollException(
        e: TilgangskontrollException,
        webRequest: WebRequest?
    ): ResponseEntity<FeilRespons> {
        return getResponseEntity(e, e.message!!, HttpStatus.FORBIDDEN)
    }

    @ExceptionHandler(value = [JwtTokenUnauthorizedException::class, AccessDeniedException::class])
    @ResponseBody
    protected fun handleUnauthorizedException(
        e: RuntimeException,
        webRequest: WebRequest?
    ): ResponseEntity<FeilRespons> {
        return getResponseEntity(e, "You are not authorized to access this resource", HttpStatus.UNAUTHORIZED)
    }

    @ExceptionHandler(value = [ProxyHttpStatusCodeException::class])
    @ResponseBody
    protected fun handleProxyHttpStatusCodeException(
        e: ProxyHttpStatusCodeException,
        webRequest: WebRequest?
    ): ResponseEntity<FeilRespons> {
        log.warn(
            "Feil ved Altinn integrasjon, med status '${e.httpStatus}' , statusText '${e.statusText}' og responseBody '${e.responseBodyAsString}'",
            e
        )

        return getResponseEntity(e, e.responseBodyAsString, e.httpStatus)
    }

    @ExceptionHandler(value = [AltinnException::class])
    @ResponseBody
    protected fun handleAltinnException(e: AltinnException, webRequest: WebRequest?): ResponseEntity<FeilRespons> {
        log.error("Feil ved Altinn integrasjon", e)
        return getResponseEntity(e.cause!!, e.message!!, HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @ExceptionHandler(value = [RuntimeException::class])
    @ResponseBody
    protected fun handleGenerellException(e: RuntimeException, webRequest: WebRequest?): ResponseEntity<FeilRespons> {
        log.error("Uhåndtert feil", e)
        return getResponseEntity(e, "Internal error", HttpStatus.INTERNAL_SERVER_ERROR)
    }

    private fun getResponseEntity(
        e: Throwable,
        melding: String,
        status: HttpStatus
    ): ResponseEntity<FeilRespons> {
        val body = FeilRespons(message = melding, cause = e.message?:"ukjent feil")
        log.info(
            "Returnerer følgende HttpStatus '$status' med melding '${melding}' pga exception '${e.message}'",
            e
        )
        return ResponseEntity.status(status).contentType(MediaType.APPLICATION_JSON).body(body)
    }

    companion object {
        private val log = LoggerFactory.getLogger(ResponseEntityExceptionHandler::class.java)
    }
}

data class FeilRespons(
    val message: String,
    val cause: String,
)