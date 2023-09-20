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
import com.ritense.valtimo.camunda.repository.CamundaTaskSpecificationHelper.Companion.all
import com.ritense.valtimo.camunda.repository.CamundaTaskSpecificationHelper.Companion.byProcessInstanceId
import com.ritense.valtimo.service.CamundaTaskService
import java.util.UUID
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.transaction.annotation.Transactional

class CamundaIdentityLinkMapperIntTest @Autowired constructor(
    private val taskService: CamundaTaskService
): BaseIntegrationTest() {

    @Test
    @WithMockUser(authorities = ["IDENTITY_LINK_ROLE"])
    fun `should have access to task with groupId`() {
        val instance = runtimeService.startProcessInstanceByKey(
            "identity-link-mapper-test-process",
            UUID.randomUUID().toString()
        )

        val tasks = taskService.findTasks(byProcessInstanceId(instance.processInstanceId))

        assertThat(tasks).hasSize(1)
    }

    @Test
    @WithMockUser(authorities = ["SOME_OTHER_ROLE"])
    fun `should not have access to task with groupId`() {
        val instance = runtimeService.startProcessInstanceByKey(
            "identity-link-mapper-test-process",
            UUID.randomUUID().toString()
        )

        val tasks = taskService.findTasks(byProcessInstanceId(instance.processInstanceId))

        assertThat(tasks).hasSize(0)
    }
}