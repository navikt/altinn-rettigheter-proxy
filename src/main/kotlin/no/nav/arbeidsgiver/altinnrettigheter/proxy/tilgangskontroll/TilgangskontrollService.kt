package no.nav.arbeidsgiver.altinnrettigheter.proxy.tilgangskontroll

import org.springframework.stereotype.Service

@Service
class TilgangskontrollService(val tokenUtils: TilgangskontrollUtils) {

    fun hentInnloggetBruker(): InnloggetBruker {
        return if (tokenUtils.erInnloggetSelvbetjeningBruker()) {
            tokenUtils.hentInnloggetSelvbetjeningBruker()!!
        } else {
            throw TilgangskontrollException("Innlogget bruker er ikke selvbetjeningsbruker")
        }
    }
}