package no.nav.arbeidsgiver.altinnrettigheter.proxy.metrics

import io.micrometer.core.instrument.Tag
import io.micrometer.core.instrument.binder.MeterBinder
import org.springframework.boot.actuate.metrics.cache.CacheMeterBinderProvider
import org.springframework.stereotype.Component

@Component
class ConcurrentMapCacheBinderProvider : CacheMeterBinderProvider<ConcurrentMapCacheMetricsWrapper> {
    override fun getMeterBinder(cache: ConcurrentMapCacheMetricsWrapper, tags: Iterable<Tag>): MeterBinder {
        return ConcurrentMapCacheMeterBinder(cache, tags)
    }
}
