package no.nav.arbeidsgiver.altinnrettigheter.proxy.maskinporten

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.Instant

class TokenResponseWrapperTest {
    @Test
    fun expiresIn() {
        assertExpiresIn(age = 120, tokenExpire = 120, expectedExpiresIn = 0)
        assertExpiresIn(age = 120, tokenExpire = 60, expectedExpiresIn = -60)
        assertExpiresIn(age = 120, tokenExpire = 180, expectedExpiresIn = 60)
    }

    @Test
    fun timeToRefresh() {
        assertTimeToRefresh(90, 180, 50.0)
        assertTimeToRefresh(180, 180, 0.0)
        assertTimeToRefresh(200, 180, 0.0)
        assertTimeToRefresh(135, 180, 25.0)
    }

    private fun assertTimeToRefresh(age: Long, tokenExpire: Long, expectedRemainingPercentage: Double) {
        val now = Instant.now()
        val requestedAt = now.minus(Duration.ofSeconds(age))
        val remainingPercetage = TokenResponseWrapper(
            requestedAt = requestedAt,
            TokenResponse(
                expiresInSeconds = tokenExpire,
                accessToken = "",
                tokenType = "",
                scope = "",
            )
        ).timeToRefresh(now)
        assertEquals(
            expectedRemainingPercentage,
            remainingPercetage
        )
    }

    @Suppress("SameParameterValue")
    private fun assertExpiresIn(age: Long, tokenExpire: Long, expectedExpiresIn: Long) {
        assertEquals(
            expiresIn(age = age, expireSeconds = tokenExpire),
            Duration.ofSeconds(expectedExpiresIn)
        )
    }

    private fun expiresIn(
        expireSeconds: Long,
        age: Long
    ): Duration {
        val now = Instant.now()
        val requestedAt = now.minus(Duration.ofSeconds(age))
        return TokenResponseWrapper(
            requestedAt = requestedAt,
            TokenResponse(
                expiresInSeconds = expireSeconds,
                accessToken = "",
                tokenType = "",
                scope = "",
            )
        ).expiresIn(now)
    }

}