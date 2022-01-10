package no.nav.arbeidsgiver.altinnrettigheter.proxy

fun <T> basedOnEnv(prod: () -> T, other: () -> T): T =
    when (System.getenv("NAIS_CLUSTER_NAME")) {
        "prod-gcp" -> prod()
        else -> other()
    }