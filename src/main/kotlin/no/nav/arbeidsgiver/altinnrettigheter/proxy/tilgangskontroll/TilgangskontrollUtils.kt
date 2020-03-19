package no.nav.arbeidsgiver.altinnrettigheter.proxy.tilgangskontroll

import com.nimbusds.jwt.JWTClaimsSet
import no.nav.arbeidsgiver.altinnrettigheter.proxy.model.Fnr
import no.nav.security.oidc.context.OIDCRequestContextHolder
import org.springframework.stereotype.Component
import java.util.*


@Component
class TilgangskontrollUtils(private val oidcRequestContextHolder: OIDCRequestContextHolder) {

    private val ISSUER_SELVBETJENING = "selvbetjening"

    fun hentInnloggetSelvbetjeningBruker(): InnloggetBruker? {
        val fnr = hentClaim(ISSUER_SELVBETJENING, "sub")
                .orElseThrow<RuntimeException> { TilgangskontrollException("Finner ikke fodselsnummer til bruker.") }
        return InnloggetBruker(Fnr(fnr ?: throw TilgangskontrollException("Finner ikke fodselsnummer til bruker.")))
    }

    fun hentClaim(issuer: String, claim: String): Optional<String> {
        val claimSet: Optional<JWTClaimsSet> = hentClaimSet(issuer)
        return claimSet.map { jwtClaimsSet: JWTClaimsSet -> java.lang.String.valueOf(jwtClaimsSet.getClaim(claim)) }
    }

    fun hentClaimSet(issuer: String): Optional<JWTClaimsSet> {
        return Optional.ofNullable(oidcRequestContextHolder.oidcValidationContext.getClaims(issuer))
                .map { claims -> claims.claimSet }
    }

    fun erInnloggetSelvbetjeningBruker(): Boolean {
        return hentClaim(ISSUER_SELVBETJENING, "sub")
                .map<Any> {
                    fnrString: String -> Fnr.erGyldigFnr(fnrString)
                }
                .orElse(false) as Boolean
    }
}