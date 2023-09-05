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

package com.ritense.objectenapi.service

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.objectenapi.client.ObjectWrapper
import com.ritense.processdocument.service.ProcessDocumentService
import com.ritense.valtimo.contract.json.Mapper
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class ZaakObjectValueResolverFactoryTest {

    val zaakObjectService = mock<ZaakObjectService>()
    val processDocumentService = mock<ProcessDocumentService>()
    val resolverFactory = ZaakObjectValueResolverFactory(zaakObjectService, ObjectMapper(), processDocumentService)

    @Test
    fun `should support zaakobject prefix`() {
        assertTrue(resolverFactory.supportedPrefix() == "zaakobject")
    }

    @Test
    fun `should get data for requested fields`() {
        val documentId = UUID.randomUUID()
        val object1 = mock<ObjectWrapper>(defaultAnswer = Mockito.RETURNS_DEEP_STUBS)
        val objectData1 = mapOf(
            "path" to "test-value-1",
            "nested" to mapOf(
                "path" to "test-value-2"
            )
        )
        whenever(object1.record.data).thenReturn(Mapper.INSTANCE.get().valueToTree(objectData1))
        whenever(zaakObjectService.getZaakObjectOfTypeByName(documentId, "sometype"))
            .thenReturn(object1)

        val resolver = resolverFactory.createResolver(documentId.toString())

        assertEquals("test-value-1", resolver.apply("sometype:/path"))
        assertEquals("test-value-2", resolver.apply("sometype:/nested/path"))

        verify(zaakObjectService, times(2)).getZaakObjectOfTypeByName(documentId, "sometype")
        verifyNoMoreInteractions(zaakObjectService)
    }
}
