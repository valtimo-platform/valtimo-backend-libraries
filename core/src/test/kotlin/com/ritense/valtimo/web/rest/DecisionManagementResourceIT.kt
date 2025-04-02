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

package com.ritense.valtimo.web.rest

import com.ritense.authorization.AuthorizationContext.Companion.runWithoutAuthorization
import com.ritense.valtimo.BaseIntegrationTest
import com.ritense.valtimo.contract.case_.CaseDefinitionId
import com.ritense.valtimo.security.DecisionHttpSecurityConfigurer.Companion.DECISION_MANAGEMENT_URL
import com.ritense.valtimo.service.CamundaProcessService
import org.camunda.bpm.engine.RepositoryService
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.context.WebApplicationContext
import java.io.ByteArrayInputStream
import kotlin.test.Test
import kotlin.test.assertEquals

@Transactional
class DecisionManagementResourceIT(
    @Autowired
    private val camundaProcessService: CamundaProcessService,

    @Autowired
    private val webApplicationContext: WebApplicationContext,

    @Autowired
    private val repositoryService: RepositoryService
): BaseIntegrationTest() {
    val caseDefinitionId = CaseDefinitionId("everything", "1.0.0")

    lateinit var mockMvc: MockMvc
    lateinit var testDecisionId : String

    @BeforeEach
    fun setup() {
        testDecisionId = deployExampleDmn("test", caseDefinitionId)
        deployExampleDmn("test-version", caseDefinitionId)
        deployExampleDmn("test-other", caseDefinitionId)

        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build()
    }

    @Test
    fun `should get decision definitions`() {
        mockMvc.perform(
            get(DECISION_MANAGEMENT_URL, "everything", "1.0.0")
                .accept(MediaType.APPLICATION_JSON_VALUE)
        )
        .andDo(MockMvcResultHandlers.print())
        .andExpect(status().isOk)
        .andExpect(jsonPath("$").isNotEmpty)
        .andExpect(jsonPath("$").isArray)
        .andExpect(jsonPath("$[0].versionTag").value("CD:everything:1.0.0"))
    }

    @Test
    fun `should create a new decision definition`() {
        mockMvc.perform(
            MockMvcRequestBuilders.multipart(DECISION_MANAGEMENT_URL, "everything", "1.0.0")
                .file(
                    MockMultipartFile(
                        "file",
                        "test-decision.dmn",
                        MediaType.APPLICATION_XML_VALUE,
                        getDecisionXml("test-2").toByteArray()
                    )
                )
                .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE)
        )
        .andDo(MockMvcResultHandlers.print())
        .andExpect(status().isNoContent)

        repositoryService.createDecisionDefinitionQuery()
            .decisionDefinitionKey("test-2")
            .singleResult()
            .let {
                assertEquals("test-2", it.key)
                assertEquals("CD:everything:1.0.0", it.versionTag)
            }
    }

    @Test
    fun `should delete an existing decision definition`() {
        mockMvc.perform(
            MockMvcRequestBuilders.delete("$DECISION_MANAGEMENT_URL/{decisionDefinitionId}", "everything", "1.0.0", "test")
                .accept(MediaType.APPLICATION_JSON_VALUE)
        )
        .andDo(MockMvcResultHandlers.print())
        .andExpect(status().isNoContent)

        val numberFound = repositoryService.createDecisionDefinitionQuery()
            .decisionDefinitionKey("test")
            .count()

        assertEquals(0, numberFound)
    }

    private fun deployExampleDmn(key: String, caseDefinitionId: CaseDefinitionId): String {
        val dmnExample = getDecisionXml(key)

        val deployment = runWithoutAuthorization {
            camundaProcessService.deploy(
                caseDefinitionId,
                "test.dmn",
                ByteArrayInputStream(dmnExample.toByteArray()),
                true,
                false
            )
        }

        return deployment.deployedDecisionDefinitions[0].id
    }

    private fun getDecisionXml(key: String): String {
        return """
            <?xml version="1.0" encoding="UTF-8"?>
            <definitions xmlns="https://www.omg.org/spec/DMN/20191111/MODEL/" xmlns:dmndi="https://www.omg.org/spec/DMN/20191111/DMNDI/" xmlns:dc="http://www.omg.org/spec/DMN/20180521/DC/" xmlns:modeler="http://camunda.org/schema/modeler/1.0" id="Definitions_014kiyg" name="DRD" namespace="http://camunda.org/schema/1.0/dmn" exporter="Camunda Modeler" exporterVersion="5.23.0" modeler:executionPlatform="Camunda Platform" modeler:executionPlatformVersion="7.21.0">
              <decision id="$key" name="Test Decision">
                <decisionTable id="${key}_table">
                  <input id="Input_1">
                    <inputExpression id="InputExpression_1" typeRef="string">
                      <text>field</text>
                    </inputExpression>
                  </input>
                  <output id="Output_1" name="out" typeRef="string" />
                  <rule id="DecisionRule_0hddff8">
                    <inputEntry id="UnaryTests_1phmjs7">
                      <text>test</text>
                    </inputEntry>
                    <outputEntry id="LiteralExpression_0hvov30">
                      <text>test 2</text>
                    </outputEntry>
                  </rule>
                </decisionTable>
              </decision>
              <dmndi:DMNDI>
                <dmndi:DMNDiagram>
                  <dmndi:DMNShape dmnElementRef="$key">
                    <dc:Bounds height="80" width="180" x="160" y="100" />
                  </dmndi:DMNShape>
                </dmndi:DMNDiagram>
              </dmndi:DMNDI>
            </definitions>
        """.trimIndent()
    }
}