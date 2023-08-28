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

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class RsinTest {

    @Test
    fun `should throw illegal argument caused by invalid rsin`() {
        assertThrows(IllegalArgumentException::class.java) { Rsin("123456789") }
    }

    @Test
    fun `should create valid rsin`() {
        val rsin = Rsin("002564440")
        assertThat(rsin.toString()).isEqualTo("002564440")
    }

    @Test
    fun `bugfix should create valid rsin`() {
        val rsin = Rsin("001326132")
        assertThat(rsin.toString()).isEqualTo("001326132")
    }

    /*
    *    0-0-2-5-6-4-4-4-0
    *    9×0=0 + 8×0=0 + 7×2=14 + 6×5=30 + 5×6=30 + 4×4=16 + 3×4=12 + 2×4=8 + 1×0=0 = 110/11 =10 -> valide is deelbaar door 11
    *
    *    0-0-1-3-2-6-1-3-2
    *    9×0=0 + 8×0=0 + 7×1=7 + 6×3=18 + 5×2=10 + 4×6=24 + 3×1=3 + 2×3=6 + 1×2=2 = 70/11 = 6.3 -> niet-valide niet deelbaar door 11
    *
    *    0-0-1-3-2-6-1-3-2 (BSN variant)
    *    9×0=0 + 8×0=0 + 7×1=7 + 6×3=18 + 5×2=10 + 4×6=24 + 3×1=3 + 2×3=6 + -1×2=-2 = 66/11 = 6 -> valide is deelbaar door 11
    * */

}