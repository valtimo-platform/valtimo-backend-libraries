/*
 * Copyright 2015-2022 Ritense BV, the Netherlands.
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

package com.ritense.klant.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test

class KlantSearchFilterTest {

    @Test
    fun `toMap should only include properties that have been set`() {
        val filter = KlantSearchFilter(bsn = "123")

        val map = filter.toMap()

        assertEquals("123", map["subjectNatuurlijkPersoon__inpBsn"])
        assertFalse(map.containsKey("klantnummer"))
        assertFalse(map.containsKey("subjectNietNatuurlijkPersoon__annIdentificatie"))
    }

    @Test
    fun `toMap should handle kvk numbers`() {
        val filter = KlantSearchFilter(kvk = "123")

        val map = filter.toMap()

        assertEquals("123", map["subjectNietNatuurlijkPersoon__annIdentificatie"])
        assertFalse(map.containsKey("klantnummer"))
        assertFalse(map.containsKey("subjectNatuurlijkPersoon__inpBsn"))
    }

    @Test
    fun `toMap should map multiple values`() {
        val filter = KlantSearchFilter(
            kvk = "123",
            klantnummer = "321")

        val map = filter.toMap()

        assertEquals("123", map["subjectNietNatuurlijkPersoon__annIdentificatie"])
        assertEquals("321", map["klantnummer"])
        assertFalse(map.containsKey("subjectNatuurlijkPersoon__inpBsn"))
    }
}