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

package com.ritense.objectenapi.service

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import com.ritense.objectenapi.client.ObjectRecord
import com.ritense.objectenapi.client.ObjectWrapper
import com.ritense.valtimo.contract.json.Mapper
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class ZaakObjectDataResolverTest {

    val zaakObjectService = mock<ZaakObjectService>()
    val resolver = ZaakObjectDataResolver(zaakObjectService, ObjectMapper())

    @Test
    fun `should support zaakobject prefix`() {
        assertTrue(resolver.supports("zaakobject"))
    }

    @Test
    fun `should not support random prefix`() {
        assertFalse(resolver.supports("something"))
    }

    @Test
    fun `should get data for requested fields`() {
        val documentId = UUID.randomUUID()
        val fieldsToRequest = arrayOf(
            "sometype:/path",
            "sometype:/nested/path",
            "typeWithColon: inName:/other-type-path"
        )

        // setup object of type sometype
        val object1 = mock<ObjectWrapper>()
        val nested = mapOf(
            "path" to "test-value-2"
        )
        val objectData1 = mapOf(
            "path" to "test-value-1",
            "nested" to nested
        )
        val objectDataJsonNode = Mapper.INSTANCE.get().valueToTree<JsonNode>(objectData1)
        val objectRecord1 = mock<ObjectRecord>()
        whenever(object1.record).thenReturn(objectRecord1)
        whenever(objectRecord1.data).thenReturn(objectDataJsonNode)
        whenever(zaakObjectService.getZaakObjectOfTypeByName(documentId, "sometype"))
            .thenReturn(object1)

        // setup object of type othertype
        val object2 = mock<ObjectWrapper>()
        val objectData2 = mapOf(
            "other-type-path" to "test-value-3"
        )
        val object2DataJsonNode = Mapper.INSTANCE.get().valueToTree<JsonNode>(objectData2)
        val objectRecord2 = mock<ObjectRecord>()
        whenever(object2.record).thenReturn(objectRecord2)
        whenever(objectRecord2.data).thenReturn(object2DataJsonNode)
        whenever(zaakObjectService.getZaakObjectOfTypeByName(documentId, "typeWithColon: inName"))
            .thenReturn(object2)

        val variableMap = resolver.get("something", documentId, *fieldsToRequest)

        assertEquals("test-value-1", variableMap["sometype:/path"])
        assertEquals("test-value-2", variableMap["sometype:/nested/path"])
        assertEquals("test-value-3", variableMap["typeWithColon: inName:/other-type-path"])

        verify(zaakObjectService, times(1)).getZaakObjectOfTypeByName(documentId, "sometype")
        verify(zaakObjectService, times(1)).getZaakObjectOfTypeByName(documentId, "typeWithColon: inName")
        verifyNoMoreInteractions(zaakObjectService)
    }

    @Test
    fun `should handle array field type property`() {
        val value = arrayOf("test1", "test2")
        val resolvedVariable = testForVariableType(value)
        assertTrue(ArrayNode::class.java.isAssignableFrom(resolvedVariable!!::class.java))
        val arrayValue = resolvedVariable as ArrayNode
        assertEquals("test1", arrayValue.get(0).textValue())
        assertEquals("test2", arrayValue.get(1).textValue())
    }

    @Test
    fun `should handle binary field type property`() {
        val value = "test".encodeToByteArray()
        val resolvedVariable = testForVariableType(value)
        assertEquals("test", String(resolvedVariable as ByteArray))
    }

    @Test
    fun `should handle boolean field type property`() {
        val value = true
        val resolvedVariable = testForVariableType(value)
        assertEquals(true, resolvedVariable)
    }

    @Test
    fun `should handle missing field type property`() {
        val resolvedVariable = testForValueMap(Mapper.INSTANCE.get().valueToTree(""))
        assertNull(resolvedVariable)
    }

    @Test
    fun `should handle null field type property`() {
        val value = null
        val resolvedVariable = testForVariableType(value)
        assertNull(resolvedVariable)
    }

    @Test
    fun `should handle number field type property`() {
        val value = 123L
        val resolvedVariable = testForVariableType(value)
        assertEquals(123L, resolvedVariable)
    }

    @Test
    fun `should handle object field type property`() {
        val value = mapOf(
            "test" to "test"
        )
        val resolvedVariable = testForVariableType(value)
        assertTrue(ObjectNode::class.java.isAssignableFrom(resolvedVariable!!::class.java))
        val arrayValue = resolvedVariable as ObjectNode
        assertEquals("test", arrayValue.get("test").textValue())
    }

    @Test
    fun `should handle string field type property`() {
        val value = "test"
        val resolvedVariable = testForVariableType(value)
        assertEquals("test", resolvedVariable)
    }

    private fun testForVariableType(value: Any?): Any? {
        return testForValueMap(
            Mapper.INSTANCE.get().valueToTree(
                mapOf(
                    "path" to value
                )
            )
        )
    }

    private fun testForValueMap(value: JsonNode): Any? {
        val documentId = UUID.randomUUID()
        val fieldsToRequest = arrayOf(
            "sometype:/path"
        )

        // setup object of type sometype
        val object1 = mock<ObjectWrapper>()
        val objectRecord1 = mock<ObjectRecord>()
        whenever(object1.record).thenReturn(objectRecord1)
        whenever(objectRecord1.data).thenReturn(value)
        whenever(zaakObjectService.getZaakObjectOfTypeByName(documentId, "sometype"))
            .thenReturn(object1)

        val variableMap = resolver.get("something", documentId, *fieldsToRequest)

        return variableMap["sometype:/path"]
    }
}
