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

package com.ritense.formlink.domain.impl.submission

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import com.ritense.document.domain.impl.JsonDocumentContent
import com.ritense.document.domain.impl.JsonSchema
import com.ritense.document.domain.impl.JsonSchemaDocument
import com.ritense.document.domain.impl.JsonSchemaDocumentDefinition
import com.ritense.document.domain.impl.JsonSchemaDocumentDefinitionId
import com.ritense.document.service.DocumentSequenceGeneratorService
import com.ritense.form.domain.FormIoFormDefinition
import com.ritense.formlink.domain.impl.formassociation.StartEventFormAssociation
import com.ritense.formlink.service.SubmissionTransformerService
import com.ritense.formlink.service.impl.result.FormSubmissionResultSucceeded
import com.ritense.processdocument.domain.ProcessDocumentDefinition
import com.ritense.processdocument.domain.impl.CamundaProcessDefinitionKey
import com.ritense.processdocument.domain.impl.CamundaProcessJsonSchemaDocumentDefinitionId
import com.ritense.processdocument.service.ProcessDocumentService
import com.ritense.processdocument.service.result.DocumentFunctionResult
import com.ritense.valtimo.service.CamundaTaskService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.context.ApplicationEventPublisher
import java.net.URI
import java.util.Optional

class FormIoSubmissionTest : BaseTest() {

    lateinit var formIoSubmission: FormIoSubmission

    @Mock
    lateinit var formAssociation: StartEventFormAssociation

    @Mock
    lateinit var formDefinition: FormIoFormDefinition

    @Mock
    lateinit var processDocumentDefinition: ProcessDocumentDefinition

    @Mock
    lateinit var formData: JsonNode

    lateinit var document: JsonSchemaDocument

    @Mock
    lateinit var processDocumentService: ProcessDocumentService

    @Mock
    lateinit var taskService: CamundaTaskService

    @Mock
    lateinit var submissionTransformerService: SubmissionTransformerService<FormIoFormDefinition>

    @Mock
    lateinit var applicationEventPublisher: ApplicationEventPublisher

    @Mock
    lateinit var documentFunctionResult: DocumentFunctionResult<JsonSchemaDocument>

    @Mock
    lateinit var documentSequenceGeneratorService: DocumentSequenceGeneratorService

    @BeforeEach
    fun init() {
        MockitoAnnotations.openMocks(this)

        whenever(documentSequenceGeneratorService.next(any())).thenReturn(1)
        val documentOptional = documentOptional()
        document = documentOptional.orElseThrow()

        whenever(processDocumentDefinition.processDocumentDefinitionId()).thenReturn(
            CamundaProcessJsonSchemaDocumentDefinitionId.existingId(
                CamundaProcessDefinitionKey("processDefintionKey"),
                JsonSchemaDocumentDefinitionId.existingId("documentId", 1)
            )
        )
        whenever(processDocumentDefinition.canInitializeDocument()).thenReturn(true)
        whenever(processDocumentService.dispatch(any())).thenReturn(documentFunctionResult)
        whenever(documentFunctionResult.resultingDocument()).thenReturn(
            documentOptional
        )
    }

    @Test
    fun `apply - should apply submission without external form data`() {
        formIoSubmission = FormIoSubmission(
            formAssociation,
            formDefinition,
            processDocumentDefinition.processDocumentDefinitionId().documentDefinitionId().name(),
            formData,
            document,
            "taskInstanceId",
            processDocumentDefinition.processDocumentDefinitionId().processDefinitionKey().toString(),
            processDocumentService,
            taskService,
            submissionTransformerService,
            applicationEventPublisher
        )

        val result = formIoSubmission.apply()

        verifyNoInteractions(applicationEventPublisher)

        assertThat(result is FormSubmissionResultSucceeded)
    }

    @Test
    fun `apply - should apply start form no document available yet`() {
        formIoSubmission = FormIoSubmission(
            formAssociation,
            formDefinition,
            null,
            formData,
            null,
            null,
            "processDefinitionKey",
            processDocumentService,
            taskService,
            submissionTransformerService,
            applicationEventPublisher
        )

        val result = formIoSubmission.apply()

        verifyNoInteractions(applicationEventPublisher)

        assertThat(result is FormSubmissionResultSucceeded)
    }

    @Test
    fun `apply - should update fields with value only`() {
        val formDefinition: FormIoFormDefinition = formDefinitionOf("form-street-only-example")

        formIoSubmission = FormIoSubmission(
            formAssociation,
            formDefinition,
            null,
            formData(),
            null,
            null,
            "processDefinitionKey",
            processDocumentService,
            taskService,
            submissionTransformerService,
            applicationEventPublisher
        )

        val result = formIoSubmission.apply()

        //assert content only contains form field, not the whole document with null values
        assertThat(formIoSubmission.documentContent().size()).isEqualTo(1)
        assertThat(formIoSubmission.documentContent().get("street").equals("Funenpark"))
        assertThat(result.errors()).isEmpty()
    }

    @Test
    fun `apply - should update fields with empty value`() {
        val formDefinition: FormIoFormDefinition = formDefinitionOf("form-street-housenumber-example")

        formIoSubmission = FormIoSubmission(
            formAssociation,
            formDefinition,
            null,
            formDataWithEmptyStringValue(),
            null,
            null,
            "processDefinitionKey",
            processDocumentService,
            taskService,
            submissionTransformerService,
            applicationEventPublisher
        )

        val result = formIoSubmission.apply()

        //assert content only contains form field, not the whole document with null values
        assertThat(formIoSubmission.documentContent().size()).isEqualTo(2)
        assertThat(formIoSubmission.documentContent().get("street").equals(""))
        assertThat(result.errors()).isEmpty()
    }

    @Test
    fun `apply - should not update field with null node`() {
        val formDefinition: FormIoFormDefinition = formDefinitionOf("form-street-only-example")

        formIoSubmission = FormIoSubmission(
            formAssociation,
            formDefinition,
            null,
            formDataWithNullNode(),
            null,
            null,
            "processDefinitionKey",
            processDocumentService,
            taskService,
            submissionTransformerService,
            applicationEventPublisher
        )

        val result = formIoSubmission.apply()

        //assert content only contains form field, not the whole document with null values
        assertThat(formIoSubmission.documentContent().size()).isEqualTo(1)
        assertThat(formIoSubmission.documentContent().get("street").equals("Funenpark"))
        assertThat(result.errors()).isEmpty()
    }

    private fun formData(): ObjectNode {
        val jsonNode = JsonNodeFactory.instance.objectNode()
        jsonNode.put("street", "Funenpark")
        return jsonNode
    }

    private fun formDataWithEmptyStringValue(): ObjectNode {
        val jsonNode = JsonNodeFactory.instance.objectNode()
        jsonNode.put("street", "")
        jsonNode.put("housenumber", 1)
        return jsonNode
    }

    private fun formDataWithNullNode(): ObjectNode {
        val jsonNode = JsonNodeFactory.instance.objectNode()
        val nullNode = JsonNodeFactory.instance.nullNode()

        jsonNode.put("street", "Funenpark")
        jsonNode.put("housenumber", nullNode)
        return jsonNode
    }

    private fun documentOptional(): Optional<JsonSchemaDocument> {
        return JsonSchemaDocument.create(
            definition(),
            JsonDocumentContent("{\"name\": \"whatever\" }"),
            "USERNAME",
            documentSequenceGeneratorService,
            null
        ).resultingDocument()
    }

    private fun definition(): JsonSchemaDocumentDefinition {
        val jsonSchemaDocumentDefinitionId = JsonSchemaDocumentDefinitionId.newId("house")
        val jsonSchema = JsonSchema.fromResourceUri(path(jsonSchemaDocumentDefinitionId.name()))
        return JsonSchemaDocumentDefinition(jsonSchemaDocumentDefinitionId, jsonSchema)
    }

    fun path(name: String): URI {
        return URI.create(String.format("config/document/definition/%s.json", "$name.schema"))
    }

}
