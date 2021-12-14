package no.nav.arbeidsgiver.altinnrettigheter.proxy.controller

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import no.nav.arbeidsgiver.altinnrettigheter.proxy.model.AltinnOrganisasjon
import no.nav.arbeidsgiver.altinnrettigheter.proxy.service.AltinnrettigheterService
import no.nav.arbeidsgiver.altinnrettigheter.proxy.tilgangskontroll.TilgangskontrollService
import no.nav.security.token.support.core.api.Protected
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.util.concurrent.ConcurrentHashMap

@Protected
@RestController
class AltinnrettigheterProxyController(
    val altinnrettigheterService: AltinnrettigheterService,
    var tilgangskontrollService: TilgangskontrollService,
    val meterRegistry: MeterRegistry
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @GetMapping(value = ["/organisasjoner"])
    fun proxyOrganisasjonerNY(
            @RequestHeader(value = "X-Consumer-ID", required = false) consumerId: String?,
            @RequestParam serviceCode: String, @RequestParam serviceEdition: String
    ): List<AltinnOrganisasjon> {

        return proxyOrganisasjoner(
                consumerId,
                mapOf(
                        "ForceEIAuthentication" to "",
                        "serviceCode" to serviceCode,
                        "serviceEdition" to serviceEdition,
                        "\$filter" to "Type+ne+'Person'+and+Status+eq+'Active'",
                        "\$top" to "500",
                        "\$skip" to "0"
                )
        )
    }

    @GetMapping("/v2/organisasjoner")
    fun proxyOrganisasjonerV2(
            @RequestHeader(value = "X-Consumer-ID") consumerId: String,
            @RequestParam(required = false) serviceCode: String?,
            @RequestParam(required = false) serviceEdition: String?,
            @RequestParam top: Number,
            @RequestParam skip: Number,
            @RequestParam(required = false, defaultValue = "true") filterPaaAktiveOrganisasjoner: String
    ): List<AltinnOrganisasjon> {
        sjekkParametreInneholderSifre(
                mapOf(
                        "serviceCode" to serviceCode,
                        "serviceEdition" to serviceEdition

                )
        )


        val queryParametre = mutableMapOf(
                "ForceEIAuthentication" to "",
                "\$top" to "$top",
                "\$skip" to "$skip"
        )

        if (filterPaaAktiveOrganisasjoner == "true") {
            queryParametre["\$filter"] = "Type ne 'Person' and Status eq 'Active'"
        }

        if (serviceCode != null) {
            queryParametre["serviceCode"] = serviceCode
        }

        if (serviceEdition != null) {
            queryParametre["serviceEdition"] = serviceEdition
        }

        return proxyOrganisasjoner(
                consumerId,
                queryParametre
        )
    }

    @GetMapping(value = ["/ekstern/altinn/api/serviceowner/reportees"])
    fun proxyOrganisasjoner(
            @RequestHeader(value = "X-Consumer-ID", required = false) consumerId: String?,
            @RequestParam query: Map<String, String>
    ): List<AltinnOrganisasjon> {
        logger.info("Mottatt request for organisasjoner innlogget brukeren har rettigheter i")

        val validertQuery = validerOgFiltrerQuery(query)

        return withTimer(consumerId ?: "UKJENT_KLIENT_APP") {
            altinnrettigheterService.hentOrganisasjoner(
                validertQuery,
                tilgangskontrollService.hentInnloggetBruker().fnr
            )
        }
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

    private fun sjekkParametreInneholderSifre(parametre: Map<String, String?>) {
        parametre.forEach {
            if ((it.value?.matches(Regex("[0-9]+")) == false)) {
                throw UgyldigParameterException(it.key, it.value!!)
            }
        }
    }

    private val reporteesTimer = ConcurrentHashMap<String, Timer>()
    private fun <T : Any> withTimer(consumerId: String, body: () -> T): T =
        reporteesTimer.computeIfAbsent(consumerId) {
            Timer.builder("altinn_rettigheter_proxy_reportees_responsetid")
                .tag("klientapp", it)
                .publishPercentileHistogram()
                .register(meterRegistry)
        }.recordCallable(body)!!
}