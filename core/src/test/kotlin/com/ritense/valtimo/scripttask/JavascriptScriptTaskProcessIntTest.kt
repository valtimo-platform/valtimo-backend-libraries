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

package com.ritense.valtimo.scripttask

import com.ritense.authorization.AuthorizationContext.Companion.runWithoutAuthorization
import com.ritense.valtimo.BaseIntegrationTest
import com.ritense.valtimo.service.CamundaProcessService
import org.camunda.bpm.engine.HistoryService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional
import java.util.UUID
import kotlin.test.assertEquals

@Transactional
class JavascriptScriptTaskProcessIntTest : BaseIntegrationTest() {

    @Autowired
    lateinit var camundaProcessService: CamundaProcessService

    @Autowired
    lateinit var historyService: HistoryService

    @Test
    fun `should execute javascript in script-task`() {
        val processInstance = runWithoutAuthorization {
            camundaProcessService.startProcess(
                "javascript-script-task-process",
                UUID.randomUUID().toString(),
                mapOf("a" to 1, "b" to 2)
            ).processInstanceDto
        }

        val c = historyService.createHistoricVariableInstanceQuery()
            .processInstanceId(processInstance.id)
            .variableName("c")
            .singleResult()
            .value
        assertEquals(3, c)
    }
}