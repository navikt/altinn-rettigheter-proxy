package no.nav.arbeidsgiver.altinnrettigheter.proxy.service

import no.nav.arbeidsgiver.altinnrettigheter.proxy.altinn.AltinnException
import no.nav.arbeidsgiver.altinnrettigheter.proxy.altinn.ProxyClientErrorException
import no.nav.arbeidsgiver.altinnrettigheter.proxy.model.AltinnOrganisasjon
import no.nav.arbeidsgiver.altinnrettigheter.proxy.model.Fnr
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class AltinnrettigheterService(val proxyService: AltinnrettigheterProxyService) {

    private val logger = LoggerFactory.getLogger(javaClass)

    fun hentOrganisasjoner(query: Map<String, String>, fnr: Fnr): List<AltinnOrganisasjon> {
        return try {
            proxyService.hentOrganisasjonerCached(query, fnr)
        } catch (e: Exception) {
            when (e) {
                is ProxyClientErrorException,
                is AltinnException -> throw e
                else -> {
                    logger.warn("Fallback etter feil mot Redis cache, pga feil ${e.message}")
                    proxyService.hentOrganisasjonerIAltinn(query, fnr)
                }
            }
        }
    }
}
