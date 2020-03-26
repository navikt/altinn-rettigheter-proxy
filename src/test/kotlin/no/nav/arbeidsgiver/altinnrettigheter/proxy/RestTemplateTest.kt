package no.nav.arbeidsgiver.altinnrettigheter.proxy

import no.nav.arbeidsgiver.altinnrettigheter.proxy.altinn.AltinnClient
import no.nav.arbeidsgiver.altinnrettigheter.proxy.altinn.ProxyClientErrorException
import no.nav.arbeidsgiver.altinnrettigheter.proxy.model.Fnr
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
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
class RestTemplateTest {

    @Autowired
    private lateinit var server: MockRestServiceServer

    @Autowired
    private lateinit var klient: AltinnClient


    @Test(expected = ProxyClientErrorException::class)
    fun altinnKlient_hentOrganisasjoner_kaster_AltinnHttpClientErrorException_dersom_Altinn_returnerer_400() {

        Assert.assertNotNull(klient)
        Assert.assertNotNull(server)


        server.expect(
                ExpectedCount.once(),
                requestTo(
                        "http://local.test/ekstern/altinn/api/serviceowner/reportees" +
                                "?ForceEIAuthentication" +
                                "&serviceCode=9999" +
                                "&serviceEdition=1"
                )
        )
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST))

        klient.hentOrganisasjoner(
                mapOf(
                        "ForceEIAuthentication" to "",
                        "serviceCode" to "9999",
                        "serviceEdition" to "1"
                ),
                Fnr("01065500791")
        )
        server.verify()
    }
}
