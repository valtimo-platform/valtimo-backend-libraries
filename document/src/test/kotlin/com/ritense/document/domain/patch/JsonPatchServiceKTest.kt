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
package com.ritense.document.domain.patch

import com.fasterxml.jackson.core.JsonPointer
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.node.TextNode
import com.ritense.valtimo.contract.json.Mapper
import com.ritense.valtimo.contract.json.patch.JsonPatchBuilder
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert

class JsonPatchServiceKTest {
    @Test
    @Throws(JsonProcessingException::class)
    fun `should patch existing object`() {
        val mapper = Mapper.INSTANCE.get()
        val jsonPatchBuilder = JsonPatchBuilder()
        val obj = mapper.createObjectNode()
        jsonPatchBuilder.addJsonNodeValue(obj, JsonPointer.compile("/x/0/y/firstName"), TextNode.valueOf("John"))
        jsonPatchBuilder.addJsonNodeValue(obj, JsonPointer.compile("/x/0/y/lastName"), TextNode.valueOf("Doe"))
        jsonPatchBuilder.addJsonNodeValue(obj, JsonPointer.compile("/x/-/y/status"), TextNode.valueOf("Unknown"))
        jsonPatchBuilder.addJsonNodeValue(obj, JsonPointer.compile("/z/-"), TextNode.valueOf("1"))
        jsonPatchBuilder.addJsonNodeValue(obj, JsonPointer.compile("/z/-"), TextNode.valueOf("2"))
        JsonPatchService.apply(jsonPatchBuilder.build(), obj)
        JSONAssert.assertEquals(
            """
            {
                "x": [
                    {
                        "y": {
                            "firstName": "John",
                            "lastName": "Doe"
                        }
                    },
                    {
                        "y": {
                            "status": "Unknown"
                        }
                    }
                ],
                "z": [
                  "1",
                  "2"
                ]
            }
            """, mapper.writeValueAsString(obj), false
        )
    }
}