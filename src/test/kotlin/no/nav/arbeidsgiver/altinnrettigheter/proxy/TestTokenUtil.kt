package no.nav.arbeidsgiver.altinnrettigheter.proxy

import no.nav.security.mock.oauth2.MockOAuth2Server
import org.springframework.stereotype.Component

@Component
class TestTokenUtil(
    val mockOAuth2Server: MockOAuth2Server
) {
    fun createToken(sub: String): String =
        mockOAuth2Server
            .issueToken("loginservice", sub, "someaudience")
            .serialize()
}