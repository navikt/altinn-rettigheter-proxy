package no.nav.arbeidsgiver.altinnrettigheter.proxy.controller

import no.nav.arbeidsgiver.altinnrettigheter.proxy.model.AltinnOrganisasjon
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
class AltinnrettigheterProxyController(val altinnrettigheterService: AltinnrettigheterService, var tilgangskontrollService: TilgangskontrollService) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @GetMapping(value = ["organisasjoner"])
    fun proxyOrganisasjonerNY(
            @RequestParam query: Map<String, String>
    ): List<AltinnOrganisasjon> {
        return proxyOrganisasjoner(
                leggTilParameter(
                        query, "ForceEIAuthentication",
                        ""
                )
        )
    }

    @GetMapping(value = ["ekstern/altinn/api/serviceowner/reportees"])
    fun proxyOrganisasjoner(
            @RequestParam query: Map<String, String>
    ): List<AltinnOrganisasjon> {
        logger.info("Mottatt request for organisasjoner innlogget brukeren har rettigheter i")

        val validertQuery = validerOgFiltrerQuery(query)

        return altinnrettigheterService.hentOrganisasjoner(
                validertQuery,
                tilgangskontrollService.hentInnloggetBruker().fnr
        )
    }


    private fun leggTilParameter(query: Map<String, String>, key: String, value: String): Map<String, String> {
        val oppdaterbarQuery = query.toMutableMap()
        oppdaterbarQuery[key] = value
        return oppdaterbarQuery.toMap()
    }

    private fun validerOgFiltrerQuery(query: Map<String, String>): Map<String, String> {
        validerObligatoriskeParametre(query,"ForceEIAuthentication")

        if (query.containsKey("subject")) {
            logger.warn("Request inneholder subject (fødselsnummer). Dette burde forhindres av personvernshensyn, da det logges av andre systemer.")

            return query.filter { (key, _) -> key != "subject" }
        }
        return query
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
