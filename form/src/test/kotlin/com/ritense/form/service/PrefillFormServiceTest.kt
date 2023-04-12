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
import com.ritense.document.domain.patch.JsonPatchService
import com.ritense.document.service.impl.JsonSchemaDocumentService
import com.ritense.form.BaseTest
import com.ritense.form.service.impl.FormIoFormDefinitionService
import com.ritense.processdocument.service.ProcessDocumentAssociationService
import com.ritense.valtimo.contract.form.FormFieldDataResolver
import com.ritense.valtimo.contract.json.patch.operation.AddOperation
import com.ritense.valtimo.contract.json.patch.operation.Operation
import com.ritense.valtimo.contract.json.patch.operation.ReplaceOperation
import com.ritense.valtimo.service.CamundaProcessService
import org.assertj.core.api.Assertions.assertThat
import org.camunda.bpm.engine.TaskService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock

class PrefillFormServiceTest : BaseTest() {

    lateinit var prefillFormService: PrefillFormService
    lateinit var documentService: JsonSchemaDocumentService
    lateinit var formDefinitionService: FormIoFormDefinitionService
    lateinit var camundaProcessService: CamundaProcessService
    lateinit var taskService: TaskService
    lateinit var formFieldDataResolver: FormFieldDataResolver
    lateinit var processDocumentAssociationService: ProcessDocumentAssociationService

    @BeforeEach
    fun setUp() {
        documentService = mock()
        formDefinitionService = mock()
        camundaProcessService = mock()
        taskService = mock()
        formFieldDataResolver = mock()
        processDocumentAssociationService = mock()
        prefillFormService = PrefillFormService(
            documentService,
            formDefinitionService,
            camundaProcessService,
            taskService,
            listOf(formFieldDataResolver),
            processDocumentAssociationService
        )
    }

    @Test
    fun prePreFillTransform() {
        val formDefinition = formDefinitionOf("existing-item-array-form-example")
        val placeholders = placeholders()
        val source = source()
        prefillFormService.prePreFillTransform(
            formDefinition,
            placeholders,
            source
        )
        assertThat(formDefinition.asJson().at("/components/0/defaultValue").textValue())
            .isEqualTo("Pita bread")
    }

    @Test
    fun preProcessExistingArrayItem() {
        val formDefinition = formDefinitionOf("existing-item-array-form-example")
        val submission = submission()
        val placeholders = placeholders()
        val source = source()
        val jsonPatch = prefillFormService.preSubmissionTransform(
            formDefinition,
            submission,
            placeholders,
            source
        )!!

        //Assert initial submission is cleaned up
        assertThat(submission["name"]).isNullOrEmpty()
        assertThat(jsonPatch.patches().size).isEqualTo(1)

        //Patch for source is created
        val jsonPatchOperation = jsonPatch.patches().iterator().next() as ReplaceOperation
        assertThat(jsonPatchOperation.operation).isEqualTo(Operation.REPLACE.toString())
        assertThat(jsonPatchOperation.path).isEqualTo("/favorites/1/name")
        assertThat(jsonPatchOperation.value.textValue()).isEqualTo("Focaccia")
        JsonPatchService.apply(jsonPatch, source)
        assertThat(source.at("/favorites/1/name").textValue()).isEqualTo("Focaccia")
    }

    @Test
    fun preProcessNewArrayItem() {
        val formDefinition = formDefinitionOf("new-item-array-form-example")
        val submission = submission()
        val placeholders = placeholders()
        val source = source()
        val jsonPatch = prefillFormService.preSubmissionTransform(
            formDefinition,
            submission,
            placeholders,
            source
        )!!

        //Assert initial submission is cleaned up
        assertThat(submission["name"]).isNullOrEmpty()
        assertThat(jsonPatch.patches().size).isEqualTo(2)

        //Patch for source is created
        val newArrayOperation = jsonPatch.patches().stream().skip((jsonPatch.patches().size - 2).toLong()).findFirst()
            .orElseThrow() as AddOperation
        assertThat(newArrayOperation.operation).isEqualTo(Operation.ADD.toString())
        assertThat(newArrayOperation.path).isEqualTo("/favorites/2")
        assertThat(newArrayOperation.value.isObject).isTrue
        val jsonPatchOperation = jsonPatch.patches().stream().skip((jsonPatch.patches().size - 1).toLong()).findFirst()
            .orElseThrow() as AddOperation
        assertThat(jsonPatchOperation.operation).isEqualTo(Operation.ADD.toString())
        assertThat(jsonPatchOperation.path).isEqualTo("/favorites/2/name")
        assertThat(jsonPatchOperation.value.textValue()).isEqualTo("Focaccia")
        JsonPatchService.apply(jsonPatch, source)
        assertThat(source.at("/favorites/2/name").textValue()).isEqualTo("Focaccia")
    }

    @Test
    fun shouldAddNewArrayNodeWhenMissing() {
        val formDefinition = formDefinitionOf("new-item-array-form-example")
        val submission = submission()
        val placeholders = placeholders()
        val source = JsonNodeFactory.instance.objectNode()
        val jsonPatch = prefillFormService.preSubmissionTransform(
            formDefinition,
            submission,
            placeholders,
            source
        )!!

        //Assert initial submission is cleaned up
        assertThat(submission["name"]).isNullOrEmpty()
        assertThat(jsonPatch.patches().size).isEqualTo(3)

        //Patch for source is created
        val newArrayOperation = jsonPatch.patches().stream().skip((jsonPatch.patches().size - 2).toLong()).findFirst()
            .orElseThrow() as AddOperation
        assertThat(newArrayOperation.operation).isEqualTo(Operation.ADD.toString())
        assertThat(newArrayOperation.path).isEqualTo("/favorites/0")
        assertThat(newArrayOperation.value.isObject).isTrue
        val jsonPatchOperation = jsonPatch.patches().stream().skip((jsonPatch.patches().size - 1).toLong()).findFirst()
            .orElseThrow() as AddOperation
        assertThat(jsonPatchOperation.operation).isEqualTo(Operation.ADD.toString())
        assertThat(jsonPatchOperation.path).isEqualTo("/favorites/0/name")
        assertThat(jsonPatchOperation.value.textValue()).isEqualTo("Focaccia")
        JsonPatchService.apply(jsonPatch, source)
        assertThat(source.at("/favorites/0/name").textValue()).isEqualTo("Focaccia")
    }

    @Test
    fun preProcessNewArrayItemMissingKeysInSubmission() {
        val formDefinition = formDefinitionOf("new-item-array-form-example")
        val submission = emptySubmission()
        val placeholders = placeholders()
        val source = source()
        val jsonPatch = prefillFormService.preSubmissionTransform(
            formDefinition,
            submission,
            placeholders,
            source
        )!!

        //Assert initial submission is cleaned up
        assertThat(submission["name"]).isNullOrEmpty()
        assertThat(jsonPatch.patches().size).isEqualTo(0)
        JsonPatchService.apply(jsonPatch, source)
        assertThat(source.at("/favorites").size()).isEqualTo(2)
        assertThat(source.at("/favorites/2/name").isMissingNode).isEqualTo(true)
    }

    @Test
    fun preProcessNewArrayItemCombinedPatch() {
        val formDefinition = formDefinitionOf("new-item-combined-array-form-example")
        val submission = submissionNewArray()
        val placeholders = placeholders()
        val source = source()
        val jsonPatch = prefillFormService.preSubmissionTransform(
            formDefinition,
            submission,
            placeholders,
            source
        )!!

        //Assert initial submission is cleaned up
        assertThat(submission["name"]).isNullOrEmpty()
        assertThat(jsonPatch.patches().size).isEqualTo(3)

        //Patch for source is created
        val newArrayOperation = jsonPatch.patches().stream().skip((jsonPatch.patches().size - 3).toLong()).findFirst()
            .orElseThrow() as AddOperation
        assertThat(newArrayOperation.operation).isEqualTo(Operation.ADD.toString())
        assertThat(newArrayOperation.path).isEqualTo("/favorites/2")
        assertThat(newArrayOperation.value.isObject).isTrue
        val nameOperation = jsonPatch.patches().stream().skip((jsonPatch.patches().size - 2).toLong()).findFirst()
            .orElseThrow() as AddOperation
        assertThat(nameOperation.operation).isEqualTo(Operation.ADD.toString())
        assertThat(nameOperation.path).isEqualTo("/favorites/2/name")
        assertThat(nameOperation.value.textValue()).isEqualTo("Focaccia")
        val sizeOperation = jsonPatch.patches().stream().skip((jsonPatch.patches().size - 1).toLong()).findFirst()
            .orElseThrow() as AddOperation
        assertThat(sizeOperation.operation).isEqualTo(Operation.ADD.toString())
        assertThat(sizeOperation.path).isEqualTo("/favorites/2/size")
        assertThat(sizeOperation.value.textValue()).isEqualTo("big")
        JsonPatchService.apply(jsonPatch, source)
        assertThat(source.at("/favorites/2/name").textValue()).isEqualTo("Focaccia")
        assertThat(source.at("/favorites/2/size").textValue()).isEqualTo("big")
    }

    private fun submission(): ObjectNode {
        val submission = JsonNodeFactory.instance.objectNode()
        submission.put("name", "Focaccia")
        submission.put("size", "big")
        return submission
    }

    private fun emptySubmission(): ObjectNode {
        return JsonNodeFactory.instance.objectNode()
    }

    private fun submissionNewArray(): ObjectNode {
        val submission = JsonNodeFactory.instance.objectNode()
        submission.put("_id", "3")
        submission.put("name", "Focaccia")
        submission.put("size", "big")
        return submission
    }

    private fun placeholders(): ObjectNode {
        val placeholders = JsonNodeFactory.instance.objectNode()
        val jsonNode = JsonNodeFactory.instance.objectNode()
        jsonNode.put("breadId", "2")
        placeholders.set<JsonNode>("pv", jsonNode)
        return placeholders
    }

    private fun source(): ObjectNode {
        val source = JsonNodeFactory.instance.objectNode()
        val breads = JsonNodeFactory.instance.arrayNode()
        val whiteBread = JsonNodeFactory.instance.objectNode()
        whiteBread.put("_id", "1")
        whiteBread.put("name", "White bread")
        whiteBread.put("size", "medium")
        breads.add(whiteBread)
        val pitaBread = JsonNodeFactory.instance.objectNode()
        pitaBread.put("_id", "2")
        pitaBread.put("name", "Pita bread")
        pitaBread.put("size", "small")
        breads.add(pitaBread)
        source.set<JsonNode>("favorites", breads)
        return source
    }
}
