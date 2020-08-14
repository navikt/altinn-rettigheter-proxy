package no.nav.arbeidsgiver.altinnrettigheter.proxy

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.common.net.HttpHeaders
import no.nav.arbeidsgiver.altinnrettigheter.proxy.model.AltinnOrganisasjon
import no.nav.security.oidc.test.support.JwtTokenGenerator
import org.apache.http.client.utils.URIBuilder
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.test.context.TestPropertySource
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.net.http.HttpResponse.BodyHandlers


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = ["wiremock.mock.port=8083"])
class ApiTest {

    @LocalServerPort
    lateinit var port: String


    /*
      Tester på endepunkt: /organisasjoner
     */

    @Test
    fun `Endepunkt _organisasjoner_ returnerer en liste av organisasjoner innlogget bruker har rettigheter i`() {
        val response = HttpClient.newBuilder().build().send(
                HttpRequest.newBuilder()
                        .uri(
                                URIBuilder()
                                        .setScheme("http")
                                        .setHost("localhost:$port")
                                        .setPath("/altinn-rettigheter-proxy/organisasjoner")
                                        .addParameter("serviceCode", "3403")
                                        .addParameter("serviceEdition", "1")
                                        .build()
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
        assertAntallOrganisasjonerEr(response, 4)
    }

    @Test
    fun `Endepunkt _organisasjoner_ krever AUTH header med gyldig token`() {
        val response = HttpClient.newBuilder().build().send(
                HttpRequest.newBuilder()
                        .uri(
                                URIBuilder()
                                        .setScheme("http")
                                        .setHost("localhost:$port")
                                        .setPath("/altinn-rettigheter-proxy/organisasjoner")
                                        .addParameter("serviceCode", "3403")
                                        .addParameter("serviceEdition", "1")
                                        .build()
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
                                URIBuilder()
                                        .setScheme("http")
                                        .setHost("localhost:$port")
                                        .setPath("/altinn-rettigheter-proxy/organisasjoner")
                                        .addParameter("serviceCode", "3403")
                                        .build()
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
      Tester på endepunkt: /v2/organisasjoner
     */

    @Test
    fun `Endepunkt _organisasjonerV2_ sjekker at serviceCode og serviceEdition bare inneholder tall`() {
        val response = HttpClient.newBuilder().build().send(
                HttpRequest.newBuilder()
                        .uri(
                                URIBuilder()
                                        .setScheme("http")
                                        .setHost("localhost:$port")
                                        .setPath("/altinn-rettigheter-proxy/v2/organisasjoner")
                                        .addParameter("serviceCode", "malicious serviceCode")
                                        .addParameter("serviceEdition", "1")
                                        .addParameter("top", "500")
                                        .addParameter("skip", "0")
                                        .build()
                        )
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer " + JwtTokenGenerator.signedJWTAsString("01065500791")
                        )
                        .header("X-Correlation-ID", "cn39rh9eawhd93rh974")
                        .header("X-Consumer-ID", "klient-applikasjon")
                        .GET()
                        .build(),
                BodyHandlers.ofString()
        )

        Assertions.assertThat(response.statusCode()).isEqualTo(400)
    }

    @Test
    fun `Endepunkt _organisasjonerV2_ returnerer en liste av aktive organisasjoner innlogget bruker har rettigheter i`() {
        val response = HttpClient.newBuilder().build().send(
                HttpRequest.newBuilder()
                        .uri(
                                URIBuilder()
                                        .setScheme("http")
                                        .setHost("localhost:$port")
                                        .setPath("/altinn-rettigheter-proxy/v2/organisasjoner")
                                        .addParameter("serviceCode", "3403")
                                        .addParameter("serviceEdition", "1")
                                        .addParameter("top", "500")
                                        .addParameter("skip", "0")
                                        .build()
                        )
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer " + JwtTokenGenerator.signedJWTAsString("01065500791")
                        )
                        .header("X-Correlation-ID", "cn39rh9eawhd93rh974")
                        .header("X-Consumer-ID", "klient-applikasjon")
                        .GET()
                        .build(),
                BodyHandlers.ofString()
        )

        Assertions.assertThat(response.statusCode()).isEqualTo(200)
        assertAntallOrganisasjonerEr(response, 4)
    }

    @Test
    fun `Endepunkt _organisasjonerV2_ returnerer en liste av alle organisasjoner innlogget bruker har rettigheter i`() {
        val response = HttpClient.newBuilder().build().send(
                HttpRequest.newBuilder()
                        .uri(
                                URIBuilder()
                                        .setScheme("http")
                                        .setHost("localhost:$port")
                                        .setPath("/altinn-rettigheter-proxy/v2/organisasjoner")
                                        .addParameter("serviceCode", "3403")
                                        .addParameter("serviceEdition", "1")
                                        .addParameter("top", "500")
                                        .addParameter("skip", "0")
                                        .addParameter("filterPaaAktiveOrganisasjoner", "false")
                                        .build()
                        )
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer " + JwtTokenGenerator.signedJWTAsString("01065500791")
                        )
                        .header("X-Correlation-ID", "cn39rh9eawhd93rh974")
                        .header("X-Consumer-ID", "klient-applikasjon")
                        .GET()
                        .build(),
                BodyHandlers.ofString()
        )

        Assertions.assertThat(response.statusCode()).isEqualTo(200)
        assertAntallOrganisasjonerEr(response, 5)
    }

    @Test
    fun `Endepunkt _organisasjonerV2_ returnerer en liste av organisasjoner innlogget bruker har rettigheter i uten noen spesisfiske rettigheter`() {
        val response = HttpClient.newBuilder().build().send(
                HttpRequest.newBuilder()
                        .uri(
                                URIBuilder()
                                        .setScheme("http")
                                        .setHost("localhost:$port")
                                        .setPath("/altinn-rettigheter-proxy/v2/organisasjoner")
                                        .addParameter("top", "500")
                                        .addParameter("skip", "0")
                                        .build()
                        )
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer " + JwtTokenGenerator.signedJWTAsString("01020300123")
                        )
                        .header("X-Correlation-ID", "cn39rh9eawhd93rh974")
                        .header("X-Consumer-ID", "klient-applikasjon")
                        .GET()
                        .build(),
                BodyHandlers.ofString()
        )

        Assertions.assertThat(response.statusCode()).isEqualTo(200)
        assertAntallOrganisasjonerEr(response, 6)
    }


    /*
      Tester på endepunkt: /ekstern/altinn/api/serviceowner/reportees
     */

    @Test
    fun `Request med gyldig token får et svar, selv med + i filter`() {

        val response = HttpClient.newBuilder().build().send(
                HttpRequest.newBuilder()
                        .uri(
                                URIBuilder()
                                        .setScheme("http")
                                        .setHost("localhost:$port")
                                        .setPath("/altinn-rettigheter-proxy/ekstern/altinn/api/serviceowner/reportees")
                                        .addParameter("ForceEIAuthentication", "")
                                        .addParameter("serviceCode", "3403")
                                        .addParameter("serviceEdition", "1")
                                        .addParameter("\$filter", "Type+ne+'Person'+and+Status+eq+'Active'")
                                        .addParameter("\$top", "500")
                                        .addParameter("\$skip", "0")
                                        .build()
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
        assertAntallOrganisasjonerEr(response, 4)
    }

    @Test
    fun `Bad request med saklig format`() {

        val response = HttpClient.newBuilder().build().send(
                HttpRequest.newBuilder()
                        .uri(
                                URIBuilder()
                                        .setScheme("http")
                                        .setHost("localhost:$port")
                                        .setPath("/altinn-rettigheter-proxy/ekstern/altinn/api/serviceowner/reportees")
                                        .addParameter("serviceCode", "3403")
                                        .addParameter("serviceEdition", "1")
                                        .build()
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


    private fun assertAntallOrganisasjonerEr(response: HttpResponse<String>, expectedAntallBedrifter: Int) {
        val mapper = ObjectMapper().registerModule(KotlinModule())
        val reportees: List<AltinnOrganisasjon> = mapper.readValue(response.body())
        Assertions.assertThat(reportees.size).isEqualTo(expectedAntallBedrifter)
    }

}
