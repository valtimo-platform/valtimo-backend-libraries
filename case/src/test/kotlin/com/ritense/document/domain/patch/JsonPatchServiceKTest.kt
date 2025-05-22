/*
 * Copyright 2015-2024 Ritense BV, the Netherlands.
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
package com.ritense.document.domain.patch

import com.fasterxml.jackson.core.JsonPointer
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.node.TextNode
import com.ritense.valtimo.contract.json.MapperSingleton
import com.ritense.valtimo.contract.json.patch.JsonPatchBuilder
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert.assertEquals
import org.skyscreamer.jsonassert.JSONCompareMode

class JsonPatchServiceKTest {

    @Test
    @Throws(JsonProcessingException::class)
    fun `should patch existing object`() {
        val mapper = MapperSingleton.get()
        val jsonPatchBuilder = JsonPatchBuilder()
        val obj = mapper.readTree("""
            {
                "z": [
                    "1"
                ]
            }
        """.trimIndent())
        jsonPatchBuilder.addJsonNodeValue(obj, JsonPointer.compile("/x/0/y/firstName"), TextNode.valueOf("John"))
        jsonPatchBuilder.addJsonNodeValue(obj, JsonPointer.compile("/x/0/y/lastName"), TextNode.valueOf("Doe"))
        jsonPatchBuilder.addJsonNodeValue(obj, JsonPointer.compile("/x/+/y/birthYear"), TextNode.valueOf("2001"))
        jsonPatchBuilder.addJsonNodeValue(obj, JsonPointer.compile("/x/-/y/status"), TextNode.valueOf("Unknown"))
        jsonPatchBuilder.addJsonNodeValue(obj, JsonPointer.compile("/x/+/y/valid"), TextNode.valueOf("true"))
        jsonPatchBuilder.addJsonNodeValue(obj, JsonPointer.compile("/u/+/v/-/w"), TextNode.valueOf("value"))
        jsonPatchBuilder.addJsonNodeValue(obj, JsonPointer.compile("/z/-"), TextNode.valueOf("1"))
        jsonPatchBuilder.addJsonNodeValue(obj, JsonPointer.compile("/z/-"), TextNode.valueOf("2"))
        jsonPatchBuilder.addJsonNodeValue(obj, JsonPointer.compile("/z/+"), TextNode.valueOf("3"))
        JsonPatchService.apply(jsonPatchBuilder.build(), obj)
        assertEquals(
            """
            {
                "x": [
                    {
                        "y": {
                            "firstName": "John",
                            "lastName": "Doe",
                            "birthYear": "2001"
                        }
                    },
                    {
                        "y": {
                            "status": "Unknown",
                            "valid": "true"
                        }
                    }
                ],
                "u": [
                    {
                        "v": [
                            {
                                "w": "value"
                            }
                        ]
                    }
                ],
                "z": [
                    "1",
                    "1",
                    "2",
                    "3"
                ]
            }
            """, mapper.writeValueAsString(obj), JSONCompareMode.STRICT_ORDER
        )
    }

    @Test
    fun `should replace existing value node`() {
        val mapper = MapperSingleton.get()
        val jsonPatchBuilder = JsonPatchBuilder()
        val obj = mapper.readTree("""{"address":"Test street"}""")
        jsonPatchBuilder.addJsonNodeValue(obj, JsonPointer.compile("/address/streetName"), TextNode.valueOf("Funenpark"))
        JsonPatchService.apply(jsonPatchBuilder.build(), obj)
        assertEquals("""{"address":{"streetName":"Funenpark"}}""", mapper.writeValueAsString(obj), false)
    }

    @Test
    fun `should add new array when none exists`() {
        val mapper = MapperSingleton.get()
        val jsonPatchBuilder = JsonPatchBuilder()
        val obj = mapper.createObjectNode()

        jsonPatchBuilder.addJsonNodeValue(
            obj,
            JsonPointer.compile("/verzoeken/+/documenten/-"),
            TextNode.valueOf("https://my.document.url")
        )
        JsonPatchService.apply(jsonPatchBuilder.build(), obj)

        assertEquals("""
            {
                "verzoeken": [
                    {
                        "documenten": [
                            "https://my.document.url"
                        ]
                    }
                ]
            }
        """.trimIndent(), mapper.writeValueAsString(obj), JSONCompareMode.STRICT_ORDER)
    }
}