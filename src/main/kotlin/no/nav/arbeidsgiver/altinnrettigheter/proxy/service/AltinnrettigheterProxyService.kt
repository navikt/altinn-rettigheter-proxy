package no.nav.arbeidsgiver.altinnrettigheter.proxy.service

import no.nav.arbeidsgiver.altinnrettigheter.proxy.altinn.AltinnClient
import no.nav.arbeidsgiver.altinnrettigheter.proxy.model.AltinnOrganisasjon
import no.nav.arbeidsgiver.altinnrettigheter.proxy.model.AltinnRolle
import no.nav.arbeidsgiver.altinnrettigheter.proxy.model.Fnr
import org.springframework.stereotype.Service

@Service
class AltinnrettigheterProxyService(val altinnClient: AltinnClient) {
    fun hentRoller(fnr: String): AltinnRolle {

        return altinnClient.hentRoller();
    }

    fun hentOrganisasjoner(fnr: String): List<AltinnOrganisasjon> {
        return altinnClient.hentOrgnumreDerBrukerHarEnkeltrettighetTilIAWeb(Fnr(fnr))
    }

}