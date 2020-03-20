package no.nav.arbeidsgiver.altinnrettigheter.proxy.service

import no.nav.arbeidsgiver.altinnrettigheter.proxy.altinn.AltinnClient
import no.nav.arbeidsgiver.altinnrettigheter.proxy.model.AltinnOrganisasjon
import no.nav.arbeidsgiver.altinnrettigheter.proxy.model.Fnr
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

@Service
class AltinnrettigheterProxyService(val altinnClient: AltinnClient) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Cacheable("reportees")
    fun hentOrganisasjoner(fnr: String): List<AltinnOrganisasjon> {
        logger.info("Kall til Altinn")
        return altinnClient.hentOrgnumreDerBrukerHarEnkeltrettighetTilIAWeb(Fnr(fnr))
    }

}