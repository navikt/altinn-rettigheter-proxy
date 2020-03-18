package no.nav.arbeidsgiver.altinnrettigheter.proxy.controller

import no.nav.arbeidsgiver.altinnrettigheter.proxy.model.AltinnOrganisasjon
import no.nav.arbeidsgiver.altinnrettigheter.proxy.service.AltinnrettigheterProxyService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class AltinnrettigheterProxyController (val altinnrettigheterProxyService: AltinnrettigheterProxyService) {
    @GetMapping(value= ["/organisasjoner"])
    fun hentOrganisasjoner(): List<AltinnOrganisasjon> {
        val organisasjoner = altinnrettigheterProxyService.hentOrganisasjoner("")
        println("Organisasjoner: $organisasjoner")
        return organisasjoner
    }

}