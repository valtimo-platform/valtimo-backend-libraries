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

package com.ritense.zakenapi.domain

import com.fasterxml.jackson.databind.node.TextNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.treeToValue
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class BetalingsindicatieTest {

    private val objectMapper = jacksonObjectMapper()

    @Test
    fun `should deserialize to null when empty`() {
        val result: Betalingsindicatie = objectMapper.treeToValue(TextNode(""))

        assertNull(result)
    }

    @Test
    fun `should deserialize to null when value does not exist`() {
        val result: Betalingsindicatie = objectMapper.treeToValue(TextNode("invalid key"))

        assertNull(result)
    }

    @Test
    fun `should deserialize to enum when key exists`() {
        val result: Betalingsindicatie = objectMapper.treeToValue(TextNode(Betalingsindicatie.NVT.key))

        assertEquals(Betalingsindicatie.NVT, result)
    }
}