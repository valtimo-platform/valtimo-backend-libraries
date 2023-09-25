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

import com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath
import com.ritense.authorization.AuthorizationContext.Companion.runWithoutAuthorization
import com.ritense.document.domain.Document
import com.ritense.document.domain.impl.request.NewDocumentRequest
import com.ritense.document.service.DocumentService
import com.ritense.form.BaseIntegrationTest
import com.ritense.form.domain.Mapper
import com.ritense.valtimo.service.CamundaProcessService
import java.time.LocalDate
import org.camunda.bpm.engine.TaskService
import org.hamcrest.CoreMatchers.hasItem
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional

class PrefillFormServiceIntTest @Autowired constructor(
    private val prefillFormService: PrefillFormService,
    private val documentService: DocumentService,
    private val processService: CamundaProcessService,
    private val taskService: TaskService
): BaseIntegrationTest() {

    @Test
    @Transactional
    fun `should prefill using the dataKey properties`() {
        val xxx = formDefinitionRepository.findByName("form-example-nested-components").get()
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
                mapOf("lastName" to "Doe")
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

        val json = prefilledFormDefinition.asJson().toString()
        assertThat(json, hasJsonPath("""${'$'}.components[?(@.key == "valueResolverDoc")].defaultValue""", hasItem("John")));
        assertThat(json, hasJsonPath("""${'$'}.components[?(@.key == "valueResolverProcessVar")].defaultValue""", hasItem("Doe")));
        assertThat(json, hasJsonPath("""${'$'}.components[?(@.key == "valueResolverTaskVar")].defaultValue""", hasItem(now.toString())));
    }

    private fun createDocument(definitionName: String, content: String): Document {
        return runWithoutAuthorization { documentService.createDocument(NewDocumentRequest(
            definitionName,
            Mapper.INSTANCE.get().readTree(
                content.trimIndent()
            )
        )
        )}.resultingDocument().get()
    }

}