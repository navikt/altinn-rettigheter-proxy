package no.nav.arbeidsgiver.altinnrettigheter.proxy;

import no.nav.arbeidsgiver.altinnrettigheter.proxy.altinn.AltinnClient
import no.nav.arbeidsgiver.altinnrettigheter.proxy.model.Fnr
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpHeaders
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest(
    properties = [
        "server.servlet.context-path=/",
    ],
)
@ActiveProfiles("test")
@AutoConfigureMockMvc
@AutoConfigureObservability
@EnableMockOAuth2Server
class AltinnRettigheterProxyIntegrationTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var testTokenUtil: TestTokenUtil

    @MockBean
    lateinit var altinnClient: AltinnClient

    @Test
    fun `samme query treffer cache men ikke på tvers av fnr`() {
        val sub1 = Fnr("01065500791")
        val sub2 = Fnr("26903848935")
        val query = mapOf(
            "ForceEIAuthentication" to "",
            "serviceCode" to "1337",
            "serviceEdition" to "1",
            "\$filter" to "Type+ne+'Person'+and+Status+eq+'Active'",
            "\$top" to "500",
            "\$skip" to "0"
        )
        `when`(altinnClient.hentOrganisasjoner(query, sub1)).thenReturn(emptyList())
        `when`(altinnClient.hentOrganisasjoner(query, sub2)).thenReturn(emptyList())

        for (i in 1..10) {
            mockMvc
                .perform(
                    get("/organisasjoner?serviceCode={code}&serviceEdition={edition}", "1337", "1")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer ${testTokenUtil.createToken(sub1.verdi)}")
                )
                .andExpect(status().isOk)
                .andDo(print())
            mockMvc
                .perform(
                    get("/organisasjoner?serviceCode={code}&serviceEdition={edition}", "1337", "1")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer ${testTokenUtil.createToken(sub2.verdi)}")
                )
                .andExpect(status().isOk)
                .andDo(print())
        }

        verify(altinnClient, times(1)).hentOrganisasjoner(query, sub1)
        verify(altinnClient, times(1)).hentOrganisasjoner(query, sub2)
    }

    @Test
    fun `is ready`() {
        mockMvc
            .perform(
                get("/internal/ready")
            )
            .andExpect(status().isOk)
        mockMvc
            .perform(
                get("/internal/actuator/prometheus")
            )
            .andExpect(status().isOk)
            .andDo(print())
    }
}