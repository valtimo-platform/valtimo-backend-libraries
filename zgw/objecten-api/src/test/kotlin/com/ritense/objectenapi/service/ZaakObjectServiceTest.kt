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

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import com.ritense.objectenapi.ObjectenApiPlugin
import com.ritense.objectenapi.client.ObjectWrapper
import com.ritense.objecttypenapi.ObjecttypenApiPlugin
import com.ritense.objecttypenapi.client.Objecttype
import com.ritense.openzaak.domain.mapping.impl.ZaakInstanceLink
import com.ritense.openzaak.exception.ZaakInstanceLinkNotFoundException
import com.ritense.openzaak.service.ZaakInstanceLinkService
import com.ritense.plugin.service.PluginService
import com.ritense.zakenapi.ZakenApiPlugin
import com.ritense.zakenapi.domain.ZaakObject
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.net.URI
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class ZaakObjectServiceTest {

    val zaakInstanceLinkService = mock<ZaakInstanceLinkService>()
    val pluginService = mock<PluginService>()
    val zaakObjectService = ZaakObjectService(zaakInstanceLinkService, pluginService)

    var zaakPlugin: ZakenApiPlugin? = null
    var objectenApiPlugin: ObjectenApiPlugin? = null
    var objecttypenApiPlugin: ObjecttypenApiPlugin? = null

    var zaakObjecten = mutableListOf<ZaakObject>()

    @Test
    fun `should get objecttypes for document`() {
        val documentId = UUID.randomUUID()

        val zaakInstanceUrl = setupZaakInstanceLink(documentId)
        setupPlugins(zaakInstanceUrl)
        val objecttype1 = setupObjecttypeWithRelations(zaakInstanceUrl)
        val objecttype2 = setupObjecttypeWithRelations(zaakInstanceUrl)

        val zaakObjectTypes = zaakObjectService.getZaakObjectTypes(documentId)

        assertEquals(objecttype1, zaakObjectTypes[0])
        assertEquals(objecttype2, zaakObjectTypes[1])
    }

    @Test
    fun `should get only one objecttype when multiple objects are same type`() {
        val documentId = UUID.randomUUID()

        val zaakInstanceUrl = setupZaakInstanceLink(documentId)
        setupPlugins(zaakInstanceUrl)
        val objecttype = mock<Objecttype>()
        val objecttypeUrl = URI("http://example.com/objecttype/123")
        setupRelationsForObjecttype(zaakInstanceUrl, objecttype, objecttypeUrl)
        setupRelationsForObjecttype(zaakInstanceUrl, objecttype, objecttypeUrl)

        val zaakObjectTypes = zaakObjectService.getZaakObjectTypes(documentId)

        assertEquals(objecttype, zaakObjectTypes[0])
        assertEquals(1, zaakObjectTypes.size)
    }

    @Test
    fun `should throw exception if zaak instance link is not found`() {
        val documentId = UUID.randomUUID()
        whenever(zaakInstanceLinkService.getByDocumentId(documentId))
            .thenThrow(ZaakInstanceLinkNotFoundException::class.java)

        assertThrows(ZaakInstanceLinkNotFoundException::class.java) {
            zaakObjectService.getZaakObjectTypes(documentId)
        }
    }

    @Test
    fun `should throw exception if no plugin is found for zaak`() {
        val documentId = UUID.randomUUID()

        val zaakInstanceUrl = setupZaakInstanceLink(documentId)
        setupPlugins(zaakInstanceUrl)
        setupObjecttypeWithRelations(zaakInstanceUrl)
        whenever(pluginService.createInstance(eq(ZakenApiPlugin::class.java), any()))
            .thenReturn(null)

        val exception = assertThrows(IllegalArgumentException::class.java) {
            zaakObjectService.getZaakObjectTypes(documentId)
        }

        assertEquals("No plugin configuration was found for zaak with URL $zaakInstanceUrl", exception.message)
    }

    @Test
    fun `should skip object if no plugin is found for object`() {
        val documentId = UUID.randomUUID()

        val zaakInstanceUrl = setupZaakInstanceLink(documentId)
        setupPlugins(zaakInstanceUrl)
        setupObjecttypeWithRelations(zaakInstanceUrl)
        whenever(pluginService.createInstance(eq(ObjectenApiPlugin::class.java), any()))
            .thenReturn(null)

        val zaakObjectTypes = zaakObjectService.getZaakObjectTypes(documentId)

        assertTrue(zaakObjectTypes.isEmpty())
    }

    @Test
    fun `should skip objecttype if no plugin is found for objecttype`() {
        val documentId = UUID.randomUUID()

        val zaakInstanceUrl = setupZaakInstanceLink(documentId)
        setupPlugins(zaakInstanceUrl)
        setupObjecttypeWithRelations(zaakInstanceUrl)
        whenever(pluginService.createInstance(eq(ObjecttypenApiPlugin::class.java), any()))
            .thenReturn(null)

        val zaakObjectTypes = zaakObjectService.getZaakObjectTypes(documentId)

        assertTrue(zaakObjectTypes.isEmpty())
    }

    @Test
    fun `should get objects for document and objecttype`() {
        val documentId = UUID.randomUUID()
        val objecttypeUrl = URI("http://example.com/objecttype")

        val zaakInstanceUrl = setupZaakInstanceLink(documentId)
        setupPlugins(zaakInstanceUrl)
        val object1 = setupObjectWithRelations(zaakInstanceUrl, objecttypeUrl)
        val object2 = setupObjectWithRelations(zaakInstanceUrl, objecttypeUrl)

        val zaakObjects = zaakObjectService.getZaakObjecten(documentId, objecttypeUrl)

        assertEquals(object1, zaakObjects[0])
        assertEquals(object2, zaakObjects[1])
    }

    @Test
    fun `should not get objects for other objecttype`() {
        val documentId = UUID.randomUUID()
        val objecttypeUrl = URI("http://example.com/objecttype")
        val otherObjecttypeUrl = URI("http://example.com/objecttype/2")

        val zaakInstanceUrl = setupZaakInstanceLink(documentId)
        setupPlugins(zaakInstanceUrl)

        setupObjectWithRelations(zaakInstanceUrl, otherObjecttypeUrl)
        setupObjectWithRelations(zaakInstanceUrl, otherObjecttypeUrl)

        val zaakObjects = zaakObjectService.getZaakObjecten(documentId, objecttypeUrl)

        assertTrue(zaakObjects.isEmpty())
    }


    private fun setupZaakInstanceLink(documentId: UUID): URI {
        val zaakInstanceUrl = URI("http://example.com/zaak/${UUID.randomUUID()}")
        val zaakInstanceLink = mock<ZaakInstanceLink>()
        whenever(zaakInstanceLink.zaakInstanceUrl).thenReturn(zaakInstanceUrl)
        whenever(zaakInstanceLinkService.getByDocumentId(documentId)).thenReturn(zaakInstanceLink)
        return zaakInstanceUrl
    }

    private fun setupPlugins(zaakUrl: URI) {
        zaakPlugin = mock()
        whenever(pluginService.createInstance(eq(ZakenApiPlugin::class.java), any()))
            .thenReturn(zaakPlugin)

        objectenApiPlugin = mock()
        whenever(pluginService.createInstance(eq(ObjectenApiPlugin::class.java), any()))
            .thenReturn(objectenApiPlugin)

        objecttypenApiPlugin = mock()
        whenever(pluginService.createInstance(eq(ObjecttypenApiPlugin::class.java), any()))
            .thenReturn(objecttypenApiPlugin)

        whenever(zaakPlugin?.getZaakObjecten(zaakUrl)).thenReturn(zaakObjecten)
    }

    private fun setupObjecttypeWithRelations(zaakUrl: URI): Objecttype {
        val zaakobject = setupZaakObject(zaakUrl)
        val objectWrapper = setupObject(zaakobject)
        return setupObjecttype(objectWrapper)
    }

    private fun setupRelationsForObjecttype(zaakUrl: URI, objecttype: Objecttype, objecttypeUrl: URI): Objecttype {
        val zaakobject = setupZaakObject(zaakUrl)
        val objectWrapper = setupObject(zaakobject, objecttypeUrl)
        return mockObjecttypeRetrieval(objectWrapper, objecttype)
    }

    private fun setupObjectWithRelations(zaakUrl: URI, objecttypeUrl: URI): ObjectWrapper {
        val zaakobject = setupZaakObject(zaakUrl)
        return setupObject(zaakobject, objecttypeUrl)
    }

    private fun setupZaakObject(zaakUrl: URI): ZaakObject{
        val zaakObject = mock<ZaakObject>()
        val objectUrl = URI("http://example.com/object/${UUID.randomUUID()}")
        whenever(zaakObject.objectUrl).thenReturn(objectUrl)
        zaakObjecten.add(zaakObject)
        return zaakObject
    }

    private fun setupObject(zaakObject: ZaakObject, objecttypeUrl: URI? = null): ObjectWrapper{
        val objectWrapper = mock<ObjectWrapper>()
        val defaultObjecttypeUrl = URI("http://example.com/objecttype/${UUID.randomUUID()}")
        whenever(objectWrapper.type).thenReturn(objecttypeUrl?:defaultObjecttypeUrl)
        whenever(objectenApiPlugin?.getObject(zaakObject.objectUrl)).thenReturn(objectWrapper)
        return objectWrapper
    }

    private fun setupObjecttype(referringObject: ObjectWrapper): Objecttype {
        val objecttype = mock<Objecttype>()
        mockObjecttypeRetrieval(referringObject, objecttype)
        return objecttype
    }

    private fun mockObjecttypeRetrieval(referringObject: ObjectWrapper, objecttype: Objecttype): Objecttype {
        whenever(objecttypenApiPlugin?.getObjecttype(referringObject.type)).thenReturn(objecttype)
        return objecttype
    }
}