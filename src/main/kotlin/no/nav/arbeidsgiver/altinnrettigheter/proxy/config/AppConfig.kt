package no.nav.arbeidsgiver.altinnrettigheter.proxy.config

import no.nav.arbeidsgiver.altinnrettigheter.proxy.maskinporten.MaskinportenConfig
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(
    MaskinportenConfig::class
)
class AppConfig {
}