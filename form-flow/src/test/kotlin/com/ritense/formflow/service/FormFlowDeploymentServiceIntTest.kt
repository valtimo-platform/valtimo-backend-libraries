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

package com.ritense.formflow.service

import com.fasterxml.jackson.databind.exc.InvalidTypeIdException
import com.ritense.formflow.BaseIntegrationTest
import com.ritense.formflow.expression.ExpressionParseException
import com.ritense.valtimo.contract.case_.CaseDefinitionId
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional

@Transactional
internal class FormFlowDeploymentServiceIntTest : BaseIntegrationTest() {

    @Autowired
    lateinit var formFlowService: FormFlowService

    @Autowired
    lateinit var formFlowDeploymentService: FormFlowDeploymentService

    @Test
    fun `should deploy Form Flow when nextStep is provided`() {
        val caseDefinitionId = CaseDefinitionId("profile", "1.0.0")
        formFlowDeploymentService.deploy(
            "testOnOpenExpression", """
            {
                "startStep": "woonplaats",
                "steps": [
                    {
                        "key": "woonplaats",
                        "nextStep": "leeftijd",
                        "type": {
                            "name": "form",
                            "properties": {
                                "definition": "my-form-definition"
                            }
                        }
                    }
                ]
            }
        """.trimIndent(),
            caseDefinitionId
        )
    }

    @Test
    fun `should deploy new version Form Flow`() {
        val caseDefinitionId = CaseDefinitionId("profile", "1.0.0")
        var inkomensLoketJson = readFileAsString("/config/form-flow/inkomens_loket.json")
        inkomensLoketJson = inkomensLoketJson.replace("4*3", "5*2")
        formFlowDeploymentService.deploy("inkomens_loket", inkomensLoketJson, caseDefinitionId)

        val inkomensLoket = formFlowService.findLatestDefinitionByKey("inkomens_loket", caseDefinitionId)

        assertThat(inkomensLoket!!.id.version).isEqualTo(2L)
    }

    @Test
    fun `should fail to deploy Form Flow when error in onOpenExpression`() {
        val caseDefinitionId = CaseDefinitionId("profile", "1.0.0")
        assertThrows<ExpressionParseException> {
            formFlowDeploymentService.deploy(
                "testOnOpenExpression", """
                {
                    "startStep": "woonplaats",
                    "steps": [
                        {
                            "key": "woonplaats",
                            "onOpen": ["${'$'}{'Hello +'world!'}"],
                            "nextSteps": [
                                {
                                    "step": "leeftijd"
                                }
                            ],
                            "type": {
                                "name": "form",
                                "properties": {
                                    "definition": "my-form-definition"
                                }
                            }
                        }
                    ]
                }
            """.trimIndent(),
                caseDefinitionId
            )
        }
    }

    @Test
    fun `should fail to deploy Form Flow on unknown properties type`() {
        assertThrows<InvalidTypeIdException> {
            val caseDefinitionId = CaseDefinitionId("profile", "1.0.0")
            formFlowDeploymentService.deploy(
                "testPropertiesType", """
                {
                    "startStep": "woonplaats",
                    "steps": [
                        {
                            "key": "woonplaats",
                            "nextSteps": [
                                {
                                    "step": "leeftijd"
                                }
                            ],
                            "type": {
                                "name": "unknown",
                                "properties": {}
                            }
                        }
                    ]
                }
            """.trimIndent(),
                caseDefinitionId
            )
        }
    }
}

