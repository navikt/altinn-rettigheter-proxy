package no.nav.arbeidsgiver.altinnrettigheter.proxy.maskinporten

import com.google.common.net.HttpHeaders
import junit.framework.Assert.assertEquals
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import org.apache.http.client.utils.URIBuilder
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

@RunWith(SpringRunner::class)
@ActiveProfiles("local")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = ["wiremock.mock.port=8083"])
@EnableMockOAuth2Server
class HealthyEndpointTests {
    @LocalServerPort
    lateinit var port: String

    @Test
    fun helloWorld() {
        val response = HttpClient.newBuilder().build().send(
            HttpRequest.newBuilder()
                .uri(
                    URIBuilder()
                        .setScheme("http")
                        .setHost("localhost:$port")
                        .setPath("/altinn-rettigheter-proxy/internal/alive")
                        .build()
                )
                .GET()
                .build(),
            HttpResponse.BodyHandlers.ofString()
        )

        println(response)

        assertEquals(200, response.statusCode())
    }
}