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

import com.nhaarman.mockitokotlin2.isNull
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.ritense.formflow.domain.definition.FormFlowDefinition
import com.ritense.formflow.domain.definition.FormFlowDefinitionId
import com.ritense.formflow.domain.definition.FormFlowStep
import com.ritense.formflow.domain.definition.FormFlowStepId
import com.ritense.formflow.domain.instance.FormFlowInstance
import com.ritense.formflow.expression.ExpressionProcessor
import com.ritense.formflow.expression.ExpressionProcessorFactory
import com.ritense.formflow.expression.spel.SpelExpressionProcessor
import com.ritense.formflow.expression.spel.SpelExpressionProcessorFactory
import com.ritense.formflow.repository.FormFlowDefinitionRepository
import com.ritense.formflow.repository.FormFlowInstanceRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.any
import org.mockito.Mockito.anyString
import org.mockito.Mockito.mock

internal class FormFlowServiceTest {

    lateinit var formFlowService: FormFlowService
    lateinit var formFlowInstanceRepository: FormFlowInstanceRepository
    lateinit var expressionProcessor: ExpressionProcessor

    @BeforeEach
    fun beforeAll() {
        val formFlowDefinitionRepository = mock(FormFlowDefinitionRepository::class.java)
        formFlowInstanceRepository = mock(FormFlowInstanceRepository::class.java)
        val expressionProcessorFactory = mock(ExpressionProcessorFactory::class.java)
        formFlowService = FormFlowService(
            formFlowDefinitionRepository,
            formFlowInstanceRepository,
            expressionProcessorFactory
        )

        expressionProcessor = spy(SpelExpressionProcessor())
        whenever(expressionProcessorFactory.create(any())).thenReturn(expressionProcessor)
    }

    @Test
    fun `should handle multiple onOpen expressions when opening a form flow instance`() {
        val instance = createAndOpenFormFlowInstance(
            onOpen = listOf(
                "\${'Hello '+'World!'}",
                "\${3 / 1}"
            )
        )

        formFlowService.open(instance.id)
        verify(expressionProcessor, times(2)).process<Any>(anyString(), isNull())
    }

    @Test
    fun `should handle multiple onComplete expressions when completing a form flow instance`() {
        val instance = createAndOpenFormFlowInstance(
            onComplete = listOf(
                "\${'Hello '+'World!'}",
                "\${3 / 1}"
            )
        )

        formFlowService.complete(instance.id)
        verify(expressionProcessor, times(2)).process<Any>(anyString(), isNull())
    }

    private fun createAndOpenFormFlowInstance(onOpen: List<String>? = null, onComplete: List<String>? = null): FormFlowInstance {
        val step = FormFlowStep(
            FormFlowStepId("start-step"),
            ArrayList(),
            onOpen?.toMutableList(),
            onComplete?.toMutableList()
        )
        val definition = FormFlowDefinition(
            FormFlowDefinitionId("test", 1L), "start-step", setOf(step)
        )
        val formFlowInstance = FormFlowInstance(
            formFlowDefinition = definition
        )

        whenever(formFlowInstanceRepository.getById(formFlowInstance.id)).thenReturn(formFlowInstance)

        return formFlowInstance
    }
}