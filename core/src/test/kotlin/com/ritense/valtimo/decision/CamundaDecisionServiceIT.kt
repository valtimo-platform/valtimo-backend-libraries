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

package com.ritense.valtimo.decision

import com.ritense.valtimo.BaseIntegrationTest
import com.ritense.valtimo.contract.case_.CaseDefinitionId
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.model.dmn.Dmn
import org.camunda.bpm.model.dmn.instance.Decision
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import java.util.function.Consumer
import kotlin.test.assertEquals

class CamundaDecisionServiceIT(
    @Autowired
    private val repositoryService: RepositoryService,

    @Autowired
    private val camundaDecisionService: CamundaDecisionService

): BaseIntegrationTest() {

    @Test
    fun `should not delete when multiple decision are in the deployment`() {

        val dmnModel1 = Dmn.readModelFromStream(getDecisionXml("delete-test-1").byteInputStream())
        dmnModel1.getDefinitions().getChildElementsByType<Decision>(Decision::class.java).forEach(
            Consumer { dmn: Decision -> dmn.setVersionTag("test-1.0.0") }
        )

        val dmnModel2 = Dmn.readModelFromStream(getDecisionXml("delete-test-2").byteInputStream())
        dmnModel2.getDefinitions().getChildElementsByType<Decision>(Decision::class.java).forEach(
            Consumer { dmn: Decision -> dmn.setVersionTag("test-1.0.0") }
        )

        val deployment = repositoryService
            .createDeployment()
            .addModelInstance("test1.dmn", dmnModel1)
            .addModelInstance("test2.dmn", dmnModel2)
            .deployWithResult()

        val exception = assertThrows<IllegalStateException> {
            camundaDecisionService.deleteDecisionDefinition(CaseDefinitionId("test", "1.0.0"), "delete-test-1")
        }

        assertEquals("Failed to delete decision definition delete-test-1 for case definition test-1.0.0. " +
            "The deployment ${deployment.id} has more resources than only the single decision definition.", exception.message)
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