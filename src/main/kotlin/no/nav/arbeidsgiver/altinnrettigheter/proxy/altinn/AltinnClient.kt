package no.nav.arbeidsgiver.altinnrettigheter.proxy.altinn

import no.nav.arbeidsgiver.altinnrettigheter.proxy.model.AltinnOrganisasjon
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI

@Component
class AltinnClient(restTemplateBuilder: RestTemplateBuilder) {

    private val restTemplate: RestTemplate = restTemplateBuilder.build()

    @Value("\${altinn.url}")
    lateinit var altinnUrl: String
    @Value("\${altinn.apigw.apikey}")
    lateinit var altinnAPIGWApikey: String
    @Value("\${altinn.apikey}")
    lateinit var altinnApikey: String

    fun hentOrganisasjoner(
            query: Map<String, String>
    ): List<AltinnOrganisasjon> {
        return try {
            val respons = restTemplate.exchange(
                    getURI(query),
                    HttpMethod.GET,
                    getHeaderEntity(),
                    object : ParameterizedTypeReference<List<AltinnOrganisasjon>>() {}
            )

            if (respons.statusCode != HttpStatus.OK) {
                val message = "Kall mot aareg feiler med HTTP-" + respons.statusCode

                throw RuntimeException(message)
            }
            respons.body!!
        } catch (exception: HttpClientErrorException) {
            if (exception.statusCode.is4xxClientError) {
                throw ProxyClientErrorException(
                        exception.statusCode,
                        exception.statusText,
                        exception.responseBodyAsString,
                        exception
                )
            }
            throw AltinnException("Feil ved kall til Altinn med returkode '${exception.statusCode}' " +
                    "og tekst '${exception.statusText}' ",
                    exception
            )
        }
        catch (exception: RestClientException) {
            throw AltinnException("Feil ved kall til Altinn", exception)
        }

    }
    private fun getURI(query: Map<String, String>): URI {
        val uriBuilder = UriComponentsBuilder.fromUriString(altinnUrl).pathSegment()
                .pathSegment("ekstern", "altinn", "api", "serviceowner", "reportees")

        query.forEach { (key, value) ->
            run {
                if (value == "") {
                    uriBuilder.queryParam(key)
                } else {
                    uriBuilder.queryParam(key, value)
                }
            }
        }

        return uriBuilder.build().toUri()
    }
    private fun getHeaderEntity(): HttpEntity<Any?>? {
        val headers = HttpHeaders()
        headers["X-NAV-APIKEY"] = altinnAPIGWApikey
        headers["APIKEY"] = altinnApikey
        return HttpEntity(headers)
    }
}
