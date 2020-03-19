package no.nav.arbeidsgiver.altinnrettigheter.proxy.config

import no.nav.security.oidc.test.support.spring.TokenGeneratorConfiguration
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Profile

@Configuration
@Import(TokenGeneratorConfiguration::class)
@Profile("local")
class LocalOgUnitTestOidcConfiguration {
}