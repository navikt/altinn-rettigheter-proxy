package no.nav.arbeidsgiver.altinnrettigheter.proxy.service

import no.nav.arbeidsgiver.altinnrettigheter.proxy.altinn.AltinnClient
import no.nav.arbeidsgiver.altinnrettigheter.proxy.config.CachingConfig.Companion.REPORTEES_CACHE
import no.nav.arbeidsgiver.altinnrettigheter.proxy.model.AltinnOrganisasjon
import no.nav.arbeidsgiver.altinnrettigheter.proxy.model.Fnr
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

@Service
class AltinnrettigheterProxyService(val altinnClient: AltinnClient) {
    @Cacheable(REPORTEES_CACHE)
    fun hentOrganisasjonerCached(query: Map<String, String>, fnr: Fnr): List<AltinnOrganisasjon> {
        return hentOrganisasjonerIAltinn(query, fnr)
    }

    fun hentOrganisasjonerIAltinn(query: Map<String, String>, fnr: Fnr): List<AltinnOrganisasjon> {
        return altinnClient.hentOrganisasjoner(query, fnr)
    }
}
