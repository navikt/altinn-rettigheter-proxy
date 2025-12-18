package no.nav.arbeidsgiver.altinnrettigheter.proxy.altinn

import io.micrometer.core.instrument.MeterRegistry
import no.nav.arbeidsgiver.altinnrettigheter.proxy.maskinporten.MaskinportenClientStub
import no.nav.arbeidsgiver.altinnrettigheter.proxy.maskinporten.MaskinportenTokenService
import no.nav.arbeidsgiver.altinnrettigheter.proxy.model.Fnr
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.*
import org.springframework.http.client.ClientHttpRequest
import org.springframework.http.client.ClientHttpResponse
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.client.ExpectedCount
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.method
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators.withStatus
import java.io.InputStream


@RunWith(SpringRunner::class)
@ActiveProfiles("test")
@RestClientTest(AltinnClient::class, MaskinportenTokenService::class, MaskinportenClientStub::class)
@EnableMockOAuth2Server
class AltinnKlientRestTemplateTest {

    @MockBean
    lateinit var meterRegistry: MeterRegistry

    @Autowired
    private lateinit var server: MockRestServiceServer

    @Autowired
    private lateinit var klient: AltinnClient


    @Test
    fun altinnKlient_hentOrganisasjoner_sender_kall_til_Altinn_med_riktige_parametre() {

        server.expect(
                ExpectedCount.once(),
                requestTo(
                        "http://local.test/api/serviceowner/reportees" +
                                "?ForceEIAuthentication" +
                                "&serviceCode=3403" +
                                "&serviceEdition=1" +
                                "&subject=01065500791"
                )
        )
                .andExpect(method(HttpMethod.GET))
                .andRespond(
                        withStatus(HttpStatus.OK)
                                .contentType(MediaType.APPLICATION_JSON)
                                .body("[]")
                )

        klient.hentOrganisasjoner(
                mapOf(
                        "ForceEIAuthentication" to "",
                        "serviceCode" to "3403",
                        "serviceEdition" to "1"
                ),
                Fnr("01065500791")
        )
        server.verify()
    }


    @Test
    fun altinnKlient_hentOrganisasjoner_returnerer_tom_liste_dersom_Altinn_returnerer_400() {

        server.expect(
                ExpectedCount.once(),
                requestTo(
                        "http://local.test/api/serviceowner/reportees" +
                                "?ForceEIAuthentication" +
                                "&serviceCode=9999" +
                                "&serviceEdition=1" +
                                "&subject=01065500791"
                )
        )
            .andExpect(method(HttpMethod.GET))
            .andRespond(
                withStatus(
                    400,
                    "User profile could not be found for ***********. User profile is created at first login to the Altinn.no portal"
                )
            )

        val organisasjoner = klient.hentOrganisasjoner(
            mapOf(
                "ForceEIAuthentication" to "",
                "serviceCode" to "9999",
                "serviceEdition" to "1"
            ),
            Fnr("01065500791")
        )
        assertThat(organisasjoner).isEmpty()
    }

    @Test
    fun altinnKlient_hentOrganisasjoner_kaster_ProxyHttpStatusCodeException_med_HttpStatus_400_dersom_Altinn_returnerer_400() {
        server.expect(
            ExpectedCount.once(),
            requestTo(
                "http://local.test/api/serviceowner/reportees" +
                        "?ForceEIAuthentication" +
                        "&serviceCode=9999" +
                        "&serviceEdition=1" +
                        "&subject=01065500791"
            )
        )
            .andExpect(method(HttpMethod.GET))
            .andRespond(withStatus(HttpStatus.BAD_REQUEST))

        try {
            klient.hentOrganisasjoner(
                mapOf(
                    "ForceEIAuthentication" to "",
                    "serviceCode" to "9999",
                    "serviceEdition" to "1"
                ),
                Fnr("01065500791")
            )
        } catch (e: ProxyHttpStatusCodeException) {
            assertThat(e.httpStatus).isEqualTo(HttpStatus.BAD_REQUEST)
        }
    }

    @Test
    fun altinnKlient_hentOrganisasjoner_kaster_ProxyHttpStatusCodeException_med_HttpStatus_502_dersom_Api_returnerer_502() {
        server.expect(
                ExpectedCount.once(),
                requestTo(
                        "http://local.test/api/serviceowner/reportees" +
                                "?ForceEIAuthentication" +
                                "&serviceCode=3403" +
                                "&serviceEdition=1" +
                                "&subject=01065500791"
                )
        )
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.BAD_GATEWAY))

        try {
            klient.hentOrganisasjoner(
                    mapOf(
                            "ForceEIAuthentication" to "",
                            "serviceCode" to "3403",
                            "serviceEdition" to "1"
                    ),
                    Fnr("01065500791")
            )
        } catch (e: ProxyHttpStatusCodeException) {
            assertThat(e.httpStatus).isEqualTo(HttpStatus.BAD_GATEWAY)
        }
    }

    @Suppress("SameParameterValue")
    private fun withStatus(
        httpStatus: Int,
        statusText: String
    ): (request: ClientHttpRequest?) -> ClientHttpResponse =
        {
            object : ClientHttpResponse {
                override fun getHeaders(): HttpHeaders = HttpHeaders()
                override fun getBody(): InputStream = InputStream.nullInputStream()
                override fun close() = Unit
                override fun getStatusCode(): HttpStatusCode = HttpStatusCode.valueOf(httpStatus)
                override fun getStatusText(): String = statusText
                @Deprecated("Deprecated in Java", ReplaceWith("httpStatus"))
                override fun getRawStatusCode(): Int = httpStatus
            }
        }
}
