package no.nav.arbeidsgiver.altinnrettigheter.proxy

import com.google.common.net.HttpHeaders
import no.nav.security.oidc.test.support.JwtTokenGenerator
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.test.context.TestPropertySource
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse.BodyHandlers


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = ["wiremock.mock.port=8083"])
//@ActiveProfiles("test")
class ApiTest {

    @LocalServerPort
    lateinit var port: String


    @AfterEach
    internal fun tearDown() {
        //stopRedisMocked() : ref https://github.com/navikt/sosialhjelp-login-api/blob/94594bc49f561bff6a03e5d67e02047df97e9e34/src/main/kotlin/no/nav/sbl/sosialhjelp/login/api/redis/RedisMockUtil.kt
    }

    @Test
    fun `Request med gyldig token og fnr som matcher subject får et svar`() {

        val response = HttpClient.newBuilder().build().send(
                HttpRequest.newBuilder()
                        .uri(
                                URI.create(
                                        "http://localhost:$port" +
                                                "/altinn-rettigheter-proxy/ekstern/altinn/api/serviceowner/reportees" +
                                                "?ForceEIAuthentication" +
                                                "&subject=01065500791" +
                                                "&serviceCode=3403" +
                                                "&serviceEdition=1"
                                )
                        )
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer " + JwtTokenGenerator.signedJWTAsString("01065500791")
                        )
                        .GET()
                        .build(),
                BodyHandlers.ofString()
        )

        Assertions.assertThat(response.statusCode()).isEqualTo(200)
    }
}