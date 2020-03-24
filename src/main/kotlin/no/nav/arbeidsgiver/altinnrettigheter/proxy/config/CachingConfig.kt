package no.nav.arbeidsgiver.altinnrettigheter.proxy.config

import com.google.common.cache.CacheBuilder
import org.springframework.cache.CacheManager
import org.springframework.cache.concurrent.ConcurrentMapCache
import org.springframework.cache.support.SimpleCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.TimeUnit


//@Configuration
class CachingConfig {

    @Bean
    fun cacheManager(): CacheManager? {
        val cacheManager = SimpleCacheManager()


        val reporteesCache = ConcurrentMapCache(
                "reportees",
                CacheBuilder
                        .newBuilder()
                        .expireAfterWrite(5, TimeUnit.MINUTES)
                        .maximumSize(10000)
                        .build<Any, Any>()
                        .asMap(),
                false)

        cacheManager.setCaches(listOf(reporteesCache))
        return cacheManager
    }
}