package no.nav.arbeidsgiver.altinnrettigheter.proxy.maskinporten

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import no.nav.arbeidsgiver.altinnrettigheter.proxy.basedOnEnv
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.InitializingBean
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.http.RequestEntity
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import java.time.Duration
import java.time.Instant
import java.util.*
import java.util.concurrent.atomic.AtomicReference

interface MaskinportenClient {
    fun fetchAccessToken(): String
}

@Component
@Profile("dev", "prod")
class MaskinportenClientImpl(
    val config: MaskinportenConfig,
    restTemplateBuilder: RestTemplateBuilder,
): MaskinportenClient, InitializingBean {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val restTemplate = restTemplateBuilder.build()

    private lateinit var wellKnownResponse: WellKnownResponse

    override fun afterPropertiesSet() {
        wellKnownResponse = restTemplate.getForObject(config.wellKnownUrl, WellKnownResponse::class.java)!!

        Thread {
            while (true) {
                logger.info("sjekker om accesstoken er i ferd med å utløpe..")
                val value = token.get()
                if (value == null || value.expiresIn() < Duration.ofSeconds(40)) {
                    getAccessToken()
                }
                Thread.sleep(Duration.ofSeconds(30).toMillis())
            }
        }
    }

    fun createClientAssertion(): String {
        val now = Instant.now()
        val expire = now + Duration.ofSeconds(120)

        val claimsSet: JWTClaimsSet = JWTClaimsSet.Builder()
            .audience(wellKnownResponse.issuer)
            .issuer(config.clientId)
            .issueTime(Date.from(now))
            .expirationTime(Date.from(expire))
            .notBeforeTime(Date.from(now))
            .claim("scope", "altinn:serviceowner/reportees")
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
            .body(LinkedMultiValueMap(mapOf(
                "grant_type" to listOf("urn:ietf:params:oauth:grant-type:jwt-bearer"),
                "assertion" to listOf(createClientAssertion())
            ))), TokenResponse::class.java).body!!
    }

    private val token = AtomicReference<TokenResponse?>()

    fun getAccessToken(): TokenResponse {
        val value = token.get()
        return if (value != null && value.isValid()) {
            value
        } else {
            /* this shouldn't happen, as refresh loop above refreshes often */
            fetchNewAccessToken().also {
                token.set(it)
            }
        }
    }

    override fun fetchAccessToken(): String {
        return getAccessToken().accessToken
    }
}


@Component
@Profile("local", "test")
class MaskinportenClientStub: MaskinportenClient {
    override fun fetchAccessToken(): String {
        return "stub-access-token"
    }
}
