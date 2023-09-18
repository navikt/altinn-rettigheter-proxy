package no.nav.arbeidsgiver.altinnrettigheter.proxy.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.data.redis.LettuceClientConfigurationBuilderCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import java.net.URI


@Configuration
class RedisConfig {

    /**
     * workaround to get spring configured with nais env vars.
     * nais exposes url, username and password, but RedisURI expects usename and password to be part of the url
     * https://lettuce.io/core/release/api/io/lettuce/core/RedisURI.html
     *
     * see org.springframework.boot.autoconfigure.data.redis.RedisProperties.url which says
     * "Connection URL. Overrides host, port, and password. User is ignored. Example: redis://user:password@example.com:6379"
     */
    @Bean
    fun redisStandalone(
        @Value("\${spring.data.redis.redisuri}") redisUri: URI,
        @Value("\${spring.data.redis.password}") password: String,
        @Value("\${spring.data.redis.username}") username: String,
    ) = RedisStandaloneConfiguration(
        redisUri.host,
        redisUri.port
    ).apply {
        setUsername(username)
        setPassword(password)
    }

    @Bean
    fun lettuceClientConfigurationBuilderCustomizer(
        @Value("\${spring.data.redis.ssl}") ssl: Boolean,
    ) = LettuceClientConfigurationBuilderCustomizer {
        if (ssl) {
            it.useSsl().startTls()
        }
    }
}