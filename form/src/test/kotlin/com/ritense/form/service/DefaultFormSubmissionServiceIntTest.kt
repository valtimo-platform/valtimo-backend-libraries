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
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath
import com.ritense.authorization.AuthorizationContext.Companion.runWithoutAuthorization
import com.ritense.document.service.impl.JsonSchemaDocumentService
import com.ritense.form.BaseIntegrationTest
import com.ritense.form.TestValueResolverFactory
import com.ritense.form.service.impl.DefaultFormSubmissionService
import com.ritense.processlink.service.ProcessLinkService
import com.ritense.valtimo.service.CamundaProcessService
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional

class DefaultFormSubmissionServiceIntTest @Autowired constructor(
    private val defaultFormSubmissionService: DefaultFormSubmissionService,
    private val processLinkService: ProcessLinkService,
    private val documentService: JsonSchemaDocumentService,
    private val processService: CamundaProcessService,
    private val testValueResolverFactory: TestValueResolverFactory
) : BaseIntegrationTest() {



    @Test
    @Transactional
    fun `should successfully submit form for new document and process`() {
        val processLink = runWithoutAuthorization {
            val processLinksByProcessDefinitionKey =
                processLinkService.getProcessLinksByProcessDefinitionKey("form-one-task-process")
            processLinksByProcessDefinitionKey.first { it.activityId == "start-event" }
        }

        val submissionResult = runWithoutAuthorization {
            defaultFormSubmissionService.handleSubmission(
                processLink.id,
                createFormData(),
                "person",
                null,
                null
            )
        }

        val document = runWithoutAuthorization { documentService.get(submissionResult.documentId()) }
        val businessKey = document.id.id.toString()
        val json = jacksonObjectMapper().writeValueAsString(document.content().asJson())
        assertThat(json, hasJsonPath("""${'$'}.person.firstName""", equalTo("John")))

        val processExecution = runWithoutAuthorization {
            processService.findExecutionByBusinessKey(businessKey)
        }
        val lastName = processExecution?.getVariable("lastName")

        assertThat(lastName, equalTo("Doe"))

        val argumentCaptor = argumentCaptor<Map<String, Any>>()
        verify(testValueResolverFactory).handleValues(any(), argumentCaptor.capture())

        assertThat(argumentCaptor.firstValue["gender"], equalTo("M"))
    }

    private fun createFormData(): JsonNode {
        return jacksonObjectMapper().readTree("""
            {
                "vrDocFirstName": "John",
                "vrPvLastName": "Doe",
                "vrTestGender": "M"
            }
        """.trimIndent())
    }
}