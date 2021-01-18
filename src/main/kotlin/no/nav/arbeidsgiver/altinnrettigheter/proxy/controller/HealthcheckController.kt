package no.nav.arbeidsgiver.altinnrettigheter.proxy.controller

import no.nav.security.oidc.api.Unprotected
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@Unprotected
@RestController
class HealthcheckController {
    @GetMapping("/altinn-rettigheter-proxy/internal/healthcheck", "/internal/healthcheck")
    fun healthcheck(): String {
        return "OK"
    }

}
