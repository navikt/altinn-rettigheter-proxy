package no.nav.arbeidsgiver.altinnrettigheter.proxy.mockserver

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer
import org.apache.commons.io.IOUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.net.URL
import java.nio.charset.StandardCharsets

@Profile("local")
@Component
class MockServer @Autowired constructor(
        @Value("\${mock.port}")
        val port: Int,
        @Value("\${altinn.url}")
        val altinnUrl: String
) {

    init {
        System.out.println("mocking")
        val server = WireMockServer(
                WireMockConfiguration()
                        .port(port)
                        .extensions(
                                ResponseTemplateTransformer(true)
                        )
        )
        val altinnPathToReportees = URL(altinnUrl).path + "ekstern/altinn/api/serviceowner/reportees"
         mockForPath(server, altinnPathToReportees,"altinnReportees.json")

        server.start()
    }

    private fun mockForPath(server: WireMockServer, path: String, responseFile: String) {
        server.stubFor(WireMock.any(WireMock.urlPathMatching("$path.*"))
                .willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(hentStringFraFil(responseFile))
                ))
    }

    fun hentStringFraFil(filnavn: String):String{
        return IOUtils.toString(MockServer::class.java.classLoader.getResourceAsStream("mock/$filnavn"), StandardCharsets.UTF_8)
    }
}