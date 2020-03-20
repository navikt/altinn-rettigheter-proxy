package no.nav.arbeidsgiver.altinnrettigheter.proxy.controller

import no.nav.arbeidsgiver.altinnrettigheter.proxy.model.AltinnOrganisasjon
import no.nav.arbeidsgiver.altinnrettigheter.proxy.model.Fnr
import no.nav.arbeidsgiver.altinnrettigheter.proxy.service.AltinnrettigheterProxyService
import no.nav.arbeidsgiver.altinnrettigheter.proxy.tilgangskontroll.TilgangskontrollService
import no.nav.security.oidc.api.Protected
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.ui.ModelMap
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@Protected
@RestController
class AltinnrettigheterProxyController(val altinnrettigheterProxyService: AltinnrettigheterProxyService, var tilgangskotrollService: TilgangskontrollService) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @GetMapping(value = ["ekstern/altinn/api/serviceowner/reportees"])
    fun proxyOrganisasjoner(@RequestParam allRequestParams: Map<String, String>, model: ModelMap): List<AltinnOrganisasjon> {
        logger.info("Mottat request for organisasjoner innlogget brukeren har rettigheter i")
        val fnrString = allRequestParams["subject"] ?: error("Mangler subject")
        val kanInnloggetGjøreOppsalg = Fnr(fnrString) == tilgangskotrollService.hentInnloggetBruker().fnr

        if (kanInnloggetGjøreOppsalg) {
            return altinnrettigheterProxyService.hentOrganisasjoner(fnrString)
        } else {
            throw ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Du har ikke rettigheter til denne")
        }
    }

}
