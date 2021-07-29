package com.ritense.openzaak.domain.configuration

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
        assertThat(rsin.value).isEqualTo("002564440")
    }

    @Test
    fun `bugfix should create valid rsin`() {
        val rsin = Rsin("001326132")
        assertThat(rsin.value).isEqualTo("001326132")
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