package no.nav.arbeidsgiver.altinnrettigheter.proxy

import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import org.springframework.context.annotation.Profile

@Profile("local")
@EnableMockOAuth2Server
class LocalOgUnitTestOidcConfiguration