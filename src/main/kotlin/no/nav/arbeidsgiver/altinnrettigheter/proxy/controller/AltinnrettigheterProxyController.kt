package no.nav.arbeidsgiver.altinnrettigheter.proxy.controller

import no.nav.arbeidsgiver.altinnrettigheter.proxy.model.AltinnOrganisasjon
import no.nav.arbeidsgiver.altinnrettigheter.proxy.model.Fnr
import no.nav.arbeidsgiver.altinnrettigheter.proxy.service.AltinnrettigheterService
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
class AltinnrettigheterProxyController(val altinnrettigheterService: AltinnrettigheterService, var tilgangskotrollService: TilgangskontrollService) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @GetMapping(value = ["ekstern/altinn/api/serviceowner/reportees"])
    fun proxyOrganisasjoner(
            @RequestParam query: Map<String, String>
    ): List<AltinnOrganisasjon> {
        logger.info("Mottatt request for organisasjoner innlogget brukeren har rettigheter i")

        validerObligatoriskeParametre(query, "subject", "ForceEIAuthentication");

        val subject: String = query["subject"]!!

        val fnr = tilgangskotrollService.hentInnloggetBruker().fnr

        if (Fnr(subject) == fnr) {
            return altinnrettigheterService.hentOrganisasjoner(query)
        } else {
            throw ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Du har ikke rettigheter til denne")
        }
    }

    private fun validerObligatoriskeParametre(query: Map<String, String>, vararg obligatorisk: String) {
        val parametreSomIkkeErMed = obligatorisk.filter { !query.containsKey(it) }
        if (parametreSomIkkeErMed.isNotEmpty()) {
            throw ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Obligatoriske parametre ble ikke sendt med: $parametreSomIkkeErMed"
            )
        }
    }

}
