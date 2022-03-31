package no.nav.arbeidsgiver.altinnrettigheter.proxy.tilgangskontroll

import no.nav.arbeidsgiver.altinnrettigheter.proxy.model.Fnr
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import org.springframework.stereotype.Service

@Service
class TilgangskontrollService(
    private val tokenValidationcontextHolder: TokenValidationContextHolder
) {
    companion object {
        private const val ISSUER_TOKENX = "tokenx"
    }

    fun hentInnloggetBruker(): InnloggetBruker {
        val fnr = tokenValidationcontextHolder
            .tokenValidationContext
            .anyValidClaims
            .orElseThrow { TilgangskontrollException("Finner ikke token") }
            .getStringClaim("pid")
            .let { Fnr(it) }
        return InnloggetBruker(fnr = fnr)
    }

    fun nameOfAppCallingUs(): String? =
        tokenValidationcontextHolder
            .tokenValidationContext
            .getClaims(ISSUER_TOKENX)
            ?.getStringClaim("client_id")
}