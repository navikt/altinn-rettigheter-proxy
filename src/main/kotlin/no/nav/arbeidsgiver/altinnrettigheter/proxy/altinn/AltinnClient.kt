package no.nav.arbeidsgiver.altinnrettigheter.proxy.altinn

import no.nav.arbeidsgiver.altinnrettigheter.proxy.maskinporten.MaskinportenTokenService
import no.nav.arbeidsgiver.altinnrettigheter.proxy.model.AltinnOrganisasjon
import no.nav.arbeidsgiver.altinnrettigheter.proxy.model.Fnr
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.retry.RetryCallback
import org.springframework.retry.backoff.FixedBackOffPolicy
import org.springframework.retry.policy.SimpleRetryPolicy
import org.springframework.retry.support.RetryTemplate
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import java.time.Duration

@Component
class AltinnClient(
    private val maskinportenTokenService: MaskinportenTokenService,
    restTemplateBuilder: RestTemplateBuilder
) {

    private val restTemplate: RestTemplate = restTemplateBuilder
        .setConnectTimeout(Duration.ofSeconds(60))
        .setReadTimeout(Duration.ofSeconds(120))
        .additionalInterceptors(
            retryInterceptor(
                maxAttempts = 3,
                backoffPeriod = 250L,
                javax.net.ssl.SSLHandshakeException::class.java
            )
        )
        .build()

    @Value("\${altinn.url}")
    lateinit var altinnUrl: String

    @Value("\${altinn.apikey}")
    lateinit var altinnApikey: String

    val apiUrl : String by lazy {
        UriComponentsBuilder
            .fromUriString(altinnUrl)
            .pathSegment()
            .pathSegment(
                "api",
                "serviceowner",
                "reportees"
            ).build().toUriString()
    }

    fun createHeaders(): HttpEntity<Any?> {
        val headers = HttpHeaders()
        headers.setBearerAuth(maskinportenTokenService.currentAccessToken())
        headers["APIKEY"] = altinnApikey
        return HttpEntity(headers)
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
            restTemplate.exchange(
                "$apiUrl?$query",
                HttpMethod.GET,
                createHeaders(),
                object : ParameterizedTypeReference<List<AltinnOrganisasjon>>() {},
                queryParametereMedSubject
            ).body!!
        } catch (exception: HttpStatusCodeException) {
            if (exception.manglerAltinnProfil()) {
                listOf()
            } else {
                throw ProxyHttpStatusCodeException(
                    HttpStatus.valueOf(exception.statusCode.value()),
                    exception.statusText,
                    exception.responseBodyAsString,
                    exception
                )
            }
        } catch (exception: RuntimeException) {
            throw AltinnException("Feil ved kall til Altinn", exception)
        }
    }
}

private fun HttpStatusCodeException.manglerAltinnProfil() =
    statusCode.value() == 400 && statusText.contains("User profile")


@Suppress("SameParameterValue")
private fun retryInterceptor(maxAttempts: Int, backoffPeriod: Long, vararg retryable: Class<out Throwable>) =
    ClientHttpRequestInterceptor { request, body, execution ->
        RetryTemplate().apply {
            setRetryPolicy(SimpleRetryPolicy(maxAttempts, retryable.associateWith { true }, true))
            setBackOffPolicy(FixedBackOffPolicy().apply {
                backOffPeriod = backoffPeriod
            })
        }.execute(RetryCallback {
            execution.execute(request, body)
        })
    }