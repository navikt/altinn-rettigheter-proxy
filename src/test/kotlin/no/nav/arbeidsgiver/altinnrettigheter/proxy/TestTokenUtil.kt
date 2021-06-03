package no.nav.arbeidsgiver.altinnrettigheter.proxy

import no.nav.security.mock.oauth2.MockOAuth2Server
import org.springframework.stereotype.Component

@Component
class TestTokenUtil(
    val mockOAuth2Server: MockOAuth2Server
) {
    fun createToken(
        sub: String,
        issuerId: String = "loginservice",
        idp: String? = null,
        pid: String? = null,
    ): String =
        mockOAuth2Server
            .issueToken(
                issuerId,
                sub,
                "someaudience",
                mutableMapOf<String, Any>().also { claims ->
                    if (idp != null) {
                        claims["idp"] = idp
                    }
                    if (pid != null) {
                        claims["pid"] = pid
                    }
                }
            )
            .serialize()
}