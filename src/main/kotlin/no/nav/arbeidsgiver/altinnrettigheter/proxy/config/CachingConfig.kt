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

    @Bean
    fun cacheManager(): CacheManager? {
        val cacheManager = SimpleCacheManager()


        val reporteesCache = CaffeineCache(
                "reportees",
                Caffeine.newBuilder()
                        .expireAfterWrite(5, TimeUnit.MINUTES)
                        .maximumSize(10000)
                        .build<Any, Any>(),
                false)

        cacheManager.setCaches(listOf(reporteesCache))
        return cacheManager
    }
}
