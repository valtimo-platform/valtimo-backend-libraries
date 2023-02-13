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

import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.ritense.BaseIntegrationTest
import com.ritense.document.domain.impl.request.NewDocumentRequest
import com.ritense.notificatiesapi.NotificatiesApiAuthentication
import com.ritense.notificatiesapi.event.NotificatiesApiNotificationReceivedEvent
import com.ritense.objectenapi.ObjectenApiAuthentication
import com.ritense.objectmanagement.domain.ObjectManagement
import com.ritense.objectmanagement.service.ObjectManagementService
import com.ritense.objecttypenapi.ObjecttypenApiAuthentication
import com.ritense.plugin.domain.ActivityType
import com.ritense.plugin.domain.PluginConfiguration
import com.ritense.plugin.domain.PluginConfigurationId
import com.ritense.plugin.domain.PluginProcessLink
import com.ritense.plugin.domain.PluginProcessLinkId
import com.ritense.plugin.repository.PluginProcessLinkRepository
import com.ritense.processdocument.domain.impl.request.NewDocumentAndStartProcessRequest
import com.ritense.processdocument.service.ProcessDocumentService
import com.ritense.valtimo.contract.json.Mapper
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.TaskService
import org.camunda.bpm.engine.delegate.VariableScope
import org.camunda.bpm.engine.task.Task
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsInAnyOrder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doCallRealMethod
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.ExchangeFunction
import reactor.core.publisher.Mono
import java.net.URI
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@Transactional
internal class PortaalTaakEventListenerIntTest : BaseIntegrationTest() {

    @Autowired
    lateinit var repositoryService: RepositoryService

    @Autowired
    lateinit var runtimeService: RuntimeService

    @Autowired
    lateinit var pluginProcessLinkRepository: PluginProcessLinkRepository

    @Autowired
    lateinit var objectManagementService: ObjectManagementService

    @Autowired
    lateinit var portaalTaakEventListener: PortaalTaakEventListener

    @Autowired
    lateinit var processDocumentService: ProcessDocumentService

    @Autowired
    lateinit var taskService: TaskService

    lateinit var processDefinitionId: String
    lateinit var objectManagement: ObjectManagement
    lateinit var portaalTaakPluginConfiguration: PluginConfiguration
    lateinit var objecttypenPluginConfiguration: PluginConfiguration
    protected var executedRequests: MutableList<RecordedRequest> = mutableListOf()
    lateinit var server: MockWebServer
    lateinit var notificatiesApiPluginConfiguration: PluginConfiguration
    lateinit var objectenApiPluginConfiguration: PluginConfiguration
    var task: Task? = null
    var documentId: UUID? = null


    @BeforeEach
    fun init() {
        server = MockWebServer()
        setupMockObjectenApiServer()
        server.start()
        val mockedId = PluginConfigurationId.existingId(UUID.fromString("27a399c7-9d70-4833-a651-57664e2e9e09"))
        doReturn(Optional.of(mock<PluginConfiguration>())).whenever(pluginConfigurationRepository).findById(mockedId)
        doReturn(TestAuthentication()).whenever(pluginService).createInstance(mockedId)
        doCallRealMethod().whenever(pluginService).createPluginConfiguration(any(), any(), any())

        notificatiesApiPluginConfiguration = createNotificatiesApiPlugin()
        objecttypenPluginConfiguration = createObjectTypenApiPlugin()
        objectenApiPluginConfiguration = createObjectenApiPlugin()
        objectManagement =
            createObjectManagement(objectenApiPluginConfiguration.id.id, objecttypenPluginConfiguration.id.id)
        portaalTaakPluginConfiguration = createPortaalTaakPlugin(notificatiesApiPluginConfiguration, objectManagement)
    }

    @Test
    fun `should complete task with data on event`() {
        val actionPropertiesJson = """
            {
                "formType" : "${TaakFormType.ID.key}",
                "formTypeId": "some-form",
                "sendData": [
                    {
                        "key": "/lastname",
                        "value": "test"
                    }
                ],
                "receiveData": [
                    {
                        "key": "doc:/name",
                        "value": "/name"
                    }
                ],
                "receiver": "${TaakReceiver.OTHER.key}",
                "otherReceiver": "${OtherTaakReceiver.KVK.key}",
                "kvk": "569312863"
            }
        """.trimIndent()
        createProcessLink(actionPropertiesJson)

        val documentContent = """
            {
                "lastname": "test"
            }
        """.trimIndent()


        task = startPortaalTaakProcess(documentContent)

        doCallRealMethod().whenever(pluginService).createInstance(any<Class<PortaaltaakPlugin>>(), any())

        val processInstanceIdCaptor = argumentCaptor<String>()
        val variableScopeCaptor = argumentCaptor<VariableScope>()
        val mapCaptor = argumentCaptor<Map<String, Any>>()
        doReturn(null).whenever(camundaProcessService).startProcess(any(), any(), any())

        val event = getEvent()
        portaalTaakEventListener.processCompletePortaalTaakEvent(event)

        verify(valueResolverService).handleValues(
            processInstanceIdCaptor.capture(),
            variableScopeCaptor.capture(),
            mapCaptor.capture()
        )

        val processDefinitionKeyCaptor = argumentCaptor<String>()
        val businessKeyCaptor = argumentCaptor<String>()
        val processVariableCaptor = argumentCaptor<Map<String, Any>>()

        verify(camundaProcessService, times(2)).startProcess(
            processDefinitionKeyCaptor.capture(),
            businessKeyCaptor.capture(),
            processVariableCaptor.capture()
        )

        // assert call to valueResolverService where data is saved
        assertEquals(task!!.processInstanceId, processInstanceIdCaptor.firstValue)
        assertNotNull(variableScopeCaptor.firstValue)
        val mapOfValuesToUpdate = mapCaptor.firstValue
        assertEquals(1, mapOfValuesToUpdate.size)
        assertEquals("Luis", mapOfValuesToUpdate["doc:/name"])

        // assert second call to camundaProcessService.startProcess() where handling process is started
        assertEquals("process-portaaltaak-uploaded-documents-mock", processDefinitionKeyCaptor.secondValue)
        assertEquals(documentId!!.toString(), businessKeyCaptor.secondValue)
        val processVariables = processVariableCaptor.secondValue
        assertEquals(event.resourceUrl, processVariables["portaalTaakObjectUrl"])
        assertEquals(objectenApiPluginConfiguration.id.id.toString(), processVariables["objectenApiPluginConfigurationId"])
        assertEquals(task!!.id, processVariables["verwerkerTaakId"])
        assertThat(processVariables["documentUrls"] as List<*>, containsInAnyOrder(
            "http://documenten-api.com/api/v1/documenten/393ba68f-0bd6-43d7-9c1c-cb33d4d2aa6e",
            "http://documenten-api.com/api/v1/documenten/205107b1-261f-4042-925a-e300cdc6d2ab",
            "http://documenten-api.com/api/v1/documenten/8c9dc2e4-db3b-4314-8e2e-76f38943d8fc"
        ))
    }

    private fun getEvent(): NotificatiesApiNotificationReceivedEvent {
        return NotificatiesApiNotificationReceivedEvent(
            kanaal = "objecten",
            actie = "update",
            resourceUrl = "${server.url("/")}objects",
            kenmerken = mapOf(Pair("objectType", objectManagement.objecttypeId))
        )
    }

    private fun createNotificatiesApiPlugin(): PluginConfiguration {
        val pluginPropertiesJson = """
            {
              "url": "${server.url("/")}",
              "callbackUrl": "http://localhost",
              "authenticationPluginConfiguration": "27a399c7-9d70-4833-a651-57664e2e9e09"
            }
        """.trimIndent()

        val configuration = pluginService.createPluginConfiguration(
            "Notificaties API plugin configuration",
            Mapper.INSTANCE.get().readTree(
                pluginPropertiesJson
            ) as ObjectNode,
            "notificatiesapi"
        )
        return configuration
    }

    private fun createObjectenApiPlugin(): PluginConfiguration {
        val pluginPropertiesJson = """
            {
              "url": "${server.url("/")}",
              "authenticationPluginConfiguration": "27a399c7-9d70-4833-a651-57664e2e9e09"
            }
        """.trimIndent()

        val configuration = pluginService.createPluginConfiguration(
            "Objecttype plugin configuration",
            Mapper.INSTANCE.get().readTree(
                pluginPropertiesJson
            ) as ObjectNode,
            "objectenapi"
        )
        return configuration
    }

    private fun createObjectManagement(
        objectenApiPluginConfigurationId: UUID,
        objecttypenApiPluginConfigurationId: UUID
    ): ObjectManagement {
        val objectManagement = ObjectManagement(
            title = "something",
            objectenApiPluginConfigurationId = objectenApiPluginConfigurationId,
            objecttypenApiPluginConfigurationId = objecttypenApiPluginConfigurationId,
            objecttypeId = "objecten"
        )
        return objectManagementService.create(objectManagement)
    }

    private fun createPortaalTaakPlugin(
        notificatiesApiPlugin: PluginConfiguration,
        objectManagement: ObjectManagement
    ): PluginConfiguration {
        val pluginPropertiesJson = """
            {
              "notificatiesApiPluginConfiguration": "${notificatiesApiPlugin.id.id}",
              "objectManagementConfigurationId": "${objectManagement.id}",
              "uploadedDocumentsHandlerProcess": "process-portaaltaak-uploaded-documents-mock"
            }
        """.trimIndent()

        val configuration = pluginService.createPluginConfiguration(
            "Portaaltaak plugin configuration",
            Mapper.INSTANCE.get().readTree(
                pluginPropertiesJson
            ) as ObjectNode,
            "portaaltaak"
        )
        return configuration
    }

    private fun createObjectTypenApiPlugin(): PluginConfiguration {
        val pluginPropertiesJson = """
            {
              "url": "${server.url("/")}",
              "authenticationPluginConfiguration": "27a399c7-9d70-4833-a651-57664e2e9e09"
            }
        """.trimIndent()

        val configuration = pluginService.createPluginConfiguration(
            "Objecten plugin configuration",
            Mapper.INSTANCE.get().readTree(
                pluginPropertiesJson
            ) as ObjectNode,
            "objecttypenapi"
        )
        return configuration
    }

    private fun setupMockObjectenApiServer() {
        val dispatcher: Dispatcher = object : Dispatcher() {
            @Throws(InterruptedException::class)
            override fun dispatch(request: RecordedRequest): MockResponse {
                executedRequests.add(request)
                val path = request.path?.substringBefore('?')
                val response = when (path) {
                    "/kanaal" -> getKanaalResponse()
                    "/abonnement" -> createAbonnementResponse()
                    "/objects" -> createObjectResponse()
                    else -> MockResponse().setResponseCode(404)
                }
                return response
            }
        }

        server.dispatcher = dispatcher
    }

    private fun getKanaalResponse(): MockResponse {
        val body = """
            [
                {
                  "naam": "objecten"
                }
            ]
        """.trimIndent()
        return mockJsonResponse(body)
    }

    private fun createAbonnementResponse(): MockResponse {
        val body = """
            {
              "url": "http://localhost",
              "auth": "test123",
              "callbackUrl": "http://localhost"
            }
        """.trimIndent()
        return mockJsonResponse(body)
    }

    private fun createObjectResponse(): MockResponse {
        val body = """
            {
              "url": "http://example.com",
              "uuid": "095be615-a8ad-4c33-8e9c-c7612fbf6c9f",
              "type": "http://example.com",
              "record": {
                "index": 0,
                "typeVersion": 32767,
                "data": ${jacksonObjectMapper().writeValueAsString(getTaakObject())},
                "geometry": {
                  "type": "string",
                  "coordinates": [
                    0,
                    0
                  ]
                },
                "startAt": "2019-08-24",
                "endAt": "2019-08-24",
                "registrationAt": "2019-08-24",
                "correctionFor": "string",
                "correctedBy": "string"
              }
            }
        """.trimIndent()
        return mockJsonResponse(body)
    }

    private fun getTaakObject(): TaakObject {
        return TaakObject(
            identificatie = TaakIdentificatie("aType", "aValue"),
            data = emptyMap(),
            title = "aTitle",
            status = TaakStatus.INGEDIEND,
            formulier = TaakForm(TaakFormType.ID, "anId"),
            verwerkerTaakId = getTaskId(),
            verzondenData = mapOf(
                "documenten" to listOf(URI.create("/some-document"), URI.create("/some-document-array")),
                "name" to "Luis",
                "phone" to "999999999",
                "some-document" to "http://documenten-api.com/api/v1/documenten/393ba68f-0bd6-43d7-9c1c-cb33d4d2aa6e",
                "some-document-array" to arrayOf(
                    "http://documenten-api.com/api/v1/documenten/205107b1-261f-4042-925a-e300cdc6d2ab",
                    "http://documenten-api.com/api/v1/documenten/8c9dc2e4-db3b-4314-8e2e-76f38943d8fc"
                )
            )
        )
    }

    private fun startPortaalTaakProcess(content: String): Task {
        val newDocumentRequest = NewDocumentRequest(DOCUMENT_DEFINITION_KEY, Mapper.INSTANCE.get().readTree(content))
        val request = NewDocumentAndStartProcessRequest(PROCESS_DEFINITION_KEY, newDocumentRequest)
        val processResult = processDocumentService.newDocumentAndStartProcess(request)
        documentId = processResult.resultingDocument().get().id().id
        return taskService
            .createTaskQuery()
            .active()
            .processInstanceId(processResult.resultingProcessInstanceId().get().toString())
            .singleResult()
    }

    private fun createProcessLink(propertiesConfig: String) {
        processDefinitionId = repositoryService.createProcessDefinitionQuery()
            .processDefinitionKey(PROCESS_DEFINITION_KEY)
            .latestVersion()
            .singleResult()
            .id

        pluginProcessLinkRepository.save(
            PluginProcessLink(
                PluginProcessLinkId(UUID.randomUUID()),
                processDefinitionId,
                "user_task",
                Mapper.INSTANCE.get().readTree(propertiesConfig) as ObjectNode,
                portaalTaakPluginConfiguration.id,
                "create-portaaltaak",
                activityType = ActivityType.USER_TASK_CREATE
            )
        )
    }

    private fun getTaskId(): String {
        return task?.id ?: ""
    }

    private fun mockJsonResponse(body: String): MockResponse {
        return MockResponse()
            .addHeader("Content-Type", "application/json")
            .setBody(body)
    }

    class TestAuthentication : ObjectenApiAuthentication, ObjecttypenApiAuthentication, NotificatiesApiAuthentication {
        override fun filter(request: ClientRequest, next: ExchangeFunction): Mono<ClientResponse> {
            return next.exchange(request)
        }
    }

    companion object {
        private const val PROCESS_DEFINITION_KEY = "portaaltaak-process"
        private const val DOCUMENT_DEFINITION_KEY = "profile"
    }

}