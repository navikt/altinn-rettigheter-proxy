package no.nav.arbeidsgiver.altinnrettigheter.proxy.service

import no.nav.arbeidsgiver.altinnrettigheter.proxy.altinn.AltinnClient
import no.nav.arbeidsgiver.altinnrettigheter.proxy.config.CachingConfig.Companion.REPORTEES_CACHE
import no.nav.arbeidsgiver.altinnrettigheter.proxy.model.AltinnOrganisasjon
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

@Service
class AltinnrettigheterProxyService(val altinnClient: AltinnClient) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Cacheable(REPORTEES_CACHE)
    fun hentOrganisasjoner(query: Map<String, String>): List<AltinnOrganisasjon> {
        logger.info("Kall til Altinn")
        return altinnClient.hentOrgnumre(query)
    }

}
