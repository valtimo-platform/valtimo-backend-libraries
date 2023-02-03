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

import com.ritense.form.domain.FormIoFormDefinition
import com.ritense.form.service.FormDefinitionService
import com.ritense.objectenapi.ObjectenApiPlugin
import com.ritense.objectenapi.client.ObjectRecord
import com.ritense.objectenapi.client.ObjectWrapper
import com.ritense.objectenapi.web.rest.ObjectManagementProvider
import com.ritense.objecttypenapi.ObjecttypenApiPlugin
import com.ritense.objecttypenapi.client.Objecttype
import com.ritense.openzaak.exception.ZaakInstanceLinkNotFoundException
import com.ritense.openzaak.service.ZaakInstanceLinkService
import com.ritense.plugin.service.PluginService
import com.ritense.valtimo.contract.json.Mapper
import com.ritense.zakenapi.ZakenApiPlugin
import com.ritense.zakenapi.domain.ZaakInstanceLink
import com.ritense.zakenapi.domain.ZaakObject
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.net.URI
import java.util.Optional
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class ZaakObjectServiceTest {

    val zaakInstanceLinkService = mock<ZaakInstanceLinkService>()
    val pluginService = mock<PluginService>()
    val formDefinitionService = mock<FormDefinitionService>()
    val objectManagementProvider = mock<ObjectManagementProvider>()
    val zaakObjectService = ZaakObjectService(zaakInstanceLinkService, pluginService, formDefinitionService, objectManagementProvider)

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
        setupObjectForObjecttype(zaakInstanceUrl, objecttype, objecttypeUrl)
        setupObjectForObjecttype(zaakInstanceUrl, objecttype, objecttypeUrl)

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

        val zaakObjects = zaakObjectService.getZaakObjectenOfType(documentId, objecttypeUrl)

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

        val zaakObjects = zaakObjectService.getZaakObjectenOfType(documentId, objecttypeUrl)

        assertTrue(zaakObjects.isEmpty())
    }

    @Test
    fun `should get zaakobject of correct type`() {
        val documentId = UUID.randomUUID()
        val objecttypeName = "test"
        val objecttypeUrl = URI("http://example.com/objecttype")
        val otherObjecttypeUrl = URI("http://example.com/objecttype/2")

        val zaakInstanceUrl = setupZaakInstanceLink(documentId)
        setupPlugins(zaakInstanceUrl)

        //set up object that should not be found
        val otherObjecttype = mock<Objecttype>()
        whenever(otherObjecttype.name).thenReturn("other")
        setupObjectForObjecttype(zaakInstanceUrl, otherObjecttype, otherObjecttypeUrl)

        //set up object with correct type name that should be found
        val objecttype = mock<Objecttype>()
        whenever(objecttype.name).thenReturn(objecttypeName)
        val objectOfType = setupObjectForObjecttype(zaakInstanceUrl, objecttype, objecttypeUrl)

        val resultObject = zaakObjectService.getZaakObjectOfTypeByName(documentId, objecttypeName)

        assertEquals(objectOfType, resultObject)
    }

    @Test
    fun `should throw exception when there are multiple types with same name`() {
        val documentId = UUID.randomUUID()
        val objecttypeName = "test"
        val objecttypeUrl = URI("http://example.com/objecttype")
        val otherObjecttypeUrl = URI("http://example.com/objecttype/2")

        val zaakInstanceUrl = setupZaakInstanceLink(documentId)
        setupPlugins(zaakInstanceUrl)

        //set up objecttype
        val objecttype = mock<Objecttype>()
        whenever(objecttype.name).thenReturn(objecttypeName)
        setupObjectForObjecttype(zaakInstanceUrl, objecttype, objecttypeUrl)

        //set up other objecttype with same name but different url
        val otherObjecttype = mock<Objecttype>()
        whenever(otherObjecttype.name).thenReturn(objecttypeName)
        setupObjectForObjecttype(zaakInstanceUrl, otherObjecttype, otherObjecttypeUrl)

        val exception = assertThrows(IllegalStateException::class.java) {
            zaakObjectService.getZaakObjectOfTypeByName(documentId, objecttypeName)
        }

        assertEquals("More than one objecttype with name 'test' was found for document $documentId",
            exception.message)
    }

    @Test
    fun `should throw exception when getting object by type and there are multiple objects`() {
        val documentId = UUID.randomUUID()
        val objecttypeName = "test"
        val objecttypeUrl = URI("http://example.com/objecttype")

        val zaakInstanceUrl = setupZaakInstanceLink(documentId)
        setupPlugins(zaakInstanceUrl)

        //set up objecttype with multiple objects for that type
        val objecttype = mock<Objecttype>()
        whenever(objecttype.name).thenReturn(objecttypeName)
        setupObjectForObjecttype(zaakInstanceUrl, objecttype, objecttypeUrl)
        setupObjectForObjecttype(zaakInstanceUrl, objecttype, objecttypeUrl)

        val exception = assertThrows(IllegalStateException::class.java) {
            zaakObjectService.getZaakObjectOfTypeByName(documentId, objecttypeName)
        }

        assertEquals("More than one object of type 'test' was found for document $documentId",
            exception.message)
    }

    @Test
    fun `should throw exception when getting object by type and there are no objects`() {
        val documentId = UUID.randomUUID()
        val objecttypeName = "test"
        val objecttypeUrl = URI("http://example.com/objecttype")

        val zaakInstanceUrl = setupZaakInstanceLink(documentId)
        setupPlugins(zaakInstanceUrl)

        val exception = assertThrows(IllegalStateException::class.java) {
            zaakObjectService.getZaakObjectOfTypeByName(documentId, objecttypeName)
        }

        assertEquals("No object was found of type 'test' for document $documentId",
            exception.message)
    }

    @Test
    fun `should get prefilled form for object`() {
        val zaakInstanceUrl = URI("http://not-relevant")
        val objectUrl = URI("http://example.com/object")
        val objecttypeUrl = URI("http://example.com/objecttype")
        val objecttypeName = "some-type"

        setupPlugins(zaakInstanceUrl)
        val theObject = setupObjectWithRelations(zaakInstanceUrl, objecttypeUrl)
        val objecttype = setupObjecttype(theObject)
        whenever(objecttype.name).thenReturn(objecttypeName)
        whenever(objectenApiPlugin?.getObject(objectUrl)).thenReturn(theObject)

        val objectRecord = mock<ObjectRecord>()
        whenever(theObject.record).thenReturn(objectRecord)
        val objectData = Mapper.INSTANCE.get().readTree("""
            {
              "test": "test-value"
            }
        """.trimIndent())
        whenever(objectRecord.data).thenReturn(objectData)

        val formDefinition = FormIoFormDefinition(
            UUID.randomUUID(),
            "form-name",
            """
                {
                    "display": "form",
                    "settings": {},
                    "components": [
                        {
                            "label": "Test",
                            "key": "test",
                            "type": "textfield",
                            "input": true
                        }
                    ]
                }
            """.trimIndent(),
            false
        )
        whenever(formDefinitionService.getFormDefinitionByNameIgnoringCase("some-type.editform"))
            .thenReturn(Optional.of(formDefinition))

        val resultingForm = zaakObjectService.getZaakObjectForm(objectUrl)

        assertNotNull(resultingForm)
        assertEquals("test-value", resultingForm.formDefinition.get("components").get(0).get("defaultValue").textValue())
    }

    @Test
    fun `should return null when getting form for object and object does not exist`() {
        val zaakInstanceUrl = URI("http://not-relevant")
        val objectUrl = URI("http://example.com/object")

        setupPlugins(zaakInstanceUrl)
        whenever(objectenApiPlugin?.getObject(objectUrl)).thenReturn(null)

        val resultingForm = zaakObjectService.getZaakObjectForm(objectUrl)

        verify(objectenApiPlugin)?.getObject(any())
        assertNull(resultingForm)
    }

    @Test
    fun `should return null when getting form for object and objecttype does not exist`() {
        val zaakInstanceUrl = URI("http://not-relevant")
        val objectUrl = URI("http://example.com/object")
        val objecttypeUrl = URI("http://example.com/objecttype")

        setupPlugins(zaakInstanceUrl)
        val theObject = setupObjectWithRelations(zaakInstanceUrl, objecttypeUrl)
        whenever(objectenApiPlugin?.getObject(objectUrl)).thenReturn(theObject)

        val objectRecord = mock<ObjectRecord>()
        whenever(theObject.record).thenReturn(objectRecord)
        val objectData = Mapper.INSTANCE.get().readTree("""
            {
              "test": "test-value"
            }
        """.trimIndent())
        whenever(objectRecord.data).thenReturn(objectData)

        val resultingForm = zaakObjectService.getZaakObjectForm(objectUrl)

        verify(objecttypenApiPlugin)?.getObjecttype(any())
        assertNull(resultingForm)
    }

    @Test
    fun `should return null when getting form for object and form does not exist`() {
        val zaakInstanceUrl = URI("http://not-relevant")
        val objectUrl = URI("http://example.com/object")
        val objecttypeUrl = URI("http://example.com/objecttype")
        val objecttypeName = "some-type"

        setupPlugins(zaakInstanceUrl)
        val theObject = setupObjectWithRelations(zaakInstanceUrl, objecttypeUrl)
        val objecttype = setupObjecttype(theObject)
        whenever(objecttype.name).thenReturn(objecttypeName)
        whenever(objectenApiPlugin?.getObject(objectUrl)).thenReturn(theObject)

        val objectRecord = mock<ObjectRecord>()
        whenever(theObject.record).thenReturn(objectRecord)
        val objectData = Mapper.INSTANCE.get().readTree("""
            {
              "test": "test-value"
            }
        """.trimIndent())
        whenever(objectRecord.data).thenReturn(objectData)

        whenever(formDefinitionService.getFormDefinitionByNameIgnoringCase("some-type.editform")).thenReturn(null)

        val resultingForm = zaakObjectService.getZaakObjectForm(objectUrl)

        verify(formDefinitionService).getFormDefinitionByNameIgnoringCase("some-type.editform")
        assertNull(resultingForm)
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

    private fun setupObjectForObjecttype(zaakUrl: URI, objecttype: Objecttype, objecttypeUrl: URI): ObjectWrapper {
        val zaakobject = setupZaakObject(zaakUrl)
        val objectWrapper = setupObject(zaakobject, objecttypeUrl)
        mockObjecttypeRetrieval(objectWrapper, objecttype)
        return objectWrapper
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
