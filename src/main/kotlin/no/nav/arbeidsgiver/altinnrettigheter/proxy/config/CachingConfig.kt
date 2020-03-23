package no.nav.arbeidsgiver.altinnrettigheter.proxy.config

import com.github.benmanes.caffeine.cache.Caffeine
import org.springframework.cache.CacheManager
import org.springframework.cache.caffeine.CaffeineCache
import org.springframework.cache.support.SimpleCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.TimeUnit

@Configuration
class CachingConfig {

    companion object {
        const val REPORTEES_CACHE = "reportees"
    }

    @Bean
    fun reporteesCache(): CaffeineCache {
        return CaffeineCache(
                REPORTEES_CACHE,
                Caffeine.newBuilder()
                        .expireAfterWrite(5, TimeUnit.MINUTES)
                        .maximumSize(10000)
                        .recordStats()
                        .build(),
                false)
    }
}
