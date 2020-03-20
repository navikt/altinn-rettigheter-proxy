package no.nav.arbeidsgiver.altinnrettigheter.proxy;

import no.nav.security.spring.oidc.api.EnableOIDCTokenValidation
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching

@SpringBootApplication
@EnableOIDCTokenValidation(
        ignore = ["springfox.documentation.swagger.web.ApiResourceController", "org.springframework"]
)
@EnableCaching
class AltinnrettigheterProxyApplication {

}

fun main(args: Array<String>) {
    runApplication<AltinnrettigheterProxyApplication>(*args)
}