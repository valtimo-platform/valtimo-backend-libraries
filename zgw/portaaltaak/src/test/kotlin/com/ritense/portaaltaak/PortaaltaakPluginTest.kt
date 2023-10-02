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

package com.ritense.portaaltaak

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.ritense.document.domain.impl.JsonSchemaDocumentId
import com.ritense.objectenapi.ObjectenApiPlugin
import com.ritense.objectenapi.client.ObjectRequest
import com.ritense.objectmanagement.domain.ObjectManagement
import com.ritense.objectmanagement.service.ObjectManagementService
import com.ritense.objecttypenapi.ObjecttypenApiPlugin
import com.ritense.plugin.domain.PluginConfigurationId
import com.ritense.plugin.service.PluginService
import com.ritense.processdocument.service.ProcessDocumentService
import com.ritense.valueresolver.ValueResolverService
import com.ritense.zakenapi.ZakenApiPlugin
import com.ritense.zakenapi.domain.ZaakInstanceLink
import com.ritense.zakenapi.domain.ZaakInstanceLinkId
import com.ritense.zakenapi.domain.rol.BetrokkeneType
import com.ritense.zakenapi.domain.rol.Rol
import com.ritense.zakenapi.domain.rol.RolNatuurlijkPersoon
import com.ritense.zakenapi.domain.rol.RolNietNatuurlijkPersoon
import com.ritense.zakenapi.link.ZaakInstanceLinkService
import java.net.URI
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import org.camunda.bpm.engine.delegate.DelegateTask
import org.camunda.bpm.engine.delegate.VariableScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever


internal class PortaaltaakPluginTest {

    lateinit var objectManagementService: ObjectManagementService
    lateinit var pluginService: PluginService
    lateinit var valueResolverService: ValueResolverService
    lateinit var processDocumentService: ProcessDocumentService
    lateinit var zaakInstanceLinkService: ZaakInstanceLinkService
    lateinit var portaaltaakPlugin: PortaaltaakPlugin
    lateinit var zakenApiPlugin: ZakenApiPlugin
    val delegateTask = mock<DelegateTask>()
    val bsn = "688223436"
    val kvk = "12345678"
    val jsonSchemaDocumentId = mock<JsonSchemaDocumentId>()

    @BeforeEach
    fun init() {
        objectManagementService = mock()
        pluginService = mock()
        valueResolverService = mock()
        processDocumentService = mock()
        zaakInstanceLinkService = mock()
        zakenApiPlugin = mock()
        portaaltaakPlugin = PortaaltaakPlugin(
            objectManagementService,
            pluginService,
            valueResolverService,
            processDocumentService,
            zaakInstanceLinkService,
            mock()
        )
        portaaltaakPlugin.notificatiesApiPluginConfiguration = mock()
        portaaltaakPlugin.objectManagementConfigurationId = mock()
    }

    @Test
    fun `should create taak object in objects api`() {
        val formType = TaakFormType.ID
        val formTypeId = "formTypeId"
        val formTypeUrl = "formTypeUrl"
        val sendData = emptyList<DataBindingConfig>()
        val receiveData = emptyList<DataBindingConfig>()
        val receiver = TaakReceiver.ZAAK_INITIATOR
        val identificationKey = TaakIdentificatie.TYPE_BSN
        val zakenApiPlugin = mock<ZakenApiPlugin>()
        val objectenApiPlugin = mock<ObjectenApiPlugin>()
        val objecttypenApiPlugin = mock<ObjecttypenApiPlugin>()

        val objectManagement = getObjectManagement()
        val objectTypeUrl = URI("https://example.com/")

        whenever(objectManagementService.getById(any())).thenReturn(objectManagement)
        whenever(pluginService.createInstance(PluginConfigurationId(objectManagement.objectenApiPluginConfigurationId))).thenReturn(objectenApiPlugin)
        whenever(pluginService.createInstance(PluginConfigurationId(objectManagement.objecttypenApiPluginConfigurationId))).thenReturn(objecttypenApiPlugin)
        whenever(pluginService.createInstance(any<Class<ZakenApiPlugin>>(), any())).thenReturn(zakenApiPlugin)
        whenever(objecttypenApiPlugin.getObjectTypeUrlById(any())).thenReturn(objectTypeUrl)
        whenever(delegateTask.processInstanceId).thenReturn(UUID.randomUUID().toString())
        whenever(zaakInstanceLinkService.getByDocumentId(any())).thenReturn(getZaakInstanceLink())
        whenever(jsonSchemaDocumentId.id).thenReturn(UUID.randomUUID())
        whenever(processDocumentService.getDocumentId(any(), any<DelegateTask>())).thenReturn(jsonSchemaDocumentId)
        whenever(zakenApiPlugin.getZaakRollen(any(), any())).thenReturn(getRol(BetrokkeneType.NATUURLIJK_PERSOON))
        whenever(delegateTask.name).thenReturn("delegateTaskName")
        whenever(delegateTask.id).thenReturn("delegateTaskId")
        whenever(jsonSchemaDocumentId.toString()).thenReturn("documentId")
        whenever(valueResolverService.resolveValues(any(), any())).thenReturn(emptyMap())
        whenever(pluginService.getObjectMapper()).thenReturn(jacksonObjectMapper())

        portaaltaakPlugin.createPortaalTaak(
            delegateTask,
            formType,
            formTypeId,
            formTypeUrl,
            sendData,
            receiveData,
            receiver,
            identificationKey,
            bsn
        )

        val captor = argumentCaptor<ObjectRequest>()

        verify(objectenApiPlugin).createObject(captor.capture())

        val objectRequest = captor.firstValue
        assertEquals(objectTypeUrl, objectRequest.type)
        assertEquals(objectManagement.objecttypeVersion, objectRequest.record.typeVersion)

        val taakObject = jacksonObjectMapper()
            .treeToValue(objectRequest.record.data, TaakObject::class.java)

        assertNotNull(taakObject.identificatie)
        assertNotNull(taakObject.data)
        assertEquals(delegateTask.name, taakObject.title)
        assertEquals(TaakStatus.OPEN, taakObject.status)
        assertNotNull(taakObject.formulier)
        assertEquals(delegateTask.id, taakObject.verwerkerTaakId)
        assertNotNull(objectRequest.record.startAt)

    }

    @Test
    fun `should get the correct identification for case initiated by a citizen`() {
        val result =
            portaaltaakPlugin.getTaakIdentification(delegateTask, TaakReceiver.OTHER, TaakIdentificatie.TYPE_BSN, bsn)
        assertEquals("bsn", result.type)
        assertEquals(bsn, result.value)
    }

    @Test
    fun `should get the correct task identification for task initiated by other with a citizen service number`() {
        val result =
            portaaltaakPlugin.getTaakIdentification(delegateTask, TaakReceiver.OTHER, TaakIdentificatie.TYPE_BSN, bsn)
        assertEquals("bsn", result.type)
        assertEquals(bsn, result.value)
    }

    @Test
    fun `should get the correct task identification for task initiated by other with a kvk number`() {
        val result =
            portaaltaakPlugin.getTaakIdentification(
                delegateTask,
                TaakReceiver.OTHER,
                TaakIdentificatie.TYPE_KVK,
                kvk
            )
        assertEquals("kvk", result.type)
        assertEquals(kvk, result.value)
    }

    @Test
    fun `should throw exception when no task sender is available`() {
        val result =
            assertThrows<IllegalStateException> {
                portaaltaakPlugin.getTaakIdentification(delegateTask, TaakReceiver.OTHER, null, null)
            }
        assertEquals(
            "Other was chosen as taak receiver, but no identification key was chosen.",
            result.message
        )
    }

    @Test
    fun `should throw exception when no task sender value is available`() {
        val result =
            assertThrows<IllegalStateException> {
                portaaltaakPlugin.getTaakIdentification(
                    delegateTask,
                    TaakReceiver.OTHER,
                    TaakIdentificatie.TYPE_KVK,
                    null
                )
            }
        assertEquals(
            "Other was chosen as taak receiver, but no identification value was chosen.",
            result.message
        )
    }

    @Test
    fun `should get a correct taak identification`() {
        val processInstanceId = UUID.randomUUID().toString()
        whenever(delegateTask.processInstanceId).thenReturn(processInstanceId)
        whenever(processDocumentService.getDocumentId(any(), any<VariableScope>())).thenReturn(jsonSchemaDocumentId)
        whenever(jsonSchemaDocumentId.id).thenReturn(UUID.randomUUID())
        whenever(zaakInstanceLinkService.getByDocumentId(jsonSchemaDocumentId.id)).thenReturn(getZaakInstanceLink())
        whenever(pluginService.createInstance(any<Class<ZakenApiPlugin>>(), any())).thenReturn(zakenApiPlugin)
        whenever(
            zakenApiPlugin.getZaakRollen(any(), any())
        ).thenReturn(getRol(BetrokkeneType.NATUURLIJK_PERSOON))


        val result =
            portaaltaakPlugin.getTaakIdentification(delegateTask, TaakReceiver.ZAAK_INITIATOR, null, null)

        assertEquals("bsn", result.type)
        assertEquals(
            (getRol(BetrokkeneType.NATUURLIJK_PERSOON)[0].betrokkeneIdentificatie as RolNatuurlijkPersoon).inpBsn,
            result.value
        )
    }

    @Test
    fun `should throw exception when getting zaak initiator with invalid zaakUrl`() {
        val processInstanceId = UUID.randomUUID().toString()
        whenever(delegateTask.processInstanceId).thenReturn(processInstanceId)
        whenever(processDocumentService.getDocumentId(any(), any<VariableScope>())).thenReturn(jsonSchemaDocumentId)
        whenever(jsonSchemaDocumentId.id).thenReturn(UUID.randomUUID())
        whenever(zaakInstanceLinkService.getByDocumentId(jsonSchemaDocumentId.id)).thenReturn(getZaakInstanceLink())
        whenever(pluginService.createInstance(any<Class<ZakenApiPlugin>>(), any())).thenReturn(null)

        val result = assertThrows<IllegalArgumentException> {
            portaaltaakPlugin.getZaakinitiator(delegateTask)
        }
        assertEquals(
            "No plugin configuration was found for zaak with URL ${getZaakInstanceLink().zaakInstanceUrl}",
            result.message
        )
    }

    @Test
    fun `should throw exception when no rol was found for zaak url`() {
        val processInstanceId = UUID.randomUUID().toString()
        whenever(delegateTask.processInstanceId).thenReturn(processInstanceId)
        whenever(processDocumentService.getDocumentId(any(), any<VariableScope>())).thenReturn(jsonSchemaDocumentId)
        whenever(jsonSchemaDocumentId.id).thenReturn(UUID.randomUUID())
        whenever(zaakInstanceLinkService.getByDocumentId(jsonSchemaDocumentId.id)).thenReturn(getZaakInstanceLink())
        whenever(pluginService.createInstance(any<Class<ZakenApiPlugin>>(), any())).thenReturn(zakenApiPlugin)
        whenever(
            zakenApiPlugin.getZaakRollen(any(), any())
        ).thenReturn(emptyList())

        val result = assertThrows<IllegalArgumentException> {
            portaaltaakPlugin.getZaakinitiator(delegateTask)
        }
        assertEquals(
            "No initiator role found for zaak with URL ${getZaakInstanceLink().zaakInstanceUrl}",
            result.message
        )
    }

    @Test
    fun `should get a correct zaak initiator`() {
        val processInstanceId = UUID.randomUUID().toString()
        whenever(delegateTask.processInstanceId).thenReturn(processInstanceId)
        whenever(processDocumentService.getDocumentId(any(), any<VariableScope>())).thenReturn(jsonSchemaDocumentId)
        whenever(jsonSchemaDocumentId.id).thenReturn(UUID.randomUUID())
        whenever(zaakInstanceLinkService.getByDocumentId(jsonSchemaDocumentId.id)).thenReturn(getZaakInstanceLink())
        whenever(pluginService.createInstance(any<Class<ZakenApiPlugin>>(), any())).thenReturn(zakenApiPlugin)
        whenever(
            zakenApiPlugin.getZaakRollen(any(), any())
        ).thenReturn(getRol(BetrokkeneType.MEDEWERKER))

        val result = assertThrows<IllegalArgumentException> {
            portaaltaakPlugin.getZaakinitiator(delegateTask)
        }
        assertEquals(
            "Could not map initiator identificatie (value=${
                getRol(BetrokkeneType.MEDEWERKER)[0].betrokkeneIdentificatie
            }) for zaak with URL ${getZaakInstanceLink().zaakInstanceUrl} to TaakIdentificatie",
            result.message
        )

    }

    private fun getZaakInstanceLink(): ZaakInstanceLink {
        return ZaakInstanceLink(
            zaakInstanceLinkId = ZaakInstanceLinkId.newId(UUID.randomUUID()),
            zaakInstanceUrl = URI.create("aZaakInstanceUrl"),
            zaakInstanceId = UUID.randomUUID(),
            documentId = UUID.randomUUID(),
            zaakTypeUrl = URI.create("zaakTypeUrl")
        )
    }

    private fun getRol(betrokkeneType: BetrokkeneType): List<Rol> {
        return listOf(
            Rol(
                zaak = URI.create("zaakUri"),
                betrokkene = URI.create("betrokkeneUri"),
                betrokkeneType = betrokkeneType,
                roltype = URI.create("roltype"),
                roltoelichting = "",
                betrokkeneIdentificatie = when (betrokkeneType) {
                    BetrokkeneType.NATUURLIJK_PERSOON -> RolNatuurlijkPersoon(
                        inpBsn = "inpBsn"
                    )

                    BetrokkeneType.NIET_NATUURLIJK_PERSOON -> RolNietNatuurlijkPersoon(
                        "annIdentificatie"
                    )

                    else -> null
                }
            )
        )
    }

    private fun getObjectManagement(): ObjectManagement {
        return ObjectManagement(
            id = UUID.randomUUID(),
            title = "request",
            objecttypenApiPluginConfigurationId = UUID.randomUUID(),
            objecttypeId = "initiator",
            objectenApiPluginConfigurationId = UUID.randomUUID(),
            showInDataMenu = false
        )
    }
}
