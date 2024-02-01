package com.ritense.zgw

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
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
        val all = Page.getAll { page ->
            callCount++
            pages[page - 1]
        }

        assertThat(callCount).isEqualTo(10)
        assertThat(all).isEqualTo(listOf(1,2,3,4,5,6,7,8,9,10))
    }
}