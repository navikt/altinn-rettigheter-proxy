package no.nav.arbeidsgiver.altinnrettigheter.proxy.controller

import org.springframework.core.annotation.Order
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.InitBinder
import org.springframework.web.bind.WebDataBinder

@ControllerAdvice
@Order(10000)
class Spring4ShellPatch {
    @InitBinder
    fun setAllowedFields(dataBinder: WebDataBinder) {
        // This code protects Spring Core from a "Remote Code Execution" attack (dubbed "Spring4Shell").
        // By applying this mitigation, you prevent the "Class Loader Manipulation" attack vector from firing.
        // For more details, see this post: https://www.lunasec.io/docs/blog/spring-rce-vulnerabilities/
        val denylist = arrayOf("class.*", "Class.*", "*.class.*", "*.Class.*")
        dataBinder.setDisallowedFields(*denylist)
    }
}