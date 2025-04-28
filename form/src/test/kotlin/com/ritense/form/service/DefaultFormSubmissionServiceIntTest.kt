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

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath
import com.jayway.jsonpath.matchers.JsonPathMatchers.hasNoJsonPath
import com.ritense.authorization.AuthorizationContext.Companion.runWithoutAuthorization
import com.ritense.document.domain.impl.request.NewDocumentRequest
import com.ritense.document.service.impl.JsonSchemaDocumentService
import com.ritense.form.BaseIntegrationTest
import com.ritense.form.TestValueResolverFactory
import com.ritense.form.service.impl.DefaultFormSubmissionService
import com.ritense.processdocument.domain.impl.request.NewDocumentAndStartProcessRequest
import com.ritense.processdocument.service.ProcessDocumentService
import com.ritense.processlink.service.ProcessLinkService
import com.ritense.valtimo.camunda.repository.CamundaTaskSpecificationHelper.Companion.byProcessInstanceId
import com.ritense.valtimo.service.CamundaProcessService
import com.ritense.valtimo.service.CamundaTaskService
import org.camunda.bpm.engine.RuntimeService
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.reset
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

class DefaultFormSubmissionServiceIntTest @Autowired constructor(
    private val defaultFormSubmissionService: DefaultFormSubmissionService,
    private val processLinkService: ProcessLinkService,
    private val documentService: JsonSchemaDocumentService,
    private val processDocumentService: ProcessDocumentService,
    private val taskService: CamundaTaskService,
    private val processService: CamundaProcessService,
    private val testValueResolverFactory: TestValueResolverFactory,
    private val objectMapper: ObjectMapper,
    private val runtimeService: RuntimeService,
) : BaseIntegrationTest() {

    @BeforeEach
    fun setUp() {
        reset(testValueResolverFactory)
    }

    @Test
    @Transactional
    fun `should successfully submit form for new document and process`() {
        val processLink = runWithoutAuthorization {
            val processLinksByProcessDefinitionKey =
                processLinkService.getProcessLinksByProcessDefinitionKey("prefill-process")
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
        val json = objectMapper.writeValueAsString(document.content().asJson())
        assertThat(json, hasJsonPath("""${'$'}.personalInformation.firstName""", equalTo("John")))
        assertThat(json, hasJsonPath("""${'$'}.fruitTypes[0].apples""", equalTo(3)))
        assertThat(json, hasNoJsonPath("""${'$'}.fruitTypes[1]"""))
        assertThat(json, hasNoJsonPath("""${'$'}.apples"""))
        assertThat(json, hasNoJsonPath("""${'$'}.favorites"""))
        assertThat(json, hasNoJsonPath("""${'$'}.name"""))
        assertThat(json, hasJsonPath("""${'$'}.hiddenInputTrue""", equalTo("test-value")))
        assertThat(json, hasNoJsonPath("""${'$'}.hiddenInputFalse"""))
        assertThat(json, hasNoJsonPath("""${'$'}.inputDisabled"""))
        assertThat(json, hasJsonPath("""${'$'}.arrayInDocument[0].property1""", equalTo("property1")))
        assertThat(json, hasJsonPath("""${'$'}.arrayInDocument[0].property2""", equalTo("property2")))
        assertThat(json, hasNoJsonPath("""${'$'}.arrayInDocument[0].property3"""))
        assertThat(json, hasNoJsonPath("""${'$'}.property1"""))
        assertThat(json, hasNoJsonPath("""${'$'}.property2"""))
        assertThat(json, hasNoJsonPath("""${'$'}.property3"""))
        assertThat(json, hasJsonPath("""${'$'}.informatieverzoeken[0].verzoek.jaartallen""", equalTo("2010")))
        assertThat(json, hasJsonPath("""${'$'}.informatieverzoeken[0].verzoek.toelichting""", equalTo("From 2010")))
        assertThat(json, hasNoJsonPath("""${'$'}.informatieverzoeken[0].verzoek.missing"""))
        assertThat(json, hasNoJsonPath("""${'$'}.verzoek"""))
        assertThat(json, hasJsonPath("""${'$'}.containerArray[0].container.containerProperty1""", equalTo("containerProperty1")))
        assertThat(json, hasJsonPath("""${'$'}.containerArray[0].container.containerProperty2""", equalTo("containerProperty2")))
        assertThat(json, hasNoJsonPath("""${'$'}.containerArray[0].container.containerProperty3"""))
        assertThat(json, hasNoJsonPath("""${'$'}.containerProperty1"""))
        assertThat(json, hasNoJsonPath("""${'$'}.containerProperty2"""))
        assertThat(json, hasNoJsonPath("""${'$'}.containerProperty3"""))
        assertThat(json, hasJsonPath("""${'$'}.aanvrager.geslacht""", equalTo("M")))
        assertThat(json, hasJsonPath("""${'$'}.aanvrager.persoonsgegevens.voornaam""", equalTo("Henk")))

        val pv = runWithoutAuthorization {
            processService.findExecutionByBusinessKey(businessKey).variables
        }
        assertThat(pv["userLastName"], equalTo("Doe"))
        assertThat(pv["dateOfBirth"], equalTo("1980-02-03"))
        assertThat(pv["object"], equalTo(mapOf("property2" to "value2")))

        val argumentCaptor = argumentCaptor<Map<String, Any?>>()
        verify(testValueResolverFactory).handleValues(any<UUID>(), argumentCaptor.capture())
        assertThat(argumentCaptor.firstValue["gender"], equalTo("M"))
    }

    @Test
    @Transactional
    fun `should successfully submit form for existing document and process`() {
        val processLink = runWithoutAuthorization {
            processLinkService.getProcessLinksByProcessDefinitionKey("prefill-process")
                .first { it.activityId == "prefill-task" }
        }
        val result = runWithoutAuthorization {
            processDocumentService.newDocumentAndStartProcess(
                NewDocumentAndStartProcessRequest(
                    "prefill-process",
                    NewDocumentRequest(
                        "person",
                        objectMapper.readTree(
                            """{
                                "fruitTypes":[{"apples":2}],
                                "favorites":[
                                    {"_id":"1","name":"White bread"},
                                    {"_id":"2","name":"Pita bread"}
                                ]
                            }"""
                        )
                    )
                ).withProcessVars(mapOf("breadId" to "2", "object" to mapOf("property1" to "value1")))
            )
        }
        val document = result.resultingDocument().get()
        val processInstanceId = result.resultingProcessInstanceId().get().toString()
        val task = runWithoutAuthorization {
            taskService.findTasks(byProcessInstanceId(processInstanceId)).first()
        }

        val submissionResult = runWithoutAuthorization {
            defaultFormSubmissionService.handleSubmission(
                processLink.id,
                createFormData(),
                "person",
                document.id().toString(),
                task.id
            )
        }

        assertThat(submissionResult.errors().size, equalTo(0))
        val json = objectMapper.writeValueAsString(document.content().asJson())
        assertThat(json, hasJsonPath("""${'$'}.fruitTypes[0].apples""", equalTo(2)))
        assertThat(json, hasJsonPath("""${'$'}.fruitTypes[1].apples""", equalTo(3)))
        assertThat(json, hasNoJsonPath("""${'$'}.fruitTypes[2]"""))
        assertThat(json, hasNoJsonPath("""${'$'}.apples"""))
        assertThat(json, hasJsonPath("""${'$'}.favorites[0]._id""", equalTo("1")))
        assertThat(json, hasJsonPath("""${'$'}.favorites[0].name""", equalTo("White bread")))
        assertThat(json, hasJsonPath("""${'$'}.favorites[1]._id""", equalTo("2")))
        assertThat(json, hasJsonPath("""${'$'}.favorites[1].name""", equalTo("Focaccia")))
        assertThat(json, hasNoJsonPath("""${'$'}.favorites[2]"""))
        assertThat(json, hasNoJsonPath("""${'$'}.name"""))
        assertThat(json, hasJsonPath("""${'$'}.hiddenInputTrue""", equalTo("test-value")))
        assertThat(json, hasNoJsonPath("""${'$'}.hiddenInputFalse"""))
        assertThat(json, hasNoJsonPath("""${'$'}.inputDisabled"""))
        assertThat(json, hasJsonPath("""${'$'}.arrayInDocument[0].property1""", equalTo("property1")))
        assertThat(json, hasJsonPath("""${'$'}.arrayInDocument[0].property2""", equalTo("property2")))
        assertThat(json, hasNoJsonPath("""${'$'}.property1"""))
        assertThat(json, hasNoJsonPath("""${'$'}.property2"""))
        assertThat(json, hasJsonPath("""${'$'}.informatieverzoeken[0].verzoek.jaartallen""", equalTo("2010")))
        assertThat(json, hasJsonPath("""${'$'}.informatieverzoeken[0].verzoek.toelichting""", equalTo("From 2010")))
        assertThat(json, hasNoJsonPath("""${'$'}.verzoek"""))
        assertThat(json, hasJsonPath("""${'$'}.containerArray[0].container.containerProperty1""", equalTo("containerProperty1")))
        assertThat(json, hasJsonPath("""${'$'}.containerArray[0].container.containerProperty2""", equalTo("containerProperty2")))
        assertThat(json, hasNoJsonPath("""${'$'}.containerArray[0].container.containerProperty3"""))
        assertThat(json, hasNoJsonPath("""${'$'}.containerProperty1"""))
        assertThat(json, hasNoJsonPath("""${'$'}.containerProperty2"""))
        assertThat(json, hasNoJsonPath("""${'$'}.containerProperty3"""))
        assertThat(json, hasJsonPath("""${'$'}.aanvrager.geslacht""", equalTo("M")))
        assertThat(json, hasJsonPath("""${'$'}.aanvrager.persoonsgegevens.voornaam""", equalTo("Henk")))
        val pv = runtimeService.getVariables(processInstanceId) as Map<*, *>
        assertThat(pv["object"], equalTo(mapOf("property1" to "value1", "property2" to "value2")))
    }

    private fun createFormData(): JsonNode {
        return objectMapper.readTree(
            """
            {
                "vrDocFirstName": "John",
                "vrPvLastName": "Doe",
                "vrTestGender": "M",
                "vrPvTaskDateOfBirth": "1980-02-03",
                "apples": 3,
                "name": "Focaccia",
                "hiddenInputTrue": "test-value",
                "hiddenInputFalse": "test-value",
                "inputDisabled": "test-value",
                "property1": "property1",
                "property2": "property2",
                "verzoek": {"jaartallen":"2010","toelichting":"From 2010"},
                "container": {"containerProperty1":"containerProperty1","containerProperty2":"containerProperty2"},
                "aanvrager":{"geslacht":"M","persoonsgegevens":{"voornaam":"Henk"}},
                "pv": {
                    "object":{"property2":"value2"}
                }
            }
        """.trimIndent()
        )
    }
}