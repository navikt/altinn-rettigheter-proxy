package no.nav.arbeidsgiver.altinnrettigheter.proxy

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.net.HttpHeaders
import no.nav.arbeidsgiver.altinnrettigheter.proxy.model.AltinnOrganisasjon
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


    /*
      Tester på endepunkt: /organisasjoner
     */

    companion object {
        val objectMapper = ObjectMapper()
    }

    @Test
    fun `Endepunkt _organisasjoner_ returnerer en liste av organisasjoner innlogget bruker har rettigheter i`() {
        val response = HttpClient.newBuilder().build().send(
                HttpRequest.newBuilder()
                        .uri(
                                URI.create(
                                        "http://localhost:$port" +
                                                "/altinn-rettigheter-proxy/organisasjoner" +
                                                "?serviceCode=3403" +
                                                "&serviceEdition=1"
                                )
                        )
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer " + JwtTokenGenerator.signedJWTAsString("01065500791")
                        )
                        .header("X-Correlation-ID", "klient-applikasjon")
                        .GET()
                        .build(),
                BodyHandlers.ofString()
        )

        Assertions.assertThat(response.statusCode()).isEqualTo(200)
        val altinnOrganisasjoner: List<AltinnOrganisasjon> = objectMapper.readValue(
                response.body(),
                objectMapper.typeFactory.constructCollectionType(
                        MutableList::class.java,
                        AltinnOrganisasjon::class.java
                )
        )
        Assertions.assertThat(altinnOrganisasjoner.size).isEqualTo(10)
    }

    @Test
    fun `Endepunkt organisasjoner returnerer en liste av organisasjoner i flere kall til Altinn`() {
        val response = HttpClient.newBuilder().build().send(
                HttpRequest.newBuilder()
                        .uri(
                                URI.create(
                                        "http://localhost:$port" +
                                                "/altinn-rettigheter-proxy/organisasjoner" +
                                                "?serviceCode=3403" +
                                                "&serviceEdition=1" +
                                                "&inkluderAlle=true"
                                )
                        )
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer " + JwtTokenGenerator.signedJWTAsString("01065500791")
                        )
                        .header("X-Correlation-ID", "klient-applikasjon")
                        .GET()
                        .build(),
                BodyHandlers.ofString()
        )

        Assertions.assertThat(response.statusCode()).isEqualTo(200)
        val altinnOrganisasjoner: List<AltinnOrganisasjon> = objectMapper.readValue(
                response.body(),
                objectMapper.typeFactory.constructCollectionType(
                        MutableList::class.java,
                        AltinnOrganisasjon::class.java
                )
        )
        Assertions.assertThat(altinnOrganisasjoner.size).isEqualTo(9)
    }

    @Test
    fun `Endepunkt _organisasjoner_ krever AUTH header med gyldig token`() {
        val response = HttpClient.newBuilder().build().send(
                HttpRequest.newBuilder()
                        .uri(
                                URI.create(
                                        "http://localhost:$port" +
                                                "/altinn-rettigheter-proxy/organisasjoner" +
                                                "?serviceCode=3403" +
                                                "&serviceEdition=1"
                                )
                        )
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer " + "DETTE_ER_IKKE_EN_GYLDIG_TOKEN"
                        )
                        .GET()
                        .build(),
                BodyHandlers.ofString()
        )

        Assertions.assertThat(response.statusCode()).isEqualTo(401)
        Assertions.assertThat(response.body()).isEqualTo("{\"message\":\"You are not authorized to access this ressource\"}")
    }

    @Test
    fun `Endepunkt _organisasjoner_ trenger serviceCode og serviceEdition`() {
        val response = HttpClient.newBuilder().build().send(
                HttpRequest.newBuilder()
                        .uri(
                                URI.create(
                                        "http://localhost:$port" +
                                                "/altinn-rettigheter-proxy/organisasjoner" +
                                                "?serviceCode=3403"
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
    }


    /*
      Tester på endepunkt: /ekstern/altinn/api/serviceowner/reportees
     */

    @Test
    fun `Request med gyldig token får et svar`() {

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
                                                "?serviceCode=3403" +
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
        Assertions.assertThat(response.body()).isEqualTo(
                "{\"message\":\"400 BAD_REQUEST \\\"Obligatoriske parametre ble ikke sendt med: [ForceEIAuthentication]\\\"\"}")
    }
}
