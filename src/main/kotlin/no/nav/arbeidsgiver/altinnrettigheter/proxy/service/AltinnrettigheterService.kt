package no.nav.arbeidsgiver.altinnrettigheter.proxy.service

import no.nav.arbeidsgiver.altinnrettigheter.proxy.model.AltinnOrganisasjon
import no.nav.arbeidsgiver.altinnrettigheter.proxy.model.Fnr
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class AltinnrettigheterService(val proxyService: AltinnrettigheterProxyService) {

    private val logger = LoggerFactory.getLogger(javaClass)

    fun hentOrganisasjoner(fnr: Fnr, serviceCode: String, serviceEdition: String): List<AltinnOrganisasjon> {
        return try {
            proxyService.hentOrganisasjonerCached(fnr, serviceCode, serviceEdition)
        } catch (e: Exception) {
            logger.warn("Fallback etter feil mot Redis cache, pga feil ${e.message}")
            proxyService.hentOrganisasjonerIAltinn(fnr, serviceCode, serviceEdition)
        }
    }

}