/*
 * Copyright 2015-2024 Ritense BV, the Netherlands.
 *
 *  Licensed under EUPL, Version 1.2 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" basis,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.ritense.valtimo.formflow.event

import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.formflow.domain.instance.FormFlowInstance
import com.ritense.formflow.domain.instance.FormFlowStepInstance
import com.ritense.formflow.service.FormFlowService
import com.ritense.outbox.domain.BaseEvent
import com.ritense.valtimo.formflow.BaseIntegrationTest
import java.util.function.Supplier
import org.assertj.core.api.Assertions.assertThat
import org.json.JSONObject
import org.junit.jupiter.api.Test
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional

@Transactional
internal class FormFlowStepCompletedIntTest : BaseIntegrationTest() {

    @Autowired
    lateinit var formFlowService: FormFlowService

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Test
    fun `should send outbox event when completing formflow step`() {
        val formFlowInstance = startFormFlow("loan:latest")

        val formFlowStepInstance = formFlowInstance.getCurrentStep()
        completeStep(formFlowInstance)

        val eventCapture = argumentCaptor<Supplier<BaseEvent>>()
        verify(outboxService, times(1)).send(eventCapture.capture())
        val event = eventCapture.firstValue
        assertThat(event.get().type).isEqualTo("com.ritense.valtimo.formflow.step.completed")
        assertThat(event.get().resultType).isEqualTo("com.ritense.valtimo.formflow.event.FormFlowStepCompletedResult")
        assertThat(event.get().resultId).isEqualTo(formFlowStepInstance.id.id.toString())
        assertThat(event.get().result).isEqualTo(
            objectMapper.valueToTree(
                FormFlowStepCompletedResult.of(
                    formFlowStepInstance
                )
            )
        )
    }

    private fun startFormFlow(formFlowDefinitionId: String): FormFlowInstance {
        val formFlowDefinition = formFlowService.findDefinition(formFlowDefinitionId)!!
        return formFlowService.save(formFlowDefinition.createInstance(emptyMap()))
    }

    private fun completeStep(instance: FormFlowInstance): FormFlowStepInstance {
        instance.getCurrentStep().open()
        val formFlowStepInstance = instance.complete(
            instance.currentFormFlowStepInstanceId!!,
            JSONObject("""{"test":"data"}""")
        )
        formFlowService.save(instance)
        return formFlowStepInstance!!
    }

}
