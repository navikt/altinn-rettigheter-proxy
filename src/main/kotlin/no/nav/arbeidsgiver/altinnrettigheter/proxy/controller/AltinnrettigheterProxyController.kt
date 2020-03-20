package no.nav.arbeidsgiver.altinnrettigheter.proxy.controller

import no.nav.arbeidsgiver.altinnrettigheter.proxy.model.AltinnOrganisasjon
import no.nav.arbeidsgiver.altinnrettigheter.proxy.model.Fnr
import no.nav.arbeidsgiver.altinnrettigheter.proxy.service.AltinnrettigheterProxyService
import no.nav.arbeidsgiver.altinnrettigheter.proxy.tilgangskontroll.TilgangskontrollService
import no.nav.security.oidc.api.Protected
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@Protected
@RestController
class AltinnrettigheterProxyController(val altinnrettigheterProxyService: AltinnrettigheterProxyService, var tilgangskotrollService: TilgangskontrollService) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @GetMapping(value = ["ekstern/altinn/api/serviceowner/reportees"])
    fun proxyOrganisasjoner(
            @RequestParam ForceEIAuthentication: String,
            @RequestParam serviceCode: String,
            @RequestParam serviceEdition: String,
            @RequestParam subject: String
    ): List<AltinnOrganisasjon> {
        logger.info("Mottatt request for organisasjoner innlogget brukeren har rettigheter i")

        val fnr = tilgangskotrollService.hentInnloggetBruker().fnr

        if (Fnr(subject) == fnr) {
            return altinnrettigheterProxyService.hentOrganisasjoner(fnr, serviceCode, serviceEdition)
        } else {
            throw ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Du har ikke rettigheter til denne")
        }
    }

}
