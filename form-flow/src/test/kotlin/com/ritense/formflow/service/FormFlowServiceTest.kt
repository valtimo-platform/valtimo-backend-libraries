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

import com.ritense.formflow.domain.definition.FormFlowDefinition
import com.ritense.formflow.domain.definition.FormFlowDefinitionId
import com.ritense.formflow.domain.definition.FormFlowStep
import com.ritense.formflow.domain.definition.FormFlowStepId
import com.ritense.formflow.domain.instance.FormFlowInstance
import com.ritense.formflow.exception.FormFlowExpressionExecutionException
import com.ritense.formflow.exception.FormFlowExpressionParseException
import com.ritense.formflow.repository.FormFlowDefinitionRepository
import com.ritense.formflow.repository.FormFlowInstanceRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock

internal class FormFlowServiceTest {

    lateinit var formFlowService: FormFlowService

    @BeforeEach
    fun beforeAll() {
        val formFlowDefinitionRepository = mock(FormFlowDefinitionRepository::class.java)
        val formFlowInstanceRepository = mock(FormFlowInstanceRepository::class.java)
        formFlowService = FormFlowService(
            formFlowDefinitionRepository, formFlowInstanceRepository
        )
    }

    @Test
    fun `should parse SPeL expression`() {
        val spelExpression = formFlowService.parseSpelExpression("\${'Hello '+'World!'}")

        assertThat(spelExpression).isNotNull
    }

    @Test
    fun `should throw exception when error in syntax of onOpenExpression`() {
        assertThat(assertThrows<FormFlowExpressionParseException> {
            formFlowService.parseSpelExpression("\${'Hello +'World!'}")
        }.message).isEqualTo("Failed to parse Form Flow expression, \${'Hello +'World!'}")
    }

    @Test
    fun `should handle multiple onOpen expressions when opening a form flow instance`() {
        val instance = createAndOpenFormFlowInstance(
            onOpen = mutableListOf(
                "\${'Hello '+'World!'}", "\${3 / 1}"
            )
        )

        formFlowService.open(instance)
    }

    @Test
    fun `should throw exception when error while executing onOpenExpression`() {
        val instance = createAndOpenFormFlowInstance(
            onOpen = mutableListOf("\${3 / 0}")
        )

        assertThat(assertThrows<FormFlowExpressionExecutionException> {
            formFlowService.open(instance)
        }.message).isEqualTo("Error while executing Form Flow onOpen expression. In 'test' with '\${3 / 0}'")
    }

    private fun createAndOpenFormFlowInstance(onOpen: MutableList<String>): FormFlowInstance {
        val step = FormFlowStep(
            FormFlowStepId("test"),
            ArrayList(),
            onOpen
        )
        val definition = FormFlowDefinition(
            FormFlowDefinitionId("test", 1L), "start-step", setOf(step)
        )
        return FormFlowInstance(
            formFlowDefinition = definition
        )
    }
}