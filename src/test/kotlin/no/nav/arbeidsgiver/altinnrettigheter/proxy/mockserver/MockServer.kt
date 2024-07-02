package no.nav.arbeidsgiver.altinnrettigheter.proxy.mockserver

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.common.ConsoleNotifier
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.core.io.Resource
import org.springframework.stereotype.Component
import java.net.URL

@Profile("local")
@Component
class MockServer(
    @Value("\${mock.port}") port: Int,
    @Value("\${altinn.url}") altinnUrl: String,
    @Value("classpath:mock/altinnReporteesAlleOrganisasjonerForServiceCode3403.json") reporteesAlle3403Json: Resource,
    @Value("classpath:mock/altinnReporteesAktiveOrganisasjonerForServiceCode3403.json") reporteesAktive3403Json: Resource,
    @Value("classpath:mock/altinnReporteesAktiveOrganisasjoner.json") reporteesAktiveJson: Resource
) {

    init {
        val server = WireMockServer(
            WireMockConfiguration()
                .port(port)
                .notifier(ConsoleNotifier(true))
        )
        val altinnPathToReportees = URL(altinnUrl).path + "api/serviceowner/reportees"

        // Mock kall til Altinn uten filter på aktive organisasjoner med serviceCode 3403 --> returnerer 5 organisasjoner
        server.stubFor(
            WireMock.get(WireMock.urlPathEqualTo(altinnPathToReportees))
                .withHeader("Accept", WireMock.containing("application/json"))
                .withQueryParams(
                    mapOf(
                        "ForceEIAuthentication" to WireMock.equalTo(""),
                        "serviceCode" to WireMock.equalTo("3403"),
                        "serviceEdition" to WireMock.equalTo("1"),
                        "\$top" to WireMock.equalTo("500"),
                        "\$skip" to WireMock.equalTo("0"),
                        "subject" to WireMock.equalTo("01065500791")
                    )
                )
                .willReturn(
                    WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(reporteesAlle3403Json.inputStream.readAllBytes())
                )
        )

        // Mock kall til Altinn MED filter på aktive organiasjoner og serviceCode 3403  --> returnerer 4 organisasjoner
        server.stubFor(
            WireMock.get(WireMock.urlPathEqualTo(altinnPathToReportees))
                .withHeader("Accept", WireMock.containing("application/json"))
                .withQueryParams(
                    mapOf(
                        "ForceEIAuthentication" to WireMock.equalTo(""),
                        "serviceCode" to WireMock.equalTo("3403"),
                        "serviceEdition" to WireMock.equalTo("1"),
                        "\$top" to WireMock.equalTo("500"),
                        "\$skip" to WireMock.equalTo("0"),
                        "\$filter" to WireMock.equalTo("Type ne 'Person' and Status eq 'Active'"),
                        "subject" to WireMock.equalTo("01065500791")
                    )
                )
                .willReturn(
                    WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(reporteesAktive3403Json.inputStream.readAllBytes())
                )
        )

        // Mock kall til Altinn MED filter på aktive organiasjoner (alle rettigheter)  --> returnerer 6 organisasjoner
        server.stubFor(
            WireMock.get(WireMock.urlPathEqualTo(altinnPathToReportees))
                .withHeader("Accept", WireMock.containing("application/json"))
                .withQueryParams(
                    mapOf(
                        "ForceEIAuthentication" to WireMock.equalTo(""),
                        "\$top" to WireMock.equalTo("500"),
                        "\$skip" to WireMock.equalTo("0"),
                        "\$filter" to WireMock.equalTo("Type ne 'Person' and Status eq 'Active'"),
                        "subject" to WireMock.equalTo("01020300123")
                    )
                )
                .willReturn(
                    WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(reporteesAktiveJson.inputStream.readAllBytes())
                )
        )
        server.start()
    }
}
