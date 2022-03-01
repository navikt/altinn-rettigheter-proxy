package no.nav.arbeidsgiver.altinnrettigheter.proxy.model

import org.slf4j.LoggerFactory

data class Fnr(val verdi: String) {
    init {
        if (!verdi.matches(digits)) {
            throw RuntimeException("Ugyldig fødselsnummer. Må bestå av 11 tegn.")
        }

        if (!validatePersonDNumberMod11(verdi)) {
            log.warn("kontroll-siffer er feil i fødselsnummeret")
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java)
        private val digits = Regex("^[0-9]{11}$")
        private val lookup1: IntArray = intArrayOf(3, 7, 6, 1, 8, 9, 4, 5, 2, 0)
        private val lookup2: IntArray = intArrayOf(5, 4, 3, 2, 7, 6, 5, 4, 3, 2)

        fun validatePersonDNumberMod11(personNumber: String): Boolean {
            if (personNumber.length != 11)
                return false

            var checksum1 = 0
            var checksum2 = 0

            for (i in 0..9) {
                val currNum = (personNumber[i] - '0')
                checksum1 += currNum * lookup1[i]
                checksum2 += currNum * lookup2[i]
            }

            checksum1 %= 11
            checksum2 %= 11

            val checksum1Final = if (checksum1 == 0) 0 else 11 - checksum1
            val checksum2Final = if (checksum2 == 0) 0 else 11 - checksum2

            return checksum1Final != 10 &&
                    personNumber[9] - '0' == checksum1Final &&
                    personNumber[10] - '0' == checksum2Final
        }
    }
}