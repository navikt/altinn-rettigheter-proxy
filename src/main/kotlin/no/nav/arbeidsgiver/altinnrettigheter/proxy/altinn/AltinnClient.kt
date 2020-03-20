package no.nav.arbeidsgiver.altinnrettigheter.proxy.altinn

import no.nav.arbeidsgiver.altinnrettigheter.proxy.model.AltinnOrganisasjon
import no.nav.arbeidsgiver.altinnrettigheter.proxy.model.AltinnRolle
import no.nav.arbeidsgiver.altinnrettigheter.proxy.model.Fnr
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.*
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI

@Component
class AltinnClient(val restTemplate: RestTemplate) {
    @Value("\${altinn.url}")
    lateinit var altinnUrl: String
    @Value("\${altinn.apigw.apikey}")
    lateinit var altinnAPIGWApikey: String
    @Value("\${altinn.apikey}")
    lateinit var altinnApikey: String
    @Value("\${altinn.iaweb.service.code}")
    lateinit var iawebServiceCode: String
    @Value("\${altinn.iaweb.service.edition}")
    lateinit var iawebServiceEdition: String

    fun hentOrgnumreDerBrukerHarEnkeltrettighetTilIAWeb(fnr: Fnr): List<AltinnOrganisasjon> {
        val uri: URI = UriComponentsBuilder.fromUriString(altinnUrl).pathSegment()
                .pathSegment("ekstern", "altinn", "api", "serviceowner", "reportees")
                .queryParam("ForceEIAuthentication")
                .queryParam("subject", fnr.verdi)
                .queryParam("serviceCode", iawebServiceCode)
                .queryParam("serviceEdition", iawebServiceEdition)
                .build()
                .toUri()

        return try {
            val respons = restTemplate.exchange(
                    uri,
                    HttpMethod.GET,
                    getHeaderEntity(),
                    object : ParameterizedTypeReference<List<AltinnOrganisasjon>>() {}
            )

            if (respons.statusCode != HttpStatus.OK) {
                val message = "Kall mot aareg feiler med HTTP-" + respons.statusCode

                throw RuntimeException(message)
            }
            respons.body!!
        } catch (exception: RestClientException) {
            throw AltinnException("Feil ved kall til Altinn", exception)
        }

    }

    private fun getHeaderEntity(): HttpEntity<Any?>? {
        val headers = HttpHeaders()
        headers["X-NAV-APIKEY"] = altinnAPIGWApikey
        headers["APIKEY"] = altinnApikey
        return HttpEntity(headers)
    }

}