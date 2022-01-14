package no.nav.arbeidsgiver.altinnrettigheter.proxy.maskinporten

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.Duration
import java.time.Instant

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
    @JsonProperty("expires_in") val expiresInSeconds: Long,
    @JsonProperty("scope") val scope: String,
) {
    val expiresIn: Duration = Duration.ofSeconds(expiresInSeconds)
}

data class TokenResponseWrapper(
    val requestedAt: Instant,
    val tokenResponse: TokenResponse,
) {
    private val expiresAt = requestedAt + tokenResponse.expiresIn
    fun expiresIn(now: Instant = Instant.now()): Duration = Duration.between(now, expiresAt)

    fun timeToRefresh(now: Instant = Instant.now()): Double {
        val brøk = tokenResponse.expiresIn.dividedBy(expiresIn(now))
        return if(brøk>0.0){
            100.0 / brøk
        } else 0.0
    }
}

