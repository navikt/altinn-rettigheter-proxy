package no.nav.arbeidsgiver.altinnrettigheter.proxy.tilgangskontroll

import no.nav.arbeidsgiver.altinnrettigheter.proxy.model.Fnr
import no.nav.security.token.support.core.context.TokenValidationContext
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import no.nav.security.token.support.core.jwt.JwtTokenClaims
import org.springframework.stereotype.Service

@Service
class TilgangskontrollService(
    private val tokenValidationcontextHolder: TokenValidationContextHolder
) {
    companion object {
        private const val ISSUER_SELVBETJENING = "loginservice"
        private const val ISSUER_TOKENX = "tokenx"
    }

    fun hentInnloggetBruker(): InnloggetBruker {
        val context = tokenValidationcontextHolder.tokenValidationContext

        context.getClaimsFor(ISSUER_SELVBETJENING)?.let { claims ->
            return InnloggetBruker(
                fnr = Fnr(claims.subject)
            )
        }

        context.getClaimsFor(ISSUER_TOKENX)?.let { claims ->
            return InnloggetBruker(
                fnr = Fnr(claims.getStringClaim("pid"))
            )
        }

        throw TilgangskontrollException("Finner ikke token")
    }

    fun nameOfAppCallingUs(): String? =
        tokenValidationcontextHolder
            .tokenValidationContext
            .getClaimsFor(ISSUER_TOKENX)
            ?.getStringClaim("client_id")

    private fun TokenValidationContext.getClaimsFor(issuer: String): JwtTokenClaims? {
        return if (hasTokenFor(issuer)) {
            getClaims(issuer)
        } else {
            null
        }
    }
}