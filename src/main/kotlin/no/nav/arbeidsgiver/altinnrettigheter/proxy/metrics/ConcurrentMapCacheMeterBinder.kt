package no.nav.arbeidsgiver.altinnrettigheter.proxy.metrics

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tag
import io.micrometer.core.instrument.binder.cache.CacheMeterBinder

class ConcurrentMapCacheMeterBinder(
        private val cache: ConcurrentMapCacheMetricsWrapper,
        tags: Iterable<Tag>
) : CacheMeterBinder(cache, cache.name, tags) {

    override fun size(): Long? {
        return cache.nativeCache.size.toLong()
    }

    override fun hitCount(): Long {
        return cache.getHitCount()
    }

    override fun missCount(): Long {
        return cache.getMissCount()
    }

    override fun evictionCount(): Long {
        return cache.getEvictCount()
    }

    override fun putCount(): Long {
        return cache.getPutCount()
    }

    override fun bindImplementationSpecificMetrics(meterRegistry: MeterRegistry) {}

}
