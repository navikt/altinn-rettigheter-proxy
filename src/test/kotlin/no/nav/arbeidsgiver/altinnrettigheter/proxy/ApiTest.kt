package no.nav.arbeidsgiver.altinnrettigheter.proxy

import com.google.common.net.HttpHeaders
import no.nav.security.oidc.test.support.JwtTokenGenerator
import org.assertj.core.api.Assertions
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
class ApiTest {

    @LocalServerPort
    lateinit var port: String


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

    @Test
    fun `Bad request med saklig format`() {

        val response = HttpClient.newBuilder().build().send(
                HttpRequest.newBuilder()
                        .uri(
                                URI.create(
                                        "http://localhost:$port" +
                                                "/altinn-rettigheter-proxy/ekstern/altinn/api/serviceowner/reportees" +
                                                "?ForceEIAuthentication" +
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

        Assertions.assertThat(response.statusCode()).isEqualTo(400)
        Assertions.assertThat(response.body()).isEqualTo("{\"message\":\"400 BAD_REQUEST \\\"Obligatoriske parametre ble ikke sendt med: [subject]\\\"\"}")
    }
}
