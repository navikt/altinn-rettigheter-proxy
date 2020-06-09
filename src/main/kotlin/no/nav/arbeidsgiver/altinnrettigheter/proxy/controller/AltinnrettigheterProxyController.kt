package no.nav.arbeidsgiver.altinnrettigheter.proxy.controller

import no.nav.arbeidsgiver.altinnrettigheter.proxy.model.AltinnOrganisasjon
import no.nav.arbeidsgiver.altinnrettigheter.proxy.model.Fnr
import no.nav.arbeidsgiver.altinnrettigheter.proxy.service.AltinnrettigheterService
import no.nav.arbeidsgiver.altinnrettigheter.proxy.tilgangskontroll.TilgangskontrollService
import no.nav.metrics.MetricsFactory
import no.nav.metrics.Timer
import no.nav.security.oidc.api.Protected
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@Protected
@RestController
class AltinnrettigheterProxyController(
        val altinnrettigheterService: AltinnrettigheterService,
        var tilgangskontrollService: TilgangskontrollService
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Value(value = "\${altinn.reportees.pagesize}")
    lateinit var altinnReporteesPageSize: String


    @GetMapping(value = ["organisasjoner"])
    fun hentOrganisasjoner(
            @RequestHeader(value = "X-Consumer-ID", required = false) consumerId: String?,
            @RequestParam serviceCode: String,
            @RequestParam serviceEdition: String,
            @RequestParam(value = "inkluderAlle", defaultValue = "false", required = false) inkluderAlle: String
    ): List<AltinnOrganisasjon> {
        logger.info("Mottatt request for organisasjoner innlogget brukeren har rettigheter i. " +
                "InkluderAlle?: $inkluderAlle ")
        val responsetidPerKlient: Timer = startResponseTidPerKlient(consumerId)
        val responsetidAlleKlienter: Timer = startResponseTidAlleKlienter()

        val inkluderAlleBool = "true".equals(inkluderAlle.toLowerCase())
        val fnr = tilgangskontrollService.hentInnloggetBruker().fnr
        val organisasjoner =  hentOrganisasjoner(
                inkluderAlleBool,
                serviceCode,
                serviceEdition,
                fnr
        )

        responsetidPerKlient.stop().report()
        responsetidAlleKlienter.stop().report()
        reportKallTilEndepunkt(consumerId)

        return organisasjoner;
    }

    @GetMapping(value = ["ekstern/altinn/api/serviceowner/reportees"])
    fun proxyOrganisasjoner(
            @RequestHeader(value = "X-Consumer-ID", required = false) consumerId: String?,
            @RequestParam query: Map<String, String>
    ): List<AltinnOrganisasjon> {

        logger.info("Mottatt request for organisasjoner innlogget brukeren har rettigheter i")
        val validertQuery = validerOgFiltrerQuery(query)

        val responsetidPerKlient: Timer = startResponseTidPerKlient(consumerId)
        val responsetidAlleKlienter: Timer = startResponseTidAlleKlienter()

        val organisasjoner = altinnrettigheterService.hentOrganisasjoner(
                validertQuery,
                tilgangskontrollService.hentInnloggetBruker().fnr
        )
        responsetidPerKlient.stop().report()
        responsetidAlleKlienter.stop().report()
        reportKallTilEndepunkt(consumerId)

        return organisasjoner
    }

    private fun hentOrganisasjoner(
            inkluderAlle: Boolean,
            serviceCode: String,
            serviceEdition: String,
            fnr: Fnr
    ): List<AltinnOrganisasjon> {
        val altinnOrganisasjoner = mutableSetOf<AltinnOrganisasjon>()
        var hasMore = true
        var pageNumber = 0

        if (inkluderAlle) {
            while (hasMore) {
                pageNumber++
                val organisasjoner = altinnrettigheterService.hentOrganisasjoner(
                        mapOf(
                                "ForceEIAuthentication" to "",
                                "serviceCode" to serviceCode,
                                "serviceEdition" to serviceEdition,
                                "\$top" to altinnReporteesPageSize,
                                "\$skip" to ((pageNumber - 1) * Integer.valueOf(altinnReporteesPageSize)).toString()
                        ),
                        fnr
                )
                altinnOrganisasjoner.addAll(organisasjoner)
                hasMore = organisasjoner.size >= Integer.valueOf(altinnReporteesPageSize)
            }
            return ArrayList(altinnOrganisasjoner)
        } else {
            return altinnrettigheterService.hentOrganisasjoner(
                    mapOf(
                            "ForceEIAuthentication" to "",
                            "serviceCode" to serviceCode,
                            "serviceEdition" to serviceEdition
                    ),
                    fnr
            )
        }
    }

    private fun startResponseTidPerKlient(consumerId: String?): Timer {
        return MetricsFactory
                .createTimer(
                        "altinn-rettigheter-proxy.reportees.responsetid.${consumerId?:"UKJENT_KLIENT_APP"}")
                .start()
    }

    private fun startResponseTidAlleKlienter(): Timer {
        return MetricsFactory
                .createTimer(
                        "altinn-rettigheter-proxy.reportees.responsetid.alle")
                .start()
    }

    private fun reportKallTilEndepunkt(consumerId: String?) {
        MetricsFactory.createEvent("altinn-rettigheter-proxy.reportees")
                .addTagToReport("klientapp", consumerId?:"UKJENT_KLIENT_APP")
                .report()
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
