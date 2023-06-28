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
package com.ritense.form.service

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import com.ritense.document.domain.impl.JsonDocumentContent
import com.ritense.document.domain.impl.JsonSchema
import com.ritense.document.domain.impl.JsonSchemaDocument
import com.ritense.document.domain.impl.JsonSchemaDocumentDefinition
import com.ritense.document.domain.impl.JsonSchemaDocumentDefinitionId
import com.ritense.document.exception.DocumentNotFoundException
import com.ritense.document.service.DocumentSequenceGeneratorService
import com.ritense.document.service.impl.JsonSchemaDocumentService
import com.ritense.form.domain.FormIoFormDefinition
import com.ritense.form.domain.FormProcessLink
import com.ritense.form.service.impl.FormIoFormDefinitionService
import com.ritense.form.web.rest.dto.FormSubmissionResultFailed
import com.ritense.form.web.rest.dto.FormSubmissionResultSucceeded
import com.ritense.processdocument.domain.impl.CamundaProcessDefinitionKey
import com.ritense.processdocument.domain.impl.CamundaProcessJsonSchemaDocumentDefinition
import com.ritense.processdocument.domain.impl.CamundaProcessJsonSchemaDocumentDefinitionId
import com.ritense.processdocument.domain.impl.request.ModifyDocumentAndCompleteTaskRequest
import com.ritense.processdocument.domain.impl.request.ModifyDocumentAndStartProcessRequest
import com.ritense.processdocument.domain.impl.request.NewDocumentAndStartProcessRequest
import com.ritense.processdocument.service.impl.CamundaProcessJsonSchemaDocumentAssociationService
import com.ritense.processdocument.service.impl.CamundaProcessJsonSchemaDocumentService
import com.ritense.processdocument.service.impl.result.ModifyDocumentAndCompleteTaskResultSucceeded
import com.ritense.processlink.domain.ActivityTypeWithEventName
import com.ritense.processlink.domain.ActivityTypeWithEventName.START_EVENT_START
import com.ritense.processlink.domain.ActivityTypeWithEventName.USER_TASK_CREATE
import com.ritense.processlink.service.ProcessLinkService
import com.ritense.valtimo.camunda.domain.CamundaProcessDefinition
import com.ritense.valtimo.contract.event.ExternalDataSubmittedEvent
import com.ritense.valtimo.contract.json.patch.JsonPatchBuilder
import com.ritense.valtimo.service.CamundaProcessService
import com.ritense.valtimo.service.CamundaTaskService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.isA
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.context.ApplicationEventPublisher
import java.net.URI
import java.util.Optional
import java.util.UUID
import java.util.stream.Collectors

class FormSubmissionServiceTest {

    lateinit var formSubmissionService: FormSubmissionService
    lateinit var processLinkService: ProcessLinkService
    lateinit var formDefinitionService: FormIoFormDefinitionService
    lateinit var documentService: JsonSchemaDocumentService
    lateinit var processDocumentAssociationService: CamundaProcessJsonSchemaDocumentAssociationService
    lateinit var processDocumentService: CamundaProcessJsonSchemaDocumentService
    lateinit var camundaTaskService: CamundaTaskService
    lateinit var camundaProcessService: CamundaProcessService
    lateinit var applicationEventPublisher: ApplicationEventPublisher
    lateinit var prefillFormService: PrefillFormService
    lateinit var documentSequenceGeneratorService: DocumentSequenceGeneratorService

    lateinit var formProcessLink: FormProcessLink
    lateinit var processDefinition: CamundaProcessDefinition
    lateinit var formDefinition: FormIoFormDefinition

    @BeforeEach
    fun beforeEach() {
        processLinkService = mock()
        formDefinitionService = mock()
        documentService = mock()
        processDocumentAssociationService = mock()
        processDocumentService = mock()
        camundaTaskService = mock()
        camundaProcessService = mock()
        applicationEventPublisher = mock()
        prefillFormService = mock()
        formSubmissionService = FormSubmissionService(
            processLinkService,
            formDefinitionService,
            documentService,
            processDocumentAssociationService,
            processDocumentService,
            camundaTaskService,
            camundaProcessService,
            applicationEventPublisher,
            prefillFormService,
        )

        documentSequenceGeneratorService = mock()
        whenever(documentSequenceGeneratorService.next(any())).thenReturn(1L)

        formProcessLink = formProcessLink()

        processDefinition = mock<CamundaProcessDefinition>()
        whenever(processDefinition.key).thenReturn("myProcessDefinitionKey")
        whenever(camundaProcessService.getProcessDefinitionById(formProcessLink.processDefinitionId))
            .thenReturn(processDefinition)

        formDefinition = formDefinitionOf("user-task")
        whenever(formDefinitionService.getFormDefinitionById(formProcessLink.formDefinitionId))
            .thenReturn(Optional.of(formDefinition))

        whenever(prefillFormService.preSubmissionTransform(any(),any(),any(),any()))
            .thenReturn(JsonPatchBuilder().build())
    }

    @Test
    fun `should handle submission - new document and start process`() {
        //Given
        val formData = formData()
        whenever(processDocumentAssociationService.getProcessDocumentDefinition(any()))
            .thenReturn(processDocumentDefinition("aName", true))
        val document = createDocument(JsonDocumentContent.build(formData))
        whenever(processDocumentService.dispatch(any()))
            .thenReturn(ModifyDocumentAndCompleteTaskResultSucceeded(document))

        //When
        val formSubmissionResult = formSubmissionService.handleSubmission(
            processLinkId = formProcessLink(START_EVENT_START).id,
            formData = formData,
            documentId = null,
            taskInstanceId = null
        )

        //Then
        assertThat(formSubmissionResult).isInstanceOf(FormSubmissionResultSucceeded::class.java)
        assertThat(formSubmissionResult.errors()).isEmpty()
        verify(applicationEventPublisher, times(0)).publishEvent(isA<ExternalDataSubmittedEvent>())
        verify(processDocumentService, times(1)).dispatch(isA<NewDocumentAndStartProcessRequest>())
    }

    @Test
    fun `should handle submission - modify document and start process`() {
        //Given
        val documentId = UUID.randomUUID().toString()
        val formData = formData()
        whenever(processDocumentAssociationService.getProcessDocumentDefinition(any(), any()))
            .thenReturn(processDocumentDefinition("aName"))
        val document = createDocument(JsonDocumentContent.build(formData))
        whenever(documentService.get(documentId)).thenReturn(document)
        whenever(processDocumentService.dispatch(any()))
            .thenReturn(ModifyDocumentAndCompleteTaskResultSucceeded(document))

        //When
        val formSubmissionResult = formSubmissionService.handleSubmission(
            processLinkId = formProcessLink(START_EVENT_START).id,
            formData = formData,
            documentId = documentId,
            taskInstanceId = null
        )

        //Then
        assertThat(formSubmissionResult).isInstanceOf(FormSubmissionResultSucceeded::class.java)
        assertThat(formSubmissionResult.errors()).isEmpty()
        verify(applicationEventPublisher, times(0)).publishEvent(isA<ExternalDataSubmittedEvent>())
        verify(processDocumentService, times(1)).dispatch(isA<ModifyDocumentAndStartProcessRequest>())
    }

    @Test
    fun `should handle submission - modify document and complete task`() {
        //Given
        val documentId = UUID.randomUUID().toString()
        val formData = formData()
        whenever(processDocumentAssociationService.getProcessDocumentDefinition(any(), any()))
            .thenReturn(processDocumentDefinition("aName"))
        val document = createDocument(JsonDocumentContent.build(formData))
        whenever(documentService.get(documentId)).thenReturn(document)
        whenever(processDocumentService.dispatch(any()))
            .thenReturn(ModifyDocumentAndCompleteTaskResultSucceeded(document))

        //When
        val formSubmissionResult = formSubmissionService.handleSubmission(
            processLinkId = formProcessLink.id,
            formData = formData,
            documentId = documentId,
            taskInstanceId = "myTaskInstanceId"
        )

        //Then
        assertThat(formSubmissionResult).isInstanceOf(FormSubmissionResultSucceeded::class.java)
        assertThat(formSubmissionResult.errors()).isEmpty()
        verify(applicationEventPublisher, times(0)).publishEvent(isA<ExternalDataSubmittedEvent>())
        verify(processDocumentService, times(1)).dispatch(isA<ModifyDocumentAndCompleteTaskRequest>())
    }

    @Test
    fun `should not handle submission`() {
        val formData = JsonNodeFactory.instance.objectNode()
        formData.put("name", "value")

        val formSubmissionResult = formSubmissionService.handleSubmission(
            processLinkId = formProcessLink.id,
            formData = formData,
            documentId = UUID.randomUUID().toString(),
            taskInstanceId = "myTaskInstanceId",
        )

        assertThat(formSubmissionResult).isInstanceOf(FormSubmissionResultFailed::class.java)
        assertThat(formSubmissionResult.errors()).isNotEmpty()
    }

    @Test
    fun `should not find document`() {
        //Given
        val documentId = UUID.randomUUID().toString()
        whenever(documentService.get(documentId))
            .thenThrow(DocumentNotFoundException("Document not found with id: $documentId"))

        //When
        val documentNotFoundException = formSubmissionService.handleSubmission(
            processLinkId = formProcessLink.id,
            formData = formData(),
            documentId = documentId,
            taskInstanceId = "myTaskInstanceId"
        )

        //Then
        assertThat(documentNotFoundException).isNotNull()
        assertThat(documentNotFoundException.errors()).isNotEmpty()
        assertThat(documentNotFoundException.errors().stream().map { obj -> obj.asString() }
            .collect(Collectors.joining())).contains(documentId)
    }

    @Test
    fun `should not find process document definition by prodDefId`() {
        //Given
        val documentId: String? = null
        val formData = formData()
        val document = createDocument(JsonDocumentContent.build(formData))
        whenever(documentService.get(documentId)).thenReturn(document)

        //When
        val documentNotFoundException = formSubmissionService.handleSubmission(
            processLinkId = formProcessLink.id,
            formData = formData,
            documentId = documentId,
            taskInstanceId = "myTaskInstanceId"
        )

        //Then
        assertThat(documentNotFoundException).isNotNull()
        assertThat(documentNotFoundException.errors()).isNotEmpty()
    }

    @Test
    fun `should not find process document definition by prodDefId and version`() {
        //Given
        val documentId = UUID.randomUUID().toString()
        val formData = formData()
        val document = createDocument(JsonDocumentContent.build(formData))
        whenever(documentService.get(documentId)).thenReturn(document)

        //When
        val documentNotFoundException = formSubmissionService.handleSubmission(
            processLinkId = formProcessLink.id,
            formData = formData,
            documentId = documentId,
            taskInstanceId = "myTaskInstanceId"
        )

        //Then
        assertThat(documentNotFoundException).isNotNull()
        assertThat(documentNotFoundException.errors()).isNotEmpty()
    }

    private fun formProcessLink(activityType: ActivityTypeWithEventName = USER_TASK_CREATE): FormProcessLink {
        val formProcessLink = FormProcessLink(
            id = UUID.randomUUID(),
            processDefinitionId = "11111111-1111-1111-1111-111111111111",
            activityId = "myActivityId",
            activityType = activityType,
            formDefinitionId = UUID.fromString("22222222-2222-2222-2222-222222222222")
        )
        whenever(processLinkService.getProcessLink(formProcessLink.id, FormProcessLink::class.java))
            .thenReturn(formProcessLink)
        return formProcessLink
    }

    private fun formDefinitionOf(formDefinitionId: String): FormIoFormDefinition {
        val formDefinition = rawFormDefinition(formDefinitionId)
        return FormIoFormDefinition(UUID.randomUUID(), "form-example", formDefinition, false)
    }

    private fun rawFormDefinition(formDefinitionId: String): String {
        return requireNotNull(Thread.currentThread().contextClassLoader.getResourceAsStream("config/form/$formDefinitionId.json"))
            .bufferedReader().use { it.readText() }
    }

    private fun processDocumentDefinition(documentDefinitionName: String, canInitializeDocument: Boolean = false): CamundaProcessJsonSchemaDocumentDefinition {
        return CamundaProcessJsonSchemaDocumentDefinition(
            CamundaProcessJsonSchemaDocumentDefinitionId.newId(
                CamundaProcessDefinitionKey(PROCESS_DEFINITION_KEY),
                JsonSchemaDocumentDefinitionId.existingId(documentDefinitionName, 1)
            ),
            canInitializeDocument,
            false
        )
    }

    private fun definition(): JsonSchemaDocumentDefinition {
        val jsonSchemaDocumentDefinitionId = JsonSchemaDocumentDefinitionId.newId("person")
        val jsonSchema = JsonSchema.fromResourceUri(path(jsonSchemaDocumentDefinitionId.name()))
        return JsonSchemaDocumentDefinition(jsonSchemaDocumentDefinitionId, jsonSchema)
    }

    private fun createDocument(content: JsonDocumentContent): JsonSchemaDocument {
        return JsonSchemaDocument.create(
            definition(),
            content,
            USERNAME,
            documentSequenceGeneratorService,
            null
        )
            .resultingDocument()
            .orElseThrow()
    }

    private fun path(name: String): URI? {
        return URI.create(String.format("config/document/definition/%s.json", "$name.schema"))
    }

    private fun formData(): ObjectNode {
        val formData = JsonNodeFactory.instance.objectNode()
        formData.put("voornaam", "jan")

        //Number
        formData.put("number", 123)

        //Checkbox
        formData.put("checkbox", true)

        //Select Boxes
        val dataSelectBoxes = JsonNodeFactory.instance.objectNode()
        dataSelectBoxes.put("option1", "true")
        formData.set<JsonNode>("selectBoxes", dataSelectBoxes)

        //Date / Time
        formData.put("dateTime", "2020-09-24T12:00:00+02:00")

        //Tags
        formData.put("tags", "tag1,tag2,tag3")

        //Phone Number
        formData.put("phoneNumber", "(020) 697-8255")

        //Url
        formData.put("url", "http://www.nu.nl")

        //Email
        formData.put("email", "a@a.com")

        //Radio
        formData.put("radio", "radio1")

        //Password
        formData.put("password", "password")

        //Upload
        val bijlageArray = JsonNodeFactory.instance.arrayNode()
        val file = JsonNodeFactory.instance.objectNode()
        file.put("storage", "url")
        file.put("name", "test-736b4bfc-5ed0-4fac-a9c2-89629ccbe451.rtf")
        file.put(
            "url",
            "https://console.test.valtimo.nl/api/v1/form-file?baseUrl=http%3A%2F%2Flocalhost%3A4200&project=&form=/test-736b4bfc-5ed0-4fac-a9c2-89629ccbe451.rtf"
        )
        file.put("size", 391)
        file.put("type", "text/rtf")
        val data = JsonNodeFactory.instance.objectNode()
        data.put("key", "test-736b4bfc-5ed0-4fac-a9c2-89629ccbe451.rtf")
        data.put("resourceId", "736b4bfc-5ed0-4fac-a9c2-89629ccbe451")
        data.put("baseUrl", "http://localhost:4200")
        data.put("project", "")
        data.put("form", "")
        file.set<JsonNode>("data", data)
        file.put("originalName", "test.rtf")
        bijlageArray.add(file)
        formData.set<JsonNode>("bijlagen", bijlageArray)
        return formData
    }

    companion object {
        private const val USERNAME = "test@test.com"
        private const val PROCESS_DEFINITION_KEY = "formlink-one-task-process"
    }
}
