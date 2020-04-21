package no.nav.arbeidsgiver.altinnrettigheter.proxy.controller

import no.nav.arbeidsgiver.altinnrettigheter.proxy.model.AltinnOrganisasjon
import no.nav.arbeidsgiver.altinnrettigheter.proxy.service.AltinnrettigheterService
import no.nav.arbeidsgiver.altinnrettigheter.proxy.tilgangskontroll.TilgangskontrollService
import no.nav.metrics.MetricsFactory
import no.nav.metrics.Timer
import no.nav.security.oidc.api.Protected
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@Protected
@RestController
class AltinnrettigheterProxyController(val altinnrettigheterService: AltinnrettigheterService, var tilgangskontrollService: TilgangskontrollService) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @GetMapping(value = ["organisasjoner"])
    fun proxyOrganisasjonerNY(
            @RequestHeader(value = "X-Correlation-ID", required = false) correlationId: String?,
            @RequestParam serviceCode: String, @RequestParam serviceEdition: String
    ): List<AltinnOrganisasjon> {
        return proxyOrganisasjoner(
                correlationId,
                mapOf(
                        "ForceEIAuthentication" to "",
                        "serviceCode" to serviceCode,
                        "serviceEdition" to serviceEdition
                )
        )
    }

    @GetMapping(value = ["ekstern/altinn/api/serviceowner/reportees"])
    fun proxyOrganisasjoner(
            @RequestHeader(value = "X-Correlation-ID", required = false) correlationId: String?,
            @RequestParam query: Map<String, String>
    ): List<AltinnOrganisasjon> {
        logger.info("Mottatt request for organisasjoner innlogget brukeren har rettigheter i")

        val validertQuery = validerOgFiltrerQuery(query)

        val timer: Timer = MetricsFactory
                .createTimer(
                "altinn-rettigheter-proxy.reportees.responsetid.${correlationId?:"UKJENT_KLIENT_APP"}")
                .start()

        val organisasjoner = altinnrettigheterService.hentOrganisasjoner(
                validertQuery,
                tilgangskontrollService.hentInnloggetBruker().fnr
        )
        timer.stop().report()

        MetricsFactory.createEvent("altinn-rettigheter-proxy.reportees")
                .addTagToReport("klientapp", correlationId?:"UKJENT_KLIENT_APP")
                .report()

        return organisasjoner
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
