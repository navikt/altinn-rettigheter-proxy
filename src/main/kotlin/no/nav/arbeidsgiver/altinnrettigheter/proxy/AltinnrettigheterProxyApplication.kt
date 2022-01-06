package no.nav.arbeidsgiver.altinnrettigheter.proxy;

import no.nav.security.token.support.spring.api.EnableJwtTokenValidation
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching

@SpringBootApplication
@EnableJwtTokenValidation(
        ignore = [
            "org.springdoc",
            "org.springframework"
        ]
)
@EnableCaching
@ConfigurationPropertiesScan("no.nav.arbeidsgiver.altinnrettigheter")
class AltinnrettigheterProxyApplication

fun main(args: Array<String>) {
    runApplication<AltinnrettigheterProxyApplication>(*args)
}