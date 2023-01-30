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

package com.ritense.verzoek

import com.fasterxml.jackson.core.JsonPointer
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.ritense.BaseIntegrationTest
import com.ritense.document.service.DocumentDefinitionService
import com.ritense.document.service.DocumentService
import com.ritense.document.service.result.DeployDocumentDefinitionResult
import com.ritense.notificatiesapi.event.NotificatiesApiNotificationReceivedEvent
import com.ritense.objectenapi.ObjectenApiPlugin
import com.ritense.objectenapi.client.ObjectRecord
import com.ritense.objectenapi.client.ObjectWrapper
import com.ritense.objectmanagement.domain.ObjectManagement
import com.ritense.objectmanagement.service.ObjectManagementService
import com.ritense.openzaak.domain.request.CreateZaakTypeLinkRequest
import com.ritense.openzaak.service.ZaakTypeLinkService
import com.ritense.plugin.domain.PluginConfiguration
import com.ritense.plugin.domain.PluginConfigurationId
import com.ritense.plugin.service.PluginService
import com.ritense.processdocument.service.ProcessDocumentService
import java.net.URI
import java.time.LocalDate
import java.util.*
import javax.transaction.Transactional
import org.camunda.bpm.engine.RuntimeService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.doCallRealMethod
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.spy
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired

@Transactional
internal class VerzoekPluginEventListenerIntTest : BaseIntegrationTest() {

    private val bsn: String = "999999999"
    private val initiatoRolType: String = "https://example.com/my-role-type"
    private val rsin: String = "637549971"
    private val objectType: String = "anObjectType"
    private val zaakTypeUrl: String = "http://example.gov"
    private val verzoekObjectType = "objection"

    lateinit var documentDefinition: DeployDocumentDefinitionResult
    lateinit var pluginServiceSpy: PluginService
    lateinit var notificatiesApiPluginConfiguration: PluginConfiguration
    lateinit var verzoekPluginEventListener: VerzoekPluginEventListener
    lateinit var objectManagement: ObjectManagement


    @Autowired
    lateinit var processDocumentService: ProcessDocumentService

    @Autowired
    lateinit var objectManagementService: ObjectManagementService

    @Autowired
    lateinit var documentService: DocumentService

    @Autowired
    lateinit var pluginService: PluginService

    @Autowired
    lateinit var documentDefinitionService: DocumentDefinitionService

    @Autowired
    lateinit var zaakTypeLinkService: ZaakTypeLinkService

    @Autowired
    lateinit var processService: RuntimeService

    @BeforeEach
    fun init() {
        pluginServiceSpy = spy(pluginService)
        verzoekPluginEventListener = VerzoekPluginEventListener(
            pluginServiceSpy,
            objectManagementService,
            documentService,
            zaakTypeLinkService,
            processDocumentService
        )

        documentDefinition = documentDefinitionService.deploy(
            """
                {
                    "${"$"}id": "verzoek-document-definition.schema",
                    "${"$"}schema": "http://json-schema.org/draft-07/schema#",
                    "type": "object",
                    "properties": {
                        "name": {
                            "type": "string"
                        }
                    }
                } 
        """.trimIndent()
        )
        assert(documentDefinition.documentDefinition() != null)

        objectManagement = objectManagementService.create(createObjectManagement())

        zaakTypeLinkService.createZaakTypeLink(
            CreateZaakTypeLinkRequest(
                documentDefinition.documentDefinition().id().name(),
                URI.create(zaakTypeUrl),
                false
            )
        )

        val authenticationPluginConfiguration = createPluginConfiguration(
            "notificatiesapiauthentication", """
            {
              "clientId": "my-client-id",
              "clientSecret": "my-client-secret"
            }
        """.trimIndent()
        )

        notificatiesApiPluginConfiguration = createPluginConfiguration(
            "notificatiesapi", """
            {
              "url": "https://example.com/my-notificatie-api-url",
              "authenticationPluginConfiguration": "${authenticationPluginConfiguration.id.id}"
            }
        """.trimIndent()
        )
    }

    @Test
    fun `should start process after receiving notification`() {
        createPluginConfiguration(
            "verzoek",
            """
            {
              "notificatiesApiPluginConfiguration": "${notificatiesApiPluginConfiguration.id.id}",
              "objectManagementId": "${objectManagement.id}",
              "systemProcessDefinitionKey": "verzoek-process",
              "rsin": "${rsin}",
              "verzoekProperties": [{
                "type": "objection",
                "caseDefinitionName": "${documentDefinition.documentDefinition().id().name()}",
                "processDefinitionKey": "objection-process",
                "initiatorRoltypeUrl": "$initiatoRolType",
                "initiatorRolDescription": "Initiator"
              }]
            }
            """.trimIndent()
        )
        //mocks
        val mockObjectenApiPlugin = mock<ObjectenApiPlugin>()
        doCallRealMethod().whenever(pluginServiceSpy).createInstance(any<Class<VerzoekPlugin>>(), any())

        doReturn(mockObjectenApiPlugin).whenever(pluginServiceSpy).createInstance(any<PluginConfigurationId>())
        doReturn(createObjectWrapper(withMetaData = true, verzoekObjectType, true)).whenever(mockObjectenApiPlugin)
            .getObject(any())
        //tested method
        verzoekPluginEventListener.createZaakFromNotificatie(createEvent())

        //assertions
        val processList = processService.createProcessInstanceQuery().processDefinitionKey("verzoek-process").list()
        assertEquals(1, processList.size)
        val processVariableMap =
            processService.createVariableInstanceQuery()
                .processInstanceIdIn(processList[0].id).list().map { it.name to it.value }.toMap()
        assertEquals(rsin, processVariableMap.get("RSIN"))
        assertEquals(zaakTypeUrl, processVariableMap.get("zaakTypeUrl"))
        assertEquals(initiatoRolType, processVariableMap.get("rolTypeUrl"))
        assertEquals(createEvent().resourceUrl, processVariableMap.get("verzoekObjectUrl"))
        assertEquals("bsn", processVariableMap.get("initiatorType"))
        assertEquals(bsn, processVariableMap.get("initiatorValue"))

        val documentInstance = documentService.get(processList[0].businessKey)
        assertEquals(
            "Luis Suarez",
            documentInstance.content().getValueBy(JsonPointer.valueOf("/name")).get().textValue()
        )
    }

    @Test
    fun `should throw exception when data is not present on retrieved object`() {
        createPluginConfiguration(
            "verzoek",
            """
            {
              "notificatiesApiPluginConfiguration": "${notificatiesApiPluginConfiguration.id.id}",
              "objectManagementId": "${objectManagement.id}",
              "systemProcessDefinitionKey": "verzoek-process",
              "rsin": "${rsin}",
              "verzoekProperties": [{
                "type": "objection",
                "caseDefinitionName": "${documentDefinition.documentDefinition().id().name()}",
                "processDefinitionKey": "objection-process",
                "initiatorRoltypeUrl": "$initiatoRolType",
                "initiatorRolDescription": "Initiator"
              }]
            }
            """.trimIndent()
        )
        //mocks
        val mockObjectenApiPlugin = mock<ObjectenApiPlugin>()
        doCallRealMethod().whenever(pluginServiceSpy).createInstance(any<Class<VerzoekPlugin>>(), any())
        doReturn(mockObjectenApiPlugin).whenever(pluginServiceSpy).createInstance(any<PluginConfigurationId>())
        doReturn(createObjectWrapper(withMetaData = false, verzoekObjectType, true)).whenever(mockObjectenApiPlugin)
            .getObject(any())
        //tested method
        val exception = assertThrows<RuntimeException> {
            verzoekPluginEventListener.createZaakFromNotificatie(createEvent())
        }
        //assertions
        assertEquals("Verzoek meta data was empty!", exception.message)
    }

    @Test
    fun `should throw exception when types do not match`() {
        createPluginConfiguration(
            "verzoek",
            """
            {
              "notificatiesApiPluginConfiguration": "${notificatiesApiPluginConfiguration.id.id}",
              "objectManagementId": "${objectManagement.id}",
              "systemProcessDefinitionKey": "verzoek-process",
              "rsin": "${rsin}",
              "verzoekProperties": [{
                "type": "objection",
                "caseDefinitionName": "${documentDefinition.documentDefinition().id().name()}",
                "processDefinitionKey": "objection-process",
                "initiatorRoltypeUrl": "$initiatoRolType",
                "initiatorRolDescription": "Initiator"
              }]
            }
            """.trimIndent()
        )
        //mocks
        val mockObjectenApiPlugin = mock<ObjectenApiPlugin>()
        doCallRealMethod().whenever(pluginServiceSpy).createInstance(any<Class<VerzoekPlugin>>(), any())
        doReturn(mockObjectenApiPlugin).whenever(pluginServiceSpy).createInstance(any<PluginConfigurationId>())
        doReturn(createObjectWrapper(withMetaData = true, "otherType", true)).whenever(mockObjectenApiPlugin)
            .getObject(any())
        //tested method
        val exception = assertThrows<RuntimeException> {
            verzoekPluginEventListener.createZaakFromNotificatie(createEvent())
        }
        //assertions
        assertEquals("Could not find properties of type otherType", exception.message)
    }

    @Test
    fun `should throw exception when object data is not present`() {
        createPluginConfiguration(
            "verzoek",
            """
            {
              "notificatiesApiPluginConfiguration": "${notificatiesApiPluginConfiguration.id.id}",
              "objectManagementId": "${objectManagement.id}",
              "systemProcessDefinitionKey": "verzoek-process",
              "rsin": "${rsin}",
              "verzoekProperties": [{
                "type": "objection",
                "caseDefinitionName": "${documentDefinition.documentDefinition().id().name()}",
                "processDefinitionKey": "objection-process",
                "initiatorRoltypeUrl": "$initiatoRolType",
                "initiatorRolDescription": "Initiator"
              }]
            }
            """.trimIndent()
        )
        //mocks
        val mockObjectenApiPlugin = mock<ObjectenApiPlugin>()
        doCallRealMethod().whenever(pluginServiceSpy).createInstance(any<Class<VerzoekPlugin>>(), any())
        doReturn(mockObjectenApiPlugin).whenever(pluginServiceSpy).createInstance(any<PluginConfigurationId>())
        doReturn(createObjectWrapper(withMetaData = true, verzoekObjectType, false)).whenever(mockObjectenApiPlugin)
            .getObject(any())
        //tested method
        val exception = assertThrows<RuntimeException> {
            verzoekPluginEventListener.createZaakFromNotificatie(createEvent())
        }
        //assertions
        assertEquals("Verzoek Object data was empty!", exception.message)
    }

    private fun createObjectWrapper(withMetaData: Boolean, withType: String, withObjectData: Boolean): ObjectWrapper {
        return ObjectWrapper(
            url = URI.create(""),
            uuid = UUID.randomUUID(),
            type = URI.create(""),
            record = createRecord(withMetaData, withType, withObjectData)
        )
    }

    private fun createRecord(withMetaData: Boolean, withType: String, withObjectData: Boolean): ObjectRecord {
        return ObjectRecord(
            typeVersion = 1,
            data = if (withMetaData) jacksonObjectMapper().readTree(
                """
                {
                    "type": "$withType",
                    ${createObjectData(withObjectData)}
                    "bsn": "$bsn"
                }
            """.trimIndent()
            ) else null,
            startAt = LocalDate.now()
        )
    }

    private fun createObjectData(withObjectData: Boolean): String {
        return if (withObjectData)
            """
                    "data": {
                          "name": "Luis Suarez"
                    },
                """.trimIndent()
        else
            ""
    }


    private fun createPluginConfiguration(pluginDefinitionKey: String, pluginProperties: String): PluginConfiguration {
        return pluginService.createPluginConfiguration(
            "my-configuration-$pluginDefinitionKey-${pluginProperties.hashCode()}",
            jacksonObjectMapper().readTree(pluginProperties).deepCopy(),
            pluginDefinitionKey
        )
    }

    private fun createObjectManagement(): ObjectManagement {
        return ObjectManagement(
            id = UUID.randomUUID(),
            title = "request",
            objecttypenApiPluginConfigurationId = UUID.randomUUID(),
            objecttypeId = objectType,
            objectenApiPluginConfigurationId = UUID.randomUUID(),
            showInDataMenu = false
        )
    }

    private fun createEvent(): NotificatiesApiNotificationReceivedEvent {
        return NotificatiesApiNotificationReceivedEvent(
            kanaal = "objecten",
            actie = "create",
            resourceUrl = "aResource",
            kenmerken = mapOf(Pair("objectType", "something/$objectType"))
        )
    }
}