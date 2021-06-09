package no.nav.arbeidsgiver.altinnrettigheter.proxy.altinn

import no.nav.arbeidsgiver.altinnrettigheter.proxy.model.AltinnOrganisasjon
import no.nav.arbeidsgiver.altinnrettigheter.proxy.model.Fnr
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder

@Component
class AltinnClient(restTemplateBuilder: RestTemplateBuilder) {

    private val restTemplate: RestTemplate = restTemplateBuilder.build()

    @Value("\${altinn.url}")
    lateinit var altinnUrl: String
    @Value("\${altinn.apigw.apikey}")
    lateinit var altinnAPIGWApikey: String
    @Value("\${altinn.apikey}")
    lateinit var altinnApikey: String
    val apiUrl : String by lazy {
        UriComponentsBuilder
            .fromUriString(altinnUrl)
            .pathSegment()
            .pathSegment(
                "ekstern",
                "altinn",
                "api",
                "serviceowner",
                "reportees"
            ).build().toUriString()
    }
    val header : HttpEntity<Any?> by lazy {
        val headers = HttpHeaders()
        headers["X-NAV-APIKEY"] = altinnAPIGWApikey
        headers["APIKEY"] = altinnApikey
        HttpEntity(headers)
    }

    fun hentOrganisasjoner(
        queryParametere: Map<String, String>,
        fnr: Fnr
    ): List<AltinnOrganisasjon> {
        return try {
            val queryParametereMedSubject = (queryParametere + mapOf("subject" to fnr.verdi))
            val query = queryParametereMedSubject.map { (key, value) ->
                if (value == "") {
                    key
                } else {
                    "$key={$key}"
                }
            }.joinToString("&")
            val respons = restTemplate.exchange(
                "$apiUrl?$query",
                HttpMethod.GET,
                header,
                object : ParameterizedTypeReference<List<AltinnOrganisasjon>>() {},
                queryParametereMedSubject
            )

            if (respons.statusCode != HttpStatus.OK) {
                val message = "Kall mot aareg feiler med HTTP-" + respons.statusCode

                throw RuntimeException(message)
            }
            respons.body!!
        } catch (exception: HttpStatusCodeException) {
            if (exception.statusCode.isError) {
                throw ProxyHttpStatusCodeException(
                    exception.statusCode,
                    exception.statusText,
                    exception.responseBodyAsString,
                    exception
                )
            }
            throw AltinnException(
                "Feil ved kall til Altinn med returkode '${exception.statusCode}' " +
                        "og tekst '${exception.statusText}' ",
                exception
            )
        } catch (exception: RestClientException) {
            throw AltinnException("Feil ved kall til Altinn", exception)
        }
    }
}
