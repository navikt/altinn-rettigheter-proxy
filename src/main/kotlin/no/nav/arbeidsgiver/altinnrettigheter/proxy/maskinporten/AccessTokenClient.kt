package no.nav.arbeidsgiver.altinnrettigheter.proxy.maskinporten

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import no.nav.arbeidsgiver.altinnrettigheter.proxy.basedOnEnv
import org.slf4j.LoggerFactory
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.http.RequestEntity
import org.springframework.stereotype.Component
import java.time.Duration
import java.util.*
import java.util.concurrent.atomic.AtomicReference

@Component
class AccessTokenClient(
    val config: MaskinportenConfig,
    restTemplateBuilder: RestTemplateBuilder,
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val restTemplate = restTemplateBuilder.build()

    private val wellKnownResponse: WellKnownResponse by lazy { // TODO: no lazy!
        restTemplate.getForObject(config.wellKnownUrl, WellKnownResponse::class.java)!!
    }

    fun createClientAssertion(): String {
        val claimsSet: JWTClaimsSet = JWTClaimsSet.Builder()
            .audience(wellKnownResponse.issuer)
            .issuer(config.clientId)
            .issueTime(Date())
            .expirationTime(Date(Date().time + 120 * 1000))
            .notBeforeTime(Date())
//            .subject(config.clientId) // kan ikke se denne nevnt i doken her https://docs.digdir.no/maskinporten_protocol_jwtgrant.html eller her https://altinn.github.io/docs/api/rest/kom-i-gang/virksomhet/#autentisering-med-virksomhetsbruker-og-maskinporten
            .claim("resource", basedOnEnv(prod = {"https://www.altinn.no/"}, other = {"https://tt02.altinn.no/"}))
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
        logger.info("henter ny accesstoken")
        return restTemplate.exchange(RequestEntity
            .method(HttpMethod.POST, wellKnownResponse.tokenEndpoint)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(mapOf(
                "grant_type" to "urn:ietf:params:oauth:grant-type:jwt-bearer",
                "assertion" to createClientAssertion()
            )), TokenResponse::class.java).body!!
    }

    private val token = AtomicReference<TokenResponse?>()

    fun getAccessToken(): TokenResponse {
        val value = token.get()
        return if (value != null && value.isValid()) {
            value
        } else {
            fetchNewAccessToken().also {
                token.set(it)
            }
        }
    }

    init {
        Thread {
            while (true) {
                logger.info("sjekker om accesstoken er i ferd med å utløpe..")
                val value = token.get()
                if (value != null && value.expiresIn() < Duration.ofMinutes(2)) {
                   getAccessToken()
                }
                Thread.sleep(Duration.ofMinutes(1).toMillis())
            }
        }

    }
}