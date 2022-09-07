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

package com.ritense.objectenapi.listener

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.IntNode
import com.fasterxml.jackson.databind.node.TextNode
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.ritense.objectenapi.ObjectenApiPlugin
import com.ritense.objectenapi.client.ObjectRecord
import com.ritense.objectenapi.client.ObjectRequest
import com.ritense.objectenapi.client.ObjectWrapper
import com.ritense.objectenapi.client.ObjectenApiClient
import com.ritense.objectenapi.service.ZaakObjectService
import com.ritense.objecttypenapi.client.Objecttype
import com.ritense.plugin.service.PluginService
import com.ritense.valtimo.contract.event.ExternalDataSubmittedEvent
import com.ritense.valtimo.contract.json.Mapper
import org.junit.jupiter.api.Test
import java.net.URI
import java.time.LocalDate
import java.util.UUID
import kotlin.test.assertEquals

class ZaakObjectListenerTest {

    val zaakObjectService = mock<ZaakObjectService>()
    val pluginService = mock<PluginService>()
    val objectenApiClient = mock<ObjectenApiClient>()
    val zaakObjectListener = ZaakObjectListener(pluginService, zaakObjectService)
    val objectenApiPlugin = mock<ObjectenApiPlugin>()

    @Test
    fun `should update zaakobject fields`() {
        val documentId = UUID.randomUUID()
        val objectUrl =  URI.create("http://object.url.com")
        val objecttypeUrl = URI.create("http://objecttype.url.com")
        val currentIndexVersion = 1
        val currentTypeVersion = 2

        val currentZaakObjectData = mapOf("person" to
            mapOf("firstname" to TextNode("Current"),
                  "lastname" to TextNode("Value")))
        val zaakObjectJsonData =  Mapper.INSTANCE.get().valueToTree<JsonNode>(currentZaakObjectData)
        val objectWrapper = ObjectWrapper(
            objectUrl,
            UUID.randomUUID(),
            objecttypeUrl,
            ObjectRecord(
                index = currentIndexVersion,
                typeVersion = currentTypeVersion,
                data = zaakObjectJsonData,
                startAt = LocalDate.now()
            )
        )

        val eventData = mapOf("zaakobject" to
            mapOf("objecttype:/person/firstname" to TextNode("John"),
                  "objecttype:/person/lastname" to TextNode("Doe"),
                  "objecttype:/person/age" to IntNode(30)))
        val event = ExternalDataSubmittedEvent(
            data = eventData,
            documentDefinition = "documentDefinition",
            documentId = documentId
        )

        val objectType = Objecttype(
            objecttypeUrl,
            UUID.randomUUID(),
            "objecttype",
            "objecttype",
            null, null, null, null, null, null, null,
            null, null, null, null, null, null, null
        )

        whenever(zaakObjectService.getZaakObjectOfTypeByName(documentId, "objecttype")).thenReturn(objectWrapper)
        whenever(zaakObjectService.getZaakObjectTypes(documentId)).thenReturn(listOf(objectType))
        whenever(pluginService.createInstance(eq(ObjectenApiPlugin::class.java), any())).thenReturn(objectenApiPlugin)
        whenever(objectenApiClient.objectUpdate(any(), any(), any())).thenReturn(objectWrapper)

        zaakObjectListener.handle(event)

        // Verify the object is updated using the objectenApiPlugin and it contains the new values for firstname and lastname
        val objectRequestCaptor = argumentCaptor<ObjectRequest>()
        verify(objectenApiPlugin).objectUpdate(any(), objectRequestCaptor.capture())

        val capturedObjectRequest = objectRequestCaptor.firstValue
        assertEquals(objecttypeUrl, capturedObjectRequest.type)
        val capturedObjectRecord = capturedObjectRequest.record
        assertEquals(currentTypeVersion, capturedObjectRecord.typeVersion)
        assertEquals(currentIndexVersion.toString(), capturedObjectRecord.correctionFor)
        assertEquals("John", capturedObjectRecord.data!!.get("person").get("firstname").textValue())
        assertEquals("Doe", capturedObjectRecord.data!!.get("person").get("lastname").textValue())
        assertEquals(true, capturedObjectRecord.data!!.get("person").get("age").isInt)
        assertEquals(30, capturedObjectRecord.data!!.get("person").get("age").intValue())
    }
}