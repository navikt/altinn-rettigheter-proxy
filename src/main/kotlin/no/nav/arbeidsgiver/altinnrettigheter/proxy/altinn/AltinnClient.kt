package no.nav.arbeidsgiver.altinnrettigheter.proxy.altinn

import no.nav.arbeidsgiver.altinnrettigheter.proxy.model.AltinnOrganisasjon
import no.nav.arbeidsgiver.altinnrettigheter.proxy.model.Fnr
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.*
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
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

    fun hentOrgnumre(
            query: Map<String, String>
    ): List<AltinnOrganisasjon> {

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

        val uri: URI = uriBuilder.build().toUri()

        logger.info("uri: $uri")

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

    companion object {
        private val logger = LoggerFactory.getLogger(AltinnClient::class.java)
    }
}

// ekstern/altinn/api/serviceowner/reportees?ForceEIAuthentication&subject=01065500791&serviceCode=3403&serviceEdition=1
// ekstern/altinn/api/serviceowner/reportees?ForceEIAuthentication=&subject=01065500791&serviceCode=3403&serviceEdition=1
