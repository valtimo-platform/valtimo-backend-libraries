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

package com.ritense.formflow.service

import com.ritense.formflow.BaseTest
import com.ritense.formflow.domain.definition.FormFlowDefinition
import com.ritense.formflow.domain.definition.FormFlowDefinitionId
import com.ritense.formflow.domain.definition.FormFlowStep
import com.ritense.formflow.domain.definition.FormFlowStepId
import com.ritense.formflow.domain.definition.configuration.FormFlowStepType
import com.ritense.formflow.domain.definition.configuration.step.FormStepTypeProperties
import com.ritense.formflow.domain.instance.FormFlowInstance
import com.ritense.formflow.expression.ExpressionProcessorFactoryHolder
import com.ritense.formflow.expression.spel.SpelExpressionProcessor
import com.ritense.formflow.expression.spel.SpelExpressionProcessorFactory
import com.ritense.formflow.repository.FormFlowAdditionalPropertiesSearchRepository
import com.ritense.formflow.repository.FormFlowDefinitionRepository
import com.ritense.formflow.repository.FormFlowInstanceRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.any
import org.mockito.Mockito.anyString
import org.mockito.Mockito.mock
import org.mockito.kotlin.isNull
import org.mockito.kotlin.spy
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.context.ApplicationContext

internal class FormFlowServiceTest : BaseTest() {

    lateinit var formFlowService: FormFlowService
    lateinit var formFlowInstanceRepository: FormFlowInstanceRepository
    lateinit var formFlowAdditionalPropertiesSearchRepository: FormFlowAdditionalPropertiesSearchRepository
    lateinit var expressionProcessor: SpelExpressionProcessor

    @BeforeEach
    fun beforeAll() {
        val formFlowDefinitionRepository = mock(FormFlowDefinitionRepository::class.java)
        formFlowInstanceRepository = mock(FormFlowInstanceRepository::class.java)
        formFlowAdditionalPropertiesSearchRepository = mock(FormFlowAdditionalPropertiesSearchRepository::class.java)
        formFlowService = FormFlowService(
            formFlowDefinitionRepository,
            formFlowInstanceRepository,
            formFlowAdditionalPropertiesSearchRepository,
            emptyList()
        )

        val expressionProcessorFactory = spy(SpelExpressionProcessorFactory())
        expressionProcessorFactory.setFlowProcessBeans(mapOf())
        whenever(expressionProcessorFactory.create(any())).thenAnswer {
            expressionProcessor = spy(it.callRealMethod() as SpelExpressionProcessor)
            expressionProcessor
        }
        ExpressionProcessorFactoryHolder.setInstance(expressionProcessorFactory, mock(ApplicationContext::class.java))
    }

    @Test
    fun `should handle multiple onOpen expressions when opening a form flow instance`() {
        val instance = createAndOpenFormFlowInstance(
            onOpen = listOf(
                "\${'Hello '+'World!'}", "\${3 / 1}"
            )
        )

        instance.getCurrentStep().open()
        verify(expressionProcessor, times(2)).process<Any>(anyString(), isNull())
    }

    @Test
    fun `should handle multiple onComplete expressions when completing a form flow instance`() {
        val instance = createAndOpenFormFlowInstance(
            onComplete = listOf(
                "\${'Hello '+'World!'}", "\${3 / 1}"
            )
        )

        instance.getCurrentStep().complete("{}")
        verify(expressionProcessor, times(2)).process<Any>(anyString(), isNull())
    }

    @Test
    fun `should handle multiple onBack expressions when going back`() {
        val instance = createAndOpenFormFlowInstance(
            onBack = listOf(
                "\${'Hello '+'World!'}", "\${3 / 1}"
            )
        )

        instance.getCurrentStep().back()
        verify(expressionProcessor, times(2)).process<Any>(anyString(), isNull())

    }

    @Test
    fun `should pass variables to expression context`() {
        val instance = createAndOpenFormFlowInstance(
            onOpen = listOf(
                "\${'Hello '+'World!'}", "\${3 / 1}"
            )
        )

        instance.getCurrentStep().open()

        val contextMap = getContextMap(expressionProcessor)
        assertThat((contextMap["step"] as Map<String, Any>)["id"]).isEqualTo(instance.getCurrentStep().id)
        assertThat((contextMap["step"] as Map<String, Any>)["key"]).isEqualTo(instance.getCurrentStep().stepKey)
        assertThat((contextMap["instance"] as Map<String, Any>)["id"]).isEqualTo(instance.id)
    }

    private fun createAndOpenFormFlowInstance(
        onBack: List<String>? = null,
        onOpen: List<String>? = null,
        onComplete: List<String>? = null
    ): FormFlowInstance {
        val step = FormFlowStep(
            FormFlowStepId("start-step"),
            listOf(),
            onBack?: listOf(),
            onOpen?: listOf(),
            onComplete?:listOf(),
            type = FormFlowStepType("form", FormStepTypeProperties("my-form-definition"))
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

    private fun getContextMap(expressionProcessor: SpelExpressionProcessor): Map<String, Any> {
        try {
            val contextMapField = SpelExpressionProcessor::class.java.getDeclaredField("contextMap")
            contextMapField.isAccessible = true
            return contextMapField.get(expressionProcessor) as Map<String, Any>
        } catch (e: NoSuchMethodException) {
            throw IllegalStateException(e)
        }
    }
}
