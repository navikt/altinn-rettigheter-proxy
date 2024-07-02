package no.nav.arbeidsgiver.altinnrettigheter.proxy.controller;

import no.nav.arbeidsgiver.altinnrettigheter.proxy.TestTokenUtil
import no.nav.arbeidsgiver.altinnrettigheter.proxy.altinn.AltinnClient
import no.nav.arbeidsgiver.altinnrettigheter.proxy.config.CachingConfig.Companion.REPORTEES_CACHE
import no.nav.arbeidsgiver.altinnrettigheter.proxy.model.Fnr
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.cache.CacheManager
import org.springframework.http.HttpHeaders
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@SpringBootTest(
    properties = [
        "server.servlet.context-path=/",
    ],
)
@ActiveProfiles("test")
@AutoConfigureMockMvc
@AutoConfigureObservability
@EnableMockOAuth2Server
@DirtiesContext(classMode= DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class AltinnRettigheterProxyIntegrationTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var testTokenUtil: TestTokenUtil

    @MockBean
    lateinit var altinnClient: AltinnClient

    @Autowired
    lateinit var cacheManager: CacheManager

    @BeforeEach
    fun beforeEach() {
        /* Repeatedly running the test may fail if not cleaned. */
        cacheManager.getCache(REPORTEES_CACHE)?.clear()
    }

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
            mockMvc.get("/organisasjoner?serviceCode={code}&serviceEdition={edition}", "1337", "1") {
                header(HttpHeaders.AUTHORIZATION, "Bearer ${testTokenUtil.createToken(sub1.verdi)}")
            }.andExpect {
                status { isOk() }
            }.andDo {
                print()
            }
            mockMvc.get("/organisasjoner?serviceCode={code}&serviceEdition={edition}", "1337", "1") {
                header(HttpHeaders.AUTHORIZATION, "Bearer ${testTokenUtil.createToken(sub2.verdi)}")
            }.andExpect {
                status { isOk() }
            }.andDo {
                print()
            }

            verify(altinnClient, times(1)).hentOrganisasjoner(query, sub1)
            verify(altinnClient, times(1)).hentOrganisasjoner(query, sub2)
        }
    }

    @Test
    fun `is ready`() {
        mockMvc
            .get("/internal/ready")
            .andExpect { status { isOk() } }
        mockMvc
            .get("/internal/actuator/prometheus")
            .andExpect { status { isOk() } }
            .andDo { print() }
    }
}