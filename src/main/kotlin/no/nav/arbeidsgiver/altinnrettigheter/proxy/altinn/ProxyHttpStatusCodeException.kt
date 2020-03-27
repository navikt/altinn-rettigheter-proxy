package no.nav.arbeidsgiver.altinnrettigheter.proxy.altinn

import org.springframework.http.HttpStatus
import org.springframework.web.client.HttpStatusCodeException

class ProxyHttpStatusCodeException(
        var httpStatus: HttpStatus,
        var statusText: String,
        var responseBodyAsString: String,
        exception: HttpStatusCodeException
) : RuntimeException(exception)