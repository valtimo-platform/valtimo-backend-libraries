/*
 * Copyright 2015-2024 Ritense BV, the Netherlands.
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

import com.fasterxml.jackson.databind.ObjectMapper
import com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath
import com.ritense.authorization.AuthorizationContext.Companion.runWithoutAuthorization
import com.ritense.document.domain.Document
import com.ritense.document.domain.impl.request.NewDocumentRequest
import com.ritense.document.service.DocumentService
import com.ritense.form.BaseIntegrationTest
import com.ritense.valtimo.service.CamundaProcessService
import org.camunda.bpm.engine.TaskService
import org.hamcrest.CoreMatchers.hasItem
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

class PrefillFormServiceIntTest @Autowired constructor(
    private val prefillFormService: PrefillFormService,
    private val documentService: DocumentService,
    private val processService: CamundaProcessService,
    private val taskService: TaskService,
    private val objectMapper: ObjectMapper,
): BaseIntegrationTest() {

    @Test
    @Transactional
    fun `should prefill a form`() {
        val formDefinition = formDefinitionRepository.findByName("form-example-various-prefill-fields").get()
        val document = createDocument("person", """
            {
                "person": {
                    "firstName": "John"
                }
            }
        """.trimIndent())

        val instance = runWithoutAuthorization {
            processService.startProcess(
                "form-one-task-process",
                document.id().toString(),
                mapOf(
                    "lastName" to "Doe"
                )
            )
        }

        val task = taskService.createTaskQuery()
            .processInstanceId(instance.processInstanceDto.id)
            .taskDefinitionKey("do-something")
            .singleResult() ?: throw NullPointerException("Task was null")

        val now = LocalDate.now()
        taskService.setVariablesLocal(
            task.id, mapOf(
                "dateOfBirth" to now
            )
        )

        val prefilledFormDefinition = prefillFormService.getPrefilledFormDefinition(formDefinition.id!!, task.processInstanceId, task.id)

        val prefilledJson = prefilledFormDefinition.asJson().toString()
        //value resolver prefill
        assertThat(prefilledJson, hasJsonPath("""${'$'}.components[?(@.key == "vrDocFirstName")].defaultValue""", hasItem("John")))
        assertThat(prefilledJson, hasJsonPath("""${'$'}.components[?(@.key == "vrPvLastName")].defaultValue""", hasItem("Doe")))
        assertThat(prefilledJson, hasJsonPath("""${'$'}.components[?(@.key == "vrPvTaskDateOfBirth")].defaultValue""", hasItem(now.toString())))
        assertThat(prefilledJson, hasJsonPath("""${'$'}.components[?(@.key == "vrTestGender")].defaultValue""", hasItem("MyGenderValue")))

        //legacy prefill
        assertThat(prefilledJson, hasJsonPath("""${'$'}.components[?(@.key == "legacy")].components[?(@.key == "person.firstName")].defaultValue""", hasItem("John")))
        assertThat(prefilledJson, hasJsonPath("""${'$'}.components[?(@.key == "legacy")].components[?(@.key == "pv.lastName")].defaultValue""", hasItem("Doe")))
        assertThat(prefilledJson, hasJsonPath("""${'$'}.components[?(@.key == "legacy")].components[?(@.key == "test:separatedByColon")].defaultValue""", hasItem("MySeparatedByColonValue")))
        assertThat(prefilledJson, hasJsonPath("""${'$'}.components[?(@.key == "legacy")].components[?(@.key == "test.separatedByDot")].defaultValue""", hasItem("MySeparatedByDotValue")))
    }

    @Test
    @Transactional
    fun `should only overwrite defaultValue when value can be resolved`() {
        val formDefinition = formDefinitionRepository.findByName("form-example-various-prefill-fields").get()
        val document = createDocument("person", "{}")

        val instance = runWithoutAuthorization {
            processService.startProcess(
                "form-one-task-process",
                document.id().toString(),
                mapOf()
            )
        }

        val task = taskService.createTaskQuery()
            .processInstanceId(instance.processInstanceDto.id)
            .taskDefinitionKey("do-something")
            .singleResult() ?: throw NullPointerException("Task was null")

        val originalJson = formDefinition.asJson().toString()

        val prefilledFormDefinition = prefillFormService.getPrefilledFormDefinition(formDefinition.id!!, task.processInstanceId, task.id)

        val prefilledJson = prefilledFormDefinition.asJson().toString()

        //value resolver prefill
        assertThat(prefilledJson, hasJsonPath("""${'$'}.components[?(@.key == "vrDocFirstName")].defaultValue""", hasItem("Jane")))
        assertThat(prefilledJson, hasJsonPath("""${'$'}.components[?(@.key == "vrPvLastName")].defaultValue""", hasItem("Don't")))
        assertThat(prefilledJson, hasJsonPath("""${'$'}.components[?(@.key == "vrPvTaskDateOfBirth")].defaultValue""", hasItem("1970-01-01")))
        assertThat(prefilledJson, hasJsonPath("""${'$'}.components[?(@.key == "vrTestGenderUnresolved")].defaultValue""", hasItem("X")))

        //legacy prefill
        assertThat(prefilledJson, hasJsonPath("""${'$'}.components[?(@.key == "legacy")].components[?(@.key == "person.firstName")].defaultValue""", hasItem("Jane")))
        assertThat(prefilledJson, hasJsonPath("""${'$'}.components[?(@.key == "legacy")].components[?(@.key == "pv.lastName")].defaultValue""", hasItem("Don't")))
        assertThat(prefilledJson, hasJsonPath("""${'$'}.components[?(@.key == "legacy")].components[?(@.key == "test:!separatedByColon")].defaultValue""", hasItem("::")))
        assertThat(prefilledJson, hasJsonPath("""${'$'}.components[?(@.key == "legacy")].components[?(@.key == "test.!separatedByDot")].defaultValue""", hasItem("..")))
    }

    private fun createDocument(definitionName: String, content: String): Document {
        return runWithoutAuthorization { documentService.createDocument(NewDocumentRequest(
            definitionName,
            objectMapper.readTree(
                content.trimIndent()
            )
        )
        )}.resultingDocument().get()
    }

}