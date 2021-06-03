package no.nav.arbeidsgiver.altinnrettigheter.proxy.controller

import no.nav.security.token.support.core.api.Unprotected
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@Unprotected
@RestController
class HealthcheckController {
    @GetMapping("/internal/healthcheck")
    fun healthcheck(): String {
        return "OK"
    }

}
