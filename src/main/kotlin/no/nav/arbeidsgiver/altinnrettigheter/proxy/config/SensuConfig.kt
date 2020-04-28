package no.nav.arbeidsgiver.altinnrettigheter.proxy.config

import no.nav.metrics.MetricsClient
import no.nav.metrics.MetricsConfig
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@Profile("dev", "prod")
class SensuConfig {
    fun SensuConfig() {
        val miljø = System.getenv("NAIS_CLUSTER_NAME")
        MetricsClient.enableMetrics(MetricsConfig.resolveNaisConfig().withEnvironment(miljø))
    }
}