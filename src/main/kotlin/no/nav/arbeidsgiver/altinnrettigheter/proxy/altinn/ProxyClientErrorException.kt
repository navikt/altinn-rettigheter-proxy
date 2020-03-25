package no.nav.arbeidsgiver.altinnrettigheter.proxy.altinn

import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.web.client.HttpClientErrorException

class ProxyClientErrorException(
        var httpStatus: HttpStatus,
        var statusText: String,
        var httpHeaders: HttpHeaders?,
        var responseBodyAsString: String,
        exception: HttpClientErrorException
) : RuntimeException(exception)