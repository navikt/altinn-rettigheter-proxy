package no.nav.arbeidsgiver.altinnrettigheter.proxy

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.arbeidsgiver.altinnrettigheter.proxy.model.AltinnOrganisasjon
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import org.assertj.core.api.Assertions
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.net.http.HttpResponse.BodyHandlers
import java.net.http.HttpResponse.BodySubscribers
import java.nio.charset.Charset


@RunWith(SpringRunner::class)
@ActiveProfiles("local")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = ["wiremock.mock.port=8083"])
@EnableMockOAuth2Server
class ApiTest {

    @LocalServerPort
    lateinit var port: String

    @Autowired
    lateinit var testTokenUtil: TestTokenUtil

    /*
      Tester på endepunkt: /organisasjoner
     */

    @Test
    fun `Endepunkt _organisasjoner_ returnerer en liste av organisasjoner innlogget bruker har rettigheter i`() {
        val response = HttpClient.newBuilder().build().send(
            HttpRequest.newBuilder()
                .uri(URI("http://localhost:$port/altinn-rettigheter-proxy/organisasjoner?serviceCode=3403&serviceEdition=1"))
                .header(AUTHORIZATION, "Bearer ${testTokenUtil.createToken(issuerId = "tokenx", pid = "01065500791")}")
                .header("X-Correlation-ID", "klient-applikasjon")
                .GET()
                .build(),
            BodyHandlers.ofString()
        )

        Assertions.assertThat(response.statusCode()).isEqualTo(200)
        assertAntallOrganisasjonerEr(response, 4)
    }

    @Test
    fun `Endepunkt _organisasjoner_ returnerer en liste av organisasjoner innlogget bruker har rettigheter i (tokenx, idp loginservice)`() {
        val response = HttpClient.newBuilder().build().send(
            HttpRequest.newBuilder()
                .uri(URI("http://localhost:$port/altinn-rettigheter-proxy/organisasjoner?serviceCode=3403&serviceEdition=1"))
                .header(AUTHORIZATION,
                    "Bearer " + testTokenUtil.createToken(
                        issuerId = "tokenx",
                        sub = "01065500791",
                        pid = "01065500791",
                        idp = "https://xyz.com/1234"
                    )
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
    fun `Endepunkt _organisasjoner_ returnerer en liste av organisasjoner innlogget bruker har rettigheter i (tokenx, idp idporten)`() {
        val response = HttpClient.newBuilder().build().send(
            HttpRequest.newBuilder()
                .uri(URI("http://localhost:$port/altinn-rettigheter-proxy/organisasjoner?serviceCode=3403&serviceEdition=1"))
                .header(
                    AUTHORIZATION,
                    "Bearer " + testTokenUtil.createToken(
                        issuerId = "tokenx",
                        sub = "dette-er-ikke-et-fnr",
                        idp = "https://oidc.difi.no/idporten-oidc-provider/",
                        pid = "01065500791",
                    )
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
                .uri(URI("http://localhost:$port/altinn-rettigheter-proxy/organisasjoner?serviceCode=3403&serviceEdition=1"))
                .header(AUTHORIZATION, "Bearer DETTE_ER_IKKE_EN_GYLDIG_TOKEN")
                .GET()
                .build(),
            JsonBodyHandler.of<FeilRespons>()
        )


        Assertions.assertThat(response.statusCode()).isEqualTo(401)
        Assertions.assertThat(response.body().message).isEqualTo("You are not authorized to access this resource")
    }

    @Test
    fun `Endepunkt _organisasjoner_ trenger serviceCode og serviceEdition`() {
        val response = HttpClient.newBuilder().build().send(
            HttpRequest.newBuilder()
                .uri(URI("http://localhost:$port/altinn-rettigheter-proxy/organisasjoner?serviceCode=3403"))
                .header(AUTHORIZATION, "Bearer ${testTokenUtil.createToken("01065500791")}")
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
                .uri(URI("http://localhost:$port/altinn-rettigheter-proxy/v2/organisasjoner?serviceCode=malicious%20serviceCode&serviceEdition=1&top=500&skip=0"))
                .header(AUTHORIZATION, "Bearer ${testTokenUtil.createToken("01065500791")}")
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
                .uri(URI("http://localhost:$port/altinn-rettigheter-proxy/v2/organisasjoner?serviceCode=3403&serviceEdition=1&top=500&skip=0"))
                .header(AUTHORIZATION, "Bearer ${testTokenUtil.createToken("01065500791")}")
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
                .uri(URI("http://localhost:$port/altinn-rettigheter-proxy/v2/organisasjoner?serviceCode=3403&serviceEdition=1&top=500&skip=0&filterPaaAktiveOrganisasjoner=false"))
                .header(AUTHORIZATION, "Bearer ${testTokenUtil.createToken("01065500791")}")
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
                .uri(URI("http://localhost:$port/altinn-rettigheter-proxy/v2/organisasjoner?top=500&skip=0"))
                .header(AUTHORIZATION, "Bearer ${testTokenUtil.createToken("01020300123")}")
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
                .uri(URI("http://localhost:$port/altinn-rettigheter-proxy/ekstern/altinn/api/serviceowner/reportees?ForceEIAuthentication=&serviceCode=3403&serviceEdition=1&\$filter=Type+ne+'Person'+and+Status+eq+'Active'&\$top=500&\$skip=0"))
                .header(AUTHORIZATION, "Bearer ${testTokenUtil.createToken("01065500791")}")
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
                .uri(URI("http://localhost:$port/altinn-rettigheter-proxy/ekstern/altinn/api/serviceowner/reportees?serviceCode=3403&serviceEdition=1"))
                .header(AUTHORIZATION, "Bearer ${testTokenUtil.createToken("01065500791")}")
                .GET()
                .build(),
            JsonBodyHandler.of<FeilRespons>()
        )

        Assertions.assertThat(response.statusCode()).isEqualTo(400)
        Assertions.assertThat(response.body().message).isEqualTo("Obligatoriske parametre ble ikke sendt med: [ForceEIAuthentication]")
    }


    private fun assertAntallOrganisasjonerEr(response: HttpResponse<String>, expectedAntallBedrifter: Int) {
        val reportees: List<AltinnOrganisasjon> = jacksonObjectMapper().readValue(response.body())
        Assertions.assertThat(reportees.size).isEqualTo(expectedAntallBedrifter)
    }


    class JsonBodyHandler<T>(val target: Class<T>) : HttpResponse.BodyHandler<T> {
        companion object {
            inline fun <reified T> of(): JsonBodyHandler<T> =
                JsonBodyHandler(T::class.java)
        }

        val objectMapper = jacksonObjectMapper()

        override fun apply(responseInfo: HttpResponse.ResponseInfo?): HttpResponse.BodySubscriber<T> =
            /* Read whole string, as the body may be chuncked (happens some times, but not always), and
             * `objectMapper.readValue` doesn't handle chunked streams, apparently.
             * Supposed to prevent this error:
             * error: java.io.IOException: chunked transfer encoding, state: READING_LENGTH. */
            BodySubscribers.mapping(BodySubscribers.ofString(Charsets.UTF_8)) { json ->
                objectMapper.readValue(json, target)
            }
    }
}
