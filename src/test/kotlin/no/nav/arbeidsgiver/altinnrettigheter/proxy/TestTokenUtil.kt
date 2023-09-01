package no.nav.arbeidsgiver.altinnrettigheter.proxy

import no.nav.security.mock.oauth2.MockOAuth2Server
import org.springframework.stereotype.Component

@Component
class TestTokenUtil(
    val mockOAuth2Server: MockOAuth2Server
) {
    fun createToken(
        pid: String,
        issuerId: String = "tokenx",
        idp: String? = null,
        sub: String = "foo",
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
                    claims["pid"] = pid
                }
            )
            .serialize()
}