package com.ritense.openzaak.domain.configuration

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import com.ritense.valtimo.contract.validation.Validatable

data class Rsin(private val value: String) : Validatable {
    companion object {
        @JvmStatic
        @JsonCreator
        fun create(value: String) = value
    }

    @JsonValue
    override fun toString() = value

    init {
        validate()
        if (value.isNotEmpty()) {
            require(isValidBsn(value)) { "Invalid RSIN (BSN rules)" }
        }
    }

    /*
    * BSN variant is used here double checked with OpenZaak community
    *
    *    Examples:
    *
    *    0-0-1-3-2-6-1-3-2 (11 Proef)
    *    9×0=0 + 8×0=0 + 7×1=7 + 6×3=18 + 5×2=10 + 4×6=24 + 3×1=3 + 2×3=6 + 1×2=2 = 70/11 = 6.3 -> niet-valide niet deelbaar door 11
    *
    *    0-0-1-3-2-6-1-3-2 (BSN variant)
    *    9×0=0 + 8×0=0 + 7×1=7 + 6×3=18 + 5×2=10 + 4×6=24 + 3×1=3 + 2×3=6 + -1×2=-2 = 66/11 = 6 -> valide is deelbaar door 11
    * */
    private fun isValidBsn(value: String): Boolean {
        var result = 0
        val elfProefRange = intArrayOf(9, 8, 7, 6, 5, 4, 3, 2, -1)
        for (multiplier in elfProefRange) {
            val number = Character.getNumericValue(value[elfProefRange.indexOf(multiplier)])
            result += number * multiplier
        }
        return result % 11 == 0
    }
}