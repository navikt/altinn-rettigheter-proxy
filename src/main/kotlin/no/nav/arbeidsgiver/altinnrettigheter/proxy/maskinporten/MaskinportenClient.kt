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
                if (value != null && value.expiresIn() < Duration.ofMinutes(2)) {
                    getAccessToken()
                }
                Thread.sleep(Duration.ofMinutes(1).toMillis())
            }
        }
    }


    fun createClientAssertion(): String {
        val claimsSet: JWTClaimsSet = JWTClaimsSet.Builder()
            .audience(wellKnownResponse.issuer)
            .issuer(config.clientId)
            .issueTime(Date())
            .expirationTime(Date(Date().time + 120 * 1000))
            .notBeforeTime(Date())
//            .subject(config.clientId) // kan ikke se denne nevnt i doken her https://docs.digdir.no/maskinporten_protocol_jwtgrant.html eller her https://altinn.github.io/docs/api/rest/kom-i-gang/virksomhet/#autentisering-med-virksomhetsbruker-og-maskinporten
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
