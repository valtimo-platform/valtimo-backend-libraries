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

import com.ritense.authorization.AuthorizationService
import com.ritense.authorization.request.EntityAuthorizationRequest
import com.ritense.valtimo.BaseIntegrationTest
import com.ritense.valtimo.camunda.domain.CamundaTask
import com.ritense.valtimo.camunda.repository.CamundaTaskSpecificationHelper.Companion.byProcessInstanceId
import com.ritense.valtimo.service.CamundaTaskService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Transactional
class CamundaIdentityLinkSpecificationIntTest @Autowired constructor(
    private val taskService: CamundaTaskService,
    private val authorizationService: AuthorizationService
) : BaseIntegrationTest() {

    @Test
    @WithMockUser(authorities = ["IDENTITY_LINK_ROLE"])
    fun `should have view_list access to task with groupId`() {
        val instance = runtimeService.startProcessInstanceByKey(
            "identity-link-mapper-test-process",
            UUID.randomUUID().toString()
        )

        val taskCount = taskService.countTasks(byProcessInstanceId(instance.processInstanceId))

        assertThat(taskCount).isEqualTo(1)
    }

    @Test
    @WithMockUser(authorities = ["SOME_OTHER_ROLE"])
    fun `should not have view_list access to task with groupId`() {
        val instance = runtimeService.startProcessInstanceByKey(
            "identity-link-mapper-test-process",
            UUID.randomUUID().toString()
        )

        val taskCount = taskService.countTasks(byProcessInstanceId(instance.processInstanceId))

        assertThat(taskCount).isEqualTo(0)
    }

    @Test
    @WithMockUser(authorities = ["IDENTITY_LINK_ROLE"])
    fun `should have view access to task with groupId`() {
        val instance = runtimeService.startProcessInstanceByKey(
            "identity-link-mapper-test-process",
            UUID.randomUUID().toString()
        )
        val task = taskService.findTask(byProcessInstanceId(instance.processInstanceId))
        authorizationService.requirePermission(
            EntityAuthorizationRequest(
                CamundaTask::class.java,
                CamundaTaskActionProvider.VIEW,
                task
            )
        )
    }

    @Test
    @WithMockUser(authorities = ["SOME_OTHER_ROLE"])
    fun `should not have view access to task with groupId`() {
        val instance = runtimeService.startProcessInstanceByKey(
            "identity-link-mapper-test-process",
            UUID.randomUUID().toString()
        )
        assertThrows(AccessDeniedException::class.java) {
            val task = taskService.findTask(byProcessInstanceId(instance.processInstanceId))
            authorizationService.requirePermission(
                EntityAuthorizationRequest(
                    CamundaTask::class.java,
                    CamundaTaskActionProvider.VIEW,
                    task
                )
            )
        }
    }
}