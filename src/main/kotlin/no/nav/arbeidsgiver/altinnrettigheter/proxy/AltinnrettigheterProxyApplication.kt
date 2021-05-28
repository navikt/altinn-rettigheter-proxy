package no.nav.arbeidsgiver.altinnrettigheter.proxy;

import no.nav.security.token.support.spring.api.EnableJwtTokenValidation
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching

@SpringBootApplication
@EnableJwtTokenValidation(
        ignore = [
            "springfox.documentation.swagger.web.ApiResourceController",
            "org.springframework"
        ]
)
@EnableCaching
class AltinnrettigheterProxyApplication

fun main(args: Array<String>) {
    runApplication<AltinnrettigheterProxyApplication>(*args)
}