/*
 * Copyright 2015-2023 Ritense BV, the Netherlands.
 *
 * Licensed under EUPL, Version 1.2 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ritense.zgw

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

data class Rsin(private val value: String) {

    constructor(value: Long) : this(value.toString().padStart(9, '0'))

    companion object {
        @JvmStatic
        @JsonCreator
        fun create(value: String) = value
    }

    @JsonValue
    override fun toString() = value

    init {
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
