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
    fun `should create entity for rsin is correct`() {
        val rsin = Rsin("002564440")
        assertThat(rsin.value).isEqualTo("002564440")
    }
}