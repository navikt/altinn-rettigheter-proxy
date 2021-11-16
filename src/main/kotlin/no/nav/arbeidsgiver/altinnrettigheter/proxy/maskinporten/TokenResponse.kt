package no.nav.arbeidsgiver.altinnrettigheter.proxy.maskinporten

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.nimbusds.jwt.JWTParser
import java.time.Duration
import java.time.Instant.now
import java.util.*

/**
 * {
 *   "access_token" : "IxC0B76vlWl3fiQhAwZUmD0hr_PPwC9hSIXRdoUslPU=",
 *   "token_type" : "Bearer",
 *   "expires_in" : 599,
 *   "scope" : "difitest:test1"
 * }
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class TokenResponse(
    @JsonProperty("access_token") val accessToken: String,
    @JsonProperty("token_type") val tokenType: String,
    @JsonProperty("expires_in") val expiresIn: Long,
    @JsonProperty("scope") val scope: String,
) {
    val jwt = JWTParser.parse(accessToken)
    fun isValid(): Boolean = expiresIn() > Duration.ofMinutes(5)
    fun expiresIn() : Duration = Duration.ofMillis(jwt.jwtClaimsSet.expirationTime.time - Date().time)
}

