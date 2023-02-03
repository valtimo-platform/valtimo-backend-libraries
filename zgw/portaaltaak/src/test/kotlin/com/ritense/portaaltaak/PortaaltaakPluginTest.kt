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

package com.ritense.portaaltaak

import com.ritense.document.domain.impl.JsonSchemaDocumentId
import com.ritense.objectenapi.ObjectenApiPlugin
import com.ritense.objectmanagement.domain.ObjectManagement
import com.ritense.objectmanagement.service.ObjectManagementService
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
import com.ritense.zakenapi.link.ZaakInstanceLinkService
import java.net.URI
import java.util.*
import org.camunda.bpm.engine.delegate.DelegateTask
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever


internal class PortaaltaakPluginTest {
    lateinit var objectManagementService: ObjectManagementService
    lateinit var pluginService: PluginService
    lateinit var valueResolverService: ValueResolverService
    lateinit var processDocumentService: ProcessDocumentService
    lateinit var zaakInstanceLinkService: ZaakInstanceLinkService
    lateinit var portaaltaakPlugin: PortaaltaakPlugin

    @BeforeEach
    fun init() {
        objectManagementService = mock()
        pluginService = mock()
        valueResolverService = mock()
        processDocumentService = mock()
        zaakInstanceLinkService = mock()
        portaaltaakPlugin = PortaaltaakPlugin(
            objectManagementService,
            pluginService,
            valueResolverService,
            processDocumentService,
            zaakInstanceLinkService
        )
        portaaltaakPlugin.notificatiesApiPluginConfiguration = mock()
        portaaltaakPlugin.objectManagementConfigurationId = mock()
    }

    @Test
    fun `should create taak object`() {
        //todo incomplete
        val delegateTask = mock<DelegateTask>()
        val formType = TaakFormType.ID
        val formTypeId = "formTypeId"
        val formTypeUrl = "formTypeUrl"
        val sendData = emptyList<DataBindingConfig>()
        val receiveData = emptyList<DataBindingConfig>()
        val receiver = TaakReceiver.ZAAK_INITIATOR
        val otherReceiver = OtherTaakReceiver.BSN
        val kvk = null
        val bsn = "9999999999"
        val zakenApiPlugin = mock<ZakenApiPlugin>()
        val objectenApiPlugin = mock<ObjectenApiPlugin>()
        val jsonSchemaDocumentId = mock<JsonSchemaDocumentId>()

        whenever(objectManagementService.getById(any())).thenReturn(getObjectManagement())
        whenever(pluginService.createInstance(any<PluginConfigurationId>())).thenReturn(objectenApiPlugin)
        whenever(pluginService.createInstance(any<Class<ZakenApiPlugin>>(), any())).thenReturn(zakenApiPlugin)
        whenever(delegateTask.processInstanceId).thenReturn(UUID.randomUUID().toString())
        whenever(zaakInstanceLinkService.getByDocumentId(any())).thenReturn(getZaakInstanceLink())
        whenever(jsonSchemaDocumentId.id).thenReturn(UUID.randomUUID())
        whenever(processDocumentService.getDocumentId(any(), any<DelegateTask>())).thenReturn(jsonSchemaDocumentId)
        whenever(zakenApiPlugin.getZaakRollen(any(), any())).thenReturn(createRol())
        whenever(delegateTask.name).thenReturn("delegateTaskName")
        whenever(delegateTask.id).thenReturn("delegateTaskId")
        whenever(jsonSchemaDocumentId.toString()).thenReturn("documentId")
        whenever(valueResolverService.resolveValues(any(), any())).thenReturn(emptyMap())

        portaaltaakPlugin.createPortaalTaak(
            delegateTask,
            formType,
            formTypeId,
            formTypeUrl,
            sendData,
            receiveData,
            receiver,
            otherReceiver,
            kvk,
            bsn
        )
        verify(pluginService, times(1)).createInstance(any<PluginConfigurationId>())
        verify(pluginService, times(1)).createInstance(any<Class<ZakenApiPlugin>>(), any())
        verify(objectManagementService, times(1)).getById(any())
        verify(delegateTask, times(2)).processInstanceId
        verify(delegateTask, times(1)).id
        verify(delegateTask, times(1)).name
        verify(zaakInstanceLinkService, times(1)).getByDocumentId(any())
        verify(jsonSchemaDocumentId, times(1)).id
        verify(processDocumentService, times(2)).getDocumentId(any(), any<DelegateTask>())
        verify(zakenApiPlugin, times(1)).getZaakRollen(any(), any())
        verify(valueResolverService, times(1)).resolveValues(any(), any())

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

    private fun createRol(): List<Rol> {
        return listOf(
            Rol(
                zaak = URI.create("zaakUri"),
                betrokkene = URI.create("betrokkeneUri"),
                betrokkeneType = BetrokkeneType.NATUURLIJK_PERSOON,
                roltype = URI.create("roltype"),
                roltoelichting = "",
                betrokkeneIdentificatie = RolNatuurlijkPersoon(
                    inpBsn = "inpBsn"
                )
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
