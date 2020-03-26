package no.nav.arbeidsgiver.altinnrettigheter.proxy.altinn

import org.springframework.http.HttpStatus
import org.springframework.web.client.HttpClientErrorException

class ProxyClientErrorException(
        var httpStatus: HttpStatus,
        var statusText: String,
        var responseBodyAsString: String,
        exception: HttpClientErrorException
) : RuntimeException(exception)