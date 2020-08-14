package no.nav.arbeidsgiver.altinnrettigheter.proxy.controller

import java.lang.RuntimeException

class UgyldigParameterException(var parameterNavn: String, var parameterValue: String) : RuntimeException() {

}
