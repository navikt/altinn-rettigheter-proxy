package no.nav.arbeidsgiver.altinnrettigheter.proxy

import org.apache.commons.lang3.StringUtils
import org.slf4j.MDC
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.io.IOException
import java.util.*
import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import kotlin.jvm.Throws

@Component
class CorrelationIdFilter : OncePerRequestFilter() {

    @Throws(ServletException::class, IOException::class)
    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain) {
        try {
            val correlationIdHeader = request.getHeader(CORRELATION_ID_HEADER_NAME)
            if (StringUtils.isBlank(correlationIdHeader)) {
                MDC.put(CORRELATION_ID_MDC_NAME, UUID.randomUUID().toString())
            } else {
                MDC.put(CORRELATION_ID_MDC_NAME, correlationIdHeader)
            }
            response.addHeader(CORRELATION_ID_HEADER_NAME, MDC.get(CORRELATION_ID_MDC_NAME))
            chain.doFilter(request, response)
        } finally {
            MDC.remove(CORRELATION_ID_MDC_NAME)
        }
    }

    companion object {
        const val CORRELATION_ID_MDC_NAME = "correlationId"
        private const val CORRELATION_ID_HEADER_NAME = "X-Correlation-Id"
    }
}
