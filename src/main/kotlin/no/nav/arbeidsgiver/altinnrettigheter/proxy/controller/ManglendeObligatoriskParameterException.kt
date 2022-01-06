package no.nav.arbeidsgiver.altinnrettigheter.proxy.controller

import java.lang.RuntimeException

class ManglendeObligatoriskParameterException(val parametere: List<String>) :
    RuntimeException()
