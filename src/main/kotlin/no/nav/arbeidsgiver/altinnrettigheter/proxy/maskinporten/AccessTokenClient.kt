package no.nav.arbeidsgiver.altinnrettigheter.proxy.maskinporten

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.http.RequestEntity
import org.springframework.stereotype.Component
import java.time.Duration
import java.util.*


@Component
class AccessTokenClient(
    val config: MaskinportenConfig,
    restTemplateBuilder: RestTemplateBuilder,
) {
    private val restTemplate = restTemplateBuilder.build()

    private val wellKnownResponse: WellKnownResponse by lazy {
        restTemplate.getForObject(config.wellKnownUrl, WellKnownResponse::class.java)!!
    }

    fun createClientAssertion(): String {
        val claimsSet: JWTClaimsSet = JWTClaimsSet.Builder()
            .subject(config.clientId)
            .issuer(config.clientId)
            .audience(wellKnownResponse.issuer)
            .issueTime(Date())
            .notBeforeTime(Date())
            .expirationTime(Date(Date().time + 120 * 1000))
            .jwtID(UUID.randomUUID().toString())
            .build()

        val signedJWT = SignedJWT(
            JWSHeader.Builder(JWSAlgorithm.RS256)
                .keyID(config.privateJwkRsa.keyID)
                .build(),
            claimsSet
        )
        signedJWT.sign(config.jwsSigner)
        return signedJWT.serialize()
    }

    fun fetchNewAccessToken(): TokenResponse {
        val request = RequestEntity
            .method(HttpMethod.POST, wellKnownResponse.tokenEndpoint)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(mapOf(
                "grant_type" to "urn:ietf:params:oauth:grant-type:jwt-bearer",
                "assertion" to createClientAssertion()
            ))
        return restTemplate.exchange(request, TokenResponse::class.java).body!!
    }

    private var token: TokenResponse? = null

    @Suppress("LocalVariableName")
    fun getAccessToken(): TokenResponse {
        var _token = token
        return if (_token != null && _token.isValid()) {
            _token
        } else {
            _token = fetchNewAccessToken()
            token = _token
            _token
        }
    }

    init {
        Thread {
            while (true) {
                // TODO: thread safety
                var _token = token
                if (_token != null && _token!!.expiresIn() < Duration.ofMinutes(2)) {
                   getAccessToken()
                }
                Thread.sleep(Duration.ofMinutes(1).toMillis())
            }
        }

    }
}