package no.nav.arbeidsgiver.altinnrettigheter.proxy.maskinporten

import com.nimbusds.jose.crypto.RSASSASigner
import com.nimbusds.jose.jwk.RSAKey
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConfigurationProperties(prefix="maskinporten")
@ConstructorBinding
data class MaskinportenConfig(
    val clientId: String,
    val scopes: String,
    val wellKnownUrl: String,
    val clientJwk: String,
) {
    val privateJwkRsa = RSAKey.parse(clientJwk)
    val jwsSigner = RSASSASigner(privateJwkRsa)
}