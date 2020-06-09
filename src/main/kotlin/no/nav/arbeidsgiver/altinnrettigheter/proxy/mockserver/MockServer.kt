package no.nav.arbeidsgiver.altinnrettigheter.proxy.mockserver

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.common.ConsoleNotifier
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer
import org.apache.commons.io.IOUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.io.UnsupportedEncodingException
import java.net.URL
import java.net.URLEncoder
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
                        .notifier(
                                ConsoleNotifier(true)
                        )
        )
        val altinnPathToReportees = URL(altinnUrl).path + "ekstern/altinn/api/serviceowner/reportees?" +
                "ForceEIAuthentication&serviceCode=3403" +
                "&serviceEdition=1"

         mockForPath(server, "$altinnPathToReportees&subject=01065500791","altinnReportees.json")
         mockForPath(
                 server,
                 "$altinnPathToReportees"
                 + "&\$top=5"
                 + "&\$skip=0"
                 + "&\$filter=Type+ne+'Person'+and+Status+eq+'Active'"
                 + "&subject=01065500791",
        "altinnReportees-del1.json"
         )
         mockForPath(
                 server,
                 "$altinnPathToReportees"
                         + "&\$top=5"
                         + "&\$skip=5"
                         + "&\$filter=Type+ne+'Person'+and+Status+eq+'Active'"
                         + "&subject=01065500791",
                 "altinnReportees-del2.json"
         )
        server.start()
    }

    private fun mockForPath(server: WireMockServer, path: String, responseFile: String) {
        server.stubFor(
                WireMock.get(
                        path
                )
                .willReturn(
                        WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                                hentStringFraFil(responseFile)
                        )
                )
        )
    }

    fun hentStringFraFil(filnavn: String):String{
        return IOUtils.toString(MockServer::class.java.classLoader.getResourceAsStream("mock/$filnavn"), StandardCharsets.UTF_8)
    }
}