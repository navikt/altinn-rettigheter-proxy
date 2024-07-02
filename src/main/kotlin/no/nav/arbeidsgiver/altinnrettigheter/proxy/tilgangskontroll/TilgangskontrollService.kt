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

    private fun getStringClaim(claim: String): String {
        val token = try {
            tokenValidationcontextHolder
                .getTokenValidationContext()
                .getClaims(ISSUER_TOKENX)
        } catch (e: Exception) {
            throw TilgangskontrollException("Finner ikke token")
        }

        return try {
            token.getStringClaim(claim)
        } catch (e: Exception) {
            throw TilgangskontrollException("Finner ikke claim $claim i token")
        }
    }

    fun hentInnloggetBruker(): InnloggetBruker {
        val fnr = Fnr(getStringClaim("pid"))
        return InnloggetBruker(fnr = fnr)
    }

    fun nameOfAppCallingUs(): String? = try {
        getStringClaim("client_id")
    } catch (e: TilgangskontrollException) {
        null
    }
}