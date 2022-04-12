/*
 * Copyright 2015-2022 Ritense BV, the Netherlands.
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

import com.ritense.formflow.BaseIntegrationTest
import com.ritense.formflow.expression.ExpressionParseException
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
    fun `should auto deploy Form Flow from config directory`() {
        formFlowDeploymentService.deployAll()

        val inkomensLoket = formFlowService.findLatestDefinitionByKey("inkomens_loket")

        assertThat(inkomensLoket!!).isNotNull
        assertThat(inkomensLoket.id.key).isEqualTo("inkomens_loket")
        assertThat(inkomensLoket.id.version).isEqualTo(1L)
        assertThat(inkomensLoket.startStep).isEqualTo("woonplaats")
        assertThat(inkomensLoket.steps).hasSize(7)
    }

    @Test
    fun `should not deploy same Form Flow twice`() {
        formFlowDeploymentService.deployAll()
        formFlowDeploymentService.deployAll()

        val inkomensLoket = formFlowService.findLatestDefinitionByKey("inkomens_loket")

        assertThat(inkomensLoket!!.id.version).isEqualTo(1L)
    }

    @Test
    fun `should deploy new version Form Flow`() {
        var inkomensLoketJson = readFileAsString("/config/form-flow/inkomens_loket.json")
        inkomensLoketJson = inkomensLoketJson.replace("isOuderDan21 == true", "isOuderDan21 == false")
        formFlowDeploymentService.deploy("inkomens_loket", inkomensLoketJson)

        val inkomensLoket = formFlowService.findLatestDefinitionByKey("inkomens_loket")

        assertThat(inkomensLoket!!.id.version).isEqualTo(2L)
    }

    @Test
    fun `should fail to deploy Form Flow when error in onOpenExpression`() {
        assertThrows<ExpressionParseException> {
            formFlowDeploymentService.deploy(
                "test", """
                {
                    "startStep": "woonplaats",
                    "steps": [
                        {
                            "key": "woonplaats",
                            "onOpen": ["$\{'Hello +'world!'}"],
                            "nextStep": "leeftijd"
                        }
                    ]
                }
            """.trimIndent()
            )
        }
    }
}

