package no.nav.arbeidsgiver.altinnrettigheter.proxy.service

import io.lettuce.core.*
import no.nav.arbeidsgiver.altinnrettigheter.proxy.model.AltinnOrganisasjon
import org.slf4j.LoggerFactory
import org.springframework.data.redis.RedisConnectionFailureException
import org.springframework.data.redis.connection.RedisSubscribedConnectionException
import org.springframework.stereotype.Service

@Service
class AltinnrettigheterService(val proxyService: AltinnrettigheterProxyService) {

    private val logger = LoggerFactory.getLogger(javaClass)

    fun hentOrganisasjoner(query: Map<String, String>): List<AltinnOrganisasjon> {
        return try {
            proxyService.hentOrganisasjonerCached(query)
        } catch (e: Exception) {
            when (e) {
                is RedisException,
                is RedisConnectionException,
                is RedisConnectionFailureException,
                is RedisSubscribedConnectionException,
                is RedisCommandExecutionException,
                is RedisCommandInterruptedException,
                is RedisCommandTimeoutException -> {
                    logger.warn("Fallback etter feil mot Redis cache, pga feil ${e.message}")
                    proxyService.hentOrganisasjonerIAltinn(query)
                }
                else -> throw e
            }
        }
    }
}