package no.nav.arbeidsgiver.altinnrettigheter.proxy.mockserver

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.common.ConsoleNotifier
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer
import com.github.tomakehurst.wiremock.matching.StringValuePattern
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

    private val MOCK_SERVER_VERBOSE_CONSOLE_LOGGING_ENABLED = false;

    init {
        System.out.println("mocking")
        val server = WireMockServer(
                WireMockConfiguration()
                        .port(port)
                        .extensions(
                                ResponseTemplateTransformer(true)
                        )
                        .notifier(
                                ConsoleNotifier(MOCK_SERVER_VERBOSE_CONSOLE_LOGGING_ENABLED)
                        )
        )
        val altinnPathToReportees = URL(altinnUrl).path + "ekstern/altinn/api/serviceowner/reportees"

        mockWithParameters(
                server,
                "$altinnPathToReportees",
                mapOf(
                        "ForceEIAuthentication" to WireMock.equalTo(""),
                        "serviceCode" to WireMock.equalTo("3403"),
                        "serviceEdition" to WireMock.equalTo("1"),
                        "\$top" to WireMock.equalTo("500"),
                        "\$skip" to WireMock.equalTo("0"),
                        "\$filter" to WireMock.equalTo("Type ne 'Person' and Status eq 'Active'"),
                        "subject" to WireMock.equalTo("01065500791")
                ),
                "altinnReportees.json"
        )
        mockWithParameters(
                server,
                "$altinnPathToReportees",
                mapOf(
                        "ForceEIAuthentication" to WireMock.equalTo(""),
                        "\$top" to WireMock.equalTo("500"),
                        "\$skip" to WireMock.equalTo("0"),
                        "\$filter" to WireMock.equalTo("Type ne 'Person' and Status eq 'Active'"),
                        "subject" to WireMock.equalTo("01020300123")
                ),
                "altinnReportees.json"
        )
        server.start()
    }


    private fun mockWithParameters(
            server: WireMockServer,
            basePath: String,
            parameters: Map<String, StringValuePattern>,
            responseFile: String
    ) {
        server.stubFor(
                WireMock.get(WireMock.urlPathEqualTo(basePath))
                .withHeader("Accept", WireMock.containing("application/json"))
                .withQueryParams(parameters)
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                                hentStringFraFil(responseFile)
                        )
                )
        )
    }


    private fun hentStringFraFil(filnavn: String):String{
        return IOUtils.toString(MockServer::class.java.classLoader.getResourceAsStream("mock/$filnavn"), StandardCharsets.UTF_8)
    }
}
