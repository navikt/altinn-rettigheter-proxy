package no.nav.arbeidsgiver.altinnrettigheter.proxy.config

import no.nav.metrics.MetricsClient
import no.nav.metrics.MetricsConfig
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile


@Configuration
@Profile("dev", "prod")
class SensuConfig() {

    private val logger = LoggerFactory.getLogger(javaClass)

    init {
        val miljø = System.getenv("NAIS_CLUSTER_NAME")
        MetricsClient.enableMetrics(MetricsConfig.resolveNaisConfig().withEnvironment(miljø))
        logger.info("MetricsClient enabled")
    }

}