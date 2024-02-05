/*
 * Copyright 2015-2024 Ritense BV, the Netherlands.
 *
 *  Licensed under EUPL, Version 1.2 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" basis,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

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