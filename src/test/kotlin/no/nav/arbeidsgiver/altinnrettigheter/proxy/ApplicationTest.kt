package no.nav.arbeidsgiver.altinnrettigheter.proxy

import no.nav.arbeidsgiver.altinnrettigheter.proxy.controller.AltinnrettigheterProxyController
import no.nav.security.mock.oauth2.MockOAuth2Server
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = ["wiremock.mock.port=8083"])
@EnableMockOAuth2Server
class ApplicationTest {

    @Autowired
    private val controller: AltinnrettigheterProxyController? = null

    @Test
    fun contexLoads() {
        assertThat(controller).isNotNull
    }
}