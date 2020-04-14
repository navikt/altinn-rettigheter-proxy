package no.nav.arbeidsgiver.altinnrettigheter.proxy.altinn

import no.nav.arbeidsgiver.altinnrettigheter.proxy.model.Fnr
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.client.ExpectedCount
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.method
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators.withStatus


@RunWith(SpringRunner::class)
@ActiveProfiles("test")
@RestClientTest(AltinnClient::class)
class AltinnKlientRestTemplateTest {

    @Autowired
    private lateinit var server: MockRestServiceServer

    @Autowired
    private lateinit var klient: AltinnClient


    @Test
    fun altinnKlient_hentOrganisasjoner_sender_kall_til_Altinn_med_riktige_parametre() {

        server.expect(
                ExpectedCount.once(),
                requestTo(
                        "http://local.test/ekstern/altinn/api/serviceowner/reportees" +
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
    fun altinnKlient_hentOrganisasjoner_kaster_ProxyHttpStatusCodeException_med_HttpStatus_400_dersom_Altinn_returnerer_400() {

        server.expect(
                ExpectedCount.once(),
                requestTo(
                        "http://local.test/ekstern/altinn/api/serviceowner/reportees" +
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
    fun altinnKlient_hentOrganisasjoner_kaster_ProxyHttpStatusCodeException_med_HttpStatus_502_dersom_ApiGw_returnerer_502() {
        server.expect(
                ExpectedCount.once(),
                requestTo(
                        "http://local.test/ekstern/altinn/api/serviceowner/reportees" +
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
}
