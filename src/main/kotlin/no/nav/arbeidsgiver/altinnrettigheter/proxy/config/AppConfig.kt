package no.nav.arbeidsgiver.altinnrettigheter.proxy.config

import no.nav.arbeidsgiver.altinnrettigheter.proxy.maskinporten.MaskinportenConfig
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate

@Configuration
@EnableConfigurationProperties(
    MaskinportenConfig::class
)
class AppConfig {
    @Bean
    fun restTemplate(builder: RestTemplateBuilder): RestTemplate {
        return builder.build()
    }
}