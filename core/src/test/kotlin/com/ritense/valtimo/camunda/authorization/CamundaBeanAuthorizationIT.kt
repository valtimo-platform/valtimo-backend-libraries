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

package com.ritense.valtimo.camunda.authorization

import com.ritense.valtimo.BaseIntegrationTest
import org.camunda.bpm.engine.DecisionService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional
import java.util.UUID
import kotlin.test.assertEquals

@Transactional
class CamundaBeanAuthorizationIT: BaseIntegrationTest() {

    @Autowired
    lateinit var decisionService: DecisionService

    @Test
    fun `should be able to execute process bean in bpmn service task without authorizing`() {
        runtimeService.startProcessInstanceByKey(
            "authorization-test-service-task",
            UUID.randomUUID().toString()
        )
    }

    @Test
    fun `should be able to execute process bean in bpmn script task without authorizing`() {
        runtimeService.startProcessInstanceByKey(
            "authorization-test-script-task",
            UUID.randomUUID().toString()
        )
    }

    @Test
    fun `should be able to execute process bean in bpmn execution listener without authorizing`() {
        runtimeService.startProcessInstanceByKey(
            "authorization-test-execution-listener",
            UUID.randomUUID().toString()
        )
    }

    @Test
    fun `should be able to execute process bean in dmn using juel without authorizing`() {
        val evaluateResult = decisionService
            .evaluateDecisionTableByKey("authorization-test-juel")
            .evaluate()

        assertEquals("High", evaluateResult.singleResult.getSingleEntry())
    }
}