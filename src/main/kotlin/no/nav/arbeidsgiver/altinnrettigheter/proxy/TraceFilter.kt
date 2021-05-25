package no.nav.arbeidsgiver.altinnrettigheter.proxy

import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.io.IOException
import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class TraceFilter : OncePerRequestFilter() {

    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain) {
        if (System.getenv("NAIS_CLUSTER_NAME") == "prod-fss") {
            val headers = request
                .headerNames
                .toList()
                .filter { it.toLowerCase() != "authorization" }
                .associateWith { request.getHeader(it) }
            logger.info("${request.method} ${request.requestURI} $headers")
        }
        chain.doFilter(request, response)
    }
}
