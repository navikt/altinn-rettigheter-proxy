package no.nav.arbeidsgiver.altinnrettigheter.proxy

import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse

@Component
class TraceFilter : OncePerRequestFilter() {

    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain) {
        if (System.getenv("NAIS_CLUSTER_NAME") == "prod-fss") {
            val headers = request
                .headerNames
                .toList()
                .filter { it.lowercase() != "authorization" }
                .associateWith { request.getHeader(it) }
            logger.info("${request.method} ${request.requestURI} $headers")
        }
        chain.doFilter(request, response)
    }
}
