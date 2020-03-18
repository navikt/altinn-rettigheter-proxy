package no.nav.arbeidsgiver.altinnrettigheter.proxy.model

import com.fasterxml.jackson.annotation.JsonValue

class Fnr (var verdi: String){

        fun Fnr(verdi: String) {
            if (!erGyldigFnr(verdi)) {
                throw RuntimeException("Ugyldig fødselsnummer. Må bestå av 11 tegn.")
            }
            this.verdi = verdi
        }

        fun erGyldigFnr(fnr: String): Boolean {
            return fnr.matches(Regex("^[0-9]{11}$"))
        }

        @JsonValue
        fun asString(): String? {
            return verdi
        }
}