package no.nav.arbeidsgiver.altinnrettigheter.proxy.tilgangskontroll

import no.nav.arbeidsgiver.altinnrettigheter.proxy.model.Fnr
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import org.springframework.stereotype.Service

@Service
class TilgangskontrollService(
    private val tokenValidationcontextHolder: TokenValidationContextHolder
) {
    companion object {
        private const val ISSUER_SELVBETJENING = "loginservice"
        private const val ISSUER_TOKENX = "tokenx"
    }

    fun hentInnloggetBruker(): InnloggetBruker =
        hentInnloggetSelvbetjeningBruker()
            ?: throw TilgangskontrollException("Innlogget bruker er ikke selvbetjeningsbruker")

    private fun hentInnloggetSelvbetjeningBruker(): InnloggetBruker? {
        val fnr = hentClaim(ISSUER_SELVBETJENING, "sub")
            ?: throw TilgangskontrollException("Finner ikke fodselsnummer til bruker.")
        return InnloggetBruker(Fnr(fnr))
    }

    private fun hentClaim(issuer: String, claim: String): String? =
        tokenValidationcontextHolder
            .tokenValidationContext
            .getClaims(issuer)
            .getStringClaim(claim)

}