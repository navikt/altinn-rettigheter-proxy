package no.nav.arbeidsgiver.altinnrettigheter.proxy.controller

import org.springframework.http.RequestEntity
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.exchange
import java.net.URI

@RestController
class AltinnProxyController (val restTemplate: RestTemplate) {


    @RequestMapping("/proxy/**")
    fun proxy(requestEntity: RequestEntity<Any>, @RequestParam params: HashMap<String, String>): ResponseEntity<Any> {
        val remoteService = URI.create("https://testaltinnmar19.free.beeceptor.com")
        val uri = requestEntity.url.run {
            URI("https", userInfo, remoteService.host, remoteService.port, path, query, fragment)
        }

        val forward = RequestEntity(
                requestEntity.body, requestEntity.headers,
                requestEntity.method, uri
        )

        val responseEntity = restTemplate.exchange<Any>(forward)
        return responseEntity
    }

}