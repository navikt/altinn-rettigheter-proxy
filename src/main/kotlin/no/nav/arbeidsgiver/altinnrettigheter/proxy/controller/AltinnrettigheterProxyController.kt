package no.nav.arbeidsgiver.altinnrettigheter.proxy.controller

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import no.nav.arbeidsgiver.altinnrettigheter.proxy.model.AltinnOrganisasjon
import no.nav.arbeidsgiver.altinnrettigheter.proxy.service.AltinnrettigheterService
import no.nav.arbeidsgiver.altinnrettigheter.proxy.tilgangskontroll.TilgangskontrollService
import no.nav.security.token.support.core.api.Protected
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
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
        @RequestHeader(value = "host", required = false) host: String?,
        @RequestHeader(value = "X-Consumer-ID", required = false) consumerId: String?,
        @RequestParam serviceCode: String, @RequestParam serviceEdition: String
    ): List<AltinnOrganisasjon> {
        return hentOrganisasjoner(
            consumerId = consumerId,
            host = host,
            query = mapOf(
                "ForceEIAuthentication" to "",
                "serviceCode" to serviceCode,
                "serviceEdition" to serviceEdition,
                "\$filter" to "Type+ne+'Person'+and+Status+eq+'Active'",
                "\$top" to "500",
                "\$skip" to "0"
            ),
        )
    }

    @GetMapping("/v2/organisasjoner")
    fun proxyOrganisasjonerV2(
        @RequestHeader(value = "host", required = false) host: String?,
        @RequestHeader(value = "X-Consumer-ID") consumerId: String,
        @RequestParam(required = false) serviceCode: String?,
        @RequestParam(required = false) serviceEdition: String?,
        @RequestParam top: Number,
        @RequestParam skip: Number,
        @RequestParam(required = false, defaultValue = "true") filterPaaAktiveOrganisasjoner: String,
        @RequestParam(required = false) filter: String?
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

        if (filter != null && filter.isNotEmpty()) {
            queryParametre["\$filter"] = filter
        } else if (filterPaaAktiveOrganisasjoner == "true") {
            queryParametre["\$filter"] = "Type ne 'Person' and Status eq 'Active'"
        }

        if (serviceCode != null) {
            queryParametre["serviceCode"] = serviceCode
        }

        if (serviceEdition != null) {
            queryParametre["serviceEdition"] = serviceEdition
        }

        return hentOrganisasjoner(
            consumerId = consumerId,
            host = host,
            query = queryParametre,
        )
    }

    @GetMapping(value = ["/ekstern/altinn/api/serviceowner/reportees"])
    fun proxyOrganisasjoner(
        @RequestHeader(value = "X-Consumer-ID", required = false) consumerId: String?,
        @RequestHeader(value = "host", required = false) host: String?,
        @RequestParam query: Map<String, String>
    ): List<AltinnOrganisasjon> {
        return hentOrganisasjoner(
            query = query,
            host = host,
            consumerId = consumerId
        )
    }

    private fun hentOrganisasjoner(
        query: Map<String, String>,
        consumerId: String?,
        host: String?,
    ): List<AltinnOrganisasjon> {
        logger.info("Mottatt request for organisasjoner innlogget brukeren har rettigheter i")
        val callingApp = tilgangskontrollService.nameOfAppCallingUs()
        val consumer = callingApp ?: consumerId ?: "UKJENT_KLIENT_APP"
        val validertQuery = validerOgFiltrerQuery(query, consumer)
        return withTimer(consumer, host) {
            altinnrettigheterService.hentOrganisasjoner(
                validertQuery,
                tilgangskontrollService.hentInnloggetBruker().fnr
            )
        }
    }


    private fun validerOgFiltrerQuery(query: Map<String, String>, consumerId: String?): Map<String, String> {
        val forceEIAuthentication = "ForceEIAuthentication"
        if (!query.containsKey(forceEIAuthentication)) {
            throw ManglendeObligatoriskParameterException(listOf(forceEIAuthentication))
        }

        if (query.containsKey("subject")) {
            logger.warn(
                """
                    Request inneholder subject (fødselsnummer). 
                    Dette burde forhindres av personvernshensyn, da det logges av andre systemer. 
                    Callerid: {}
                """.trimMargin(),
                consumerId ?: "ukjent"
            )

            return query.filter { (key, _) -> key != "subject" }
        }
        return query
    }

    private fun sjekkParametreInneholderSifre(parametre: Map<String, String?>) {
        parametre.forEach {
            if ((it.value?.matches(Regex("[0-9]+")) == false)) {
                throw UgyldigParameterException(it.key, it.value!!)
            }
        }
    }

    private val reporteesTimer = ConcurrentHashMap<String, Timer>()
    private fun <T : Any> withTimer(consumerId: String, host: String?, body: () -> T): T =
        reporteesTimer.computeIfAbsent(consumerId) {
            Timer.builder("altinn_rettigheter_proxy_reportees_responsetid")
                .tag("klientapp", it)
                .apply {
                    if (host != null) {
                        tag("host", host)
                    }
                }
                .publishPercentileHistogram()
                .register(meterRegistry)
        }.recordCallable(body)!!
}