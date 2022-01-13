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

    @Suppress("RegExpUnexpectedAnchor", "RegExpRepeatedSpace")
    companion object {
        private const val ISSUER_SELVBETJENING = "loginservice"
        private const val ISSUER_TOKENX = "tokenx"

        val idportenIssuer = Regex(
            option = RegexOption.COMMENTS,
            pattern = """
                ^ https://oidc .* difi .* \.no/idporten-oidc-provider/ $
            """)

        val loginserviceIssuer = Regex(
            option = RegexOption.COMMENTS,
            pattern = """
                ^ https://nav (no | test) b2c\.b2clogin\.com/ .* $
             """
        )
    }


    fun hentInnloggetBruker(): InnloggetBruker {
        val context = tokenValidationcontextHolder.tokenValidationContext

        context.getClaimsFor(ISSUER_SELVBETJENING)?.let { claims ->
            return InnloggetBruker(
                fnr = Fnr(claims.subject)
            )
        }

        context.getClaimsFor(ISSUER_TOKENX)?.let { claims ->
                val fnr = claims.getTokenXFnr()
                return InnloggetBruker(
                    fnr = Fnr(fnr)
                )
            }

        throw TilgangskontrollException("Finner ikke token")
    }

    fun nameOfAppCallingUs(): String? =
        tokenValidationcontextHolder
            .tokenValidationContext
            .getClaimsFor(ISSUER_TOKENX)
            ?.getStringClaim("client_id")

    private fun JwtTokenClaims.getTokenXFnr(): String {
        /* NOTE: This is not validation of original issuer. We trust TokenX to only issue
         * tokens from trustworthy sources. The purpose is simply to differentiate different
         * original issuers to extract the fnr. */
        val idp = this.getStringClaim("idp")
        return when {
            idp.matches(idportenIssuer) -> this.getStringClaim("pid")
            idp.matches(loginserviceIssuer) -> this.subject
            else -> throw TilgangskontrollException("Ukjent idp fra tokendings")
        }
    }

    private fun TokenValidationContext.getClaimsFor(issuer: String): JwtTokenClaims? {
        return if (hasTokenFor(issuer)) {
            getClaims(issuer)
        } else {
            null
        }
    }
}