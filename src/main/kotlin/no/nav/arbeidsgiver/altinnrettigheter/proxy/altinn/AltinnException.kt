package no.nav.arbeidsgiver.altinnrettigheter.proxy.altinn

import java.lang.RuntimeException

class AltinnException(message: String, e: Exception) : RuntimeException(message, e)