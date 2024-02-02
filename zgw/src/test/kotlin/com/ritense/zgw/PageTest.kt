package com.ritense.zgw

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.net.URI

class PageTest {

    @Test
    fun `should get all page results`() {
        val pages = IntRange(1, 10).map { i ->
            Page(
                1,
                (if (i >= 10) null else URI("")),
                (if (i <= 1) null else URI("")),
                listOf(i)
            )
        }

        var callCount = 0;
        val all = Page.getAll(20) { page ->
            callCount++
            pages[page - 1]
        }

        assertThat(callCount).isEqualTo(10)
        assertThat(all).isEqualTo(listOf(1,2,3,4,5,6,7,8,9,10))
    }

    @Test
    fun `should stop after maxPages`() {
        val pages = IntRange(1, 10).map { i ->
            Page(
                1,
                (if (i >= 10) null else URI("")),
                (if (i <= 1) null else URI("")),
                listOf(i)
            )
        }

        var callCount = 0;
        val all = Page.getAll(3) { page ->
            callCount++
            pages[page - 1]
        }

        assertThat(callCount).isEqualTo(3)
        assertThat(all).isEqualTo(listOf(1,2,3))
    }

    @Test
    fun `should throw an exception when pageLimit is valid`() {
        val ex = assertThrows<IllegalArgumentException> {
            Page.getAll(0) { _ ->
                Page(1, null, null, listOf(1))
            }
        }

        assertThat(ex.message).isEqualTo("pageLimit should be > 0 but was: 0")
    }
}