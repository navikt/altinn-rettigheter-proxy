package no.nav.arbeidsgiver.altinnrettigheter.proxy.controller

import no.nav.arbeidsgiver.altinnrettigheter.proxy.maskinporten.MaskinportenTokenService
import no.nav.security.token.support.core.api.Unprotected
import org.slf4j.LoggerFactory
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@Unprotected
@RestController
class HealthcheckController(
    val redisConnectionFactory: RedisConnectionFactory,
    val maskinportenTokenService: MaskinportenTokenService
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @GetMapping("/internal/alive")
    fun alive(): ResponseEntity<String> {
        return if (maskinportenTokenService.alive())
            ResponseEntity.ok("OK")
        else
            ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build()
    }

    @GetMapping("/internal/ready")
    fun ready(): ResponseEntity<String> {
        val readyReadis = try {
            redisConnectionFactory.connection.use {
                it.ping() != null
            }
        } catch (e: RuntimeException) {
            logger.warn("redis ping feilet", e)
            false
        }

        val readyMaskinporten = maskinportenTokenService.ready()

        return if (readyReadis && readyMaskinporten) {
            ResponseEntity.ok("READY")
        } else {
            ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build()
        }
    }
}
