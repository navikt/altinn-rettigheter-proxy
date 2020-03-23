package no.nav.arbeidsgiver.altinnrettigheter.proxy.metrics

import org.springframework.cache.Cache
import org.springframework.cache.concurrent.ConcurrentMapCache
import org.springframework.lang.Nullable
import java.util.concurrent.Callable
import java.util.concurrent.ConcurrentMap
import java.util.concurrent.atomic.AtomicLong

class ConcurrentMapCacheMetricsWrapper(
        name: String, store: ConcurrentMap<Any, Any>,
        allowNullValues: Boolean
) : ConcurrentMapCache(name, store, allowNullValues) {
    private val hitCount = AtomicLong(0)
    private val missCount = AtomicLong(0)
    private val putCount = AtomicLong(0)
    private val evictCount = AtomicLong(0)

    override fun get(key: Any): Cache.ValueWrapper? {
        countGet(key)
        return super.get(key)
    }

    override fun <T> get(key: Any, type: Class<T>?): T? {
        countGet(key)
        return super.get(key, type)
    }

    @Nullable
    override fun <T> get(key: Any, valueLoader: Callable<T>): T? {
        countGet(key)
        return super.get(key, valueLoader)
    }

    @Nullable
    private fun countGet(key: Any): Cache.ValueWrapper? {
        val valueWrapper = super.get(key)
        if (valueWrapper != null) hitCount.incrementAndGet() else missCount.incrementAndGet()
        return valueWrapper
    }

    override fun put(key: Any, value: Any?) {
        putCount.incrementAndGet()
        super.put(key, value)
    }

    override fun putIfAbsent(key: Any, value: Any?): Cache.ValueWrapper? {
        if (!nativeCache.containsKey(key)) {
            putCount.incrementAndGet()
        }
        return super.putIfAbsent(key, value)
    }

    override fun evict(key: Any) {
        evictCount.incrementAndGet()
        super.evict(key)
    }

    override fun clear() {
        super.clear()
    }

    fun getHitCount(): Long {
        return hitCount.get()
    }

    fun getMissCount(): Long {
        return missCount.get()
    }

    fun getPutCount(): Long {
        return putCount.get()
    }

    fun getEvictCount(): Long {
        return evictCount.get()
    }
}
