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

package com.ritense.valtimo.camunda.repository

import com.ritense.valtimo.camunda.domain.CamundaIdentityLink
import com.ritense.valtimo.camunda.domain.CamundaTask
import org.springframework.data.jpa.domain.Specification
import com.ritense.valtimo.camunda.repository.CamundaTaskSpecificationHelper.Companion.ID as TASK_ID

class CamundaIdentityLinkSpecificationHelper {

    companion object {

        const val ID: String = "id"
        const val REVISION: String = "revision"
        const val GROUP_ID: String = "groupId"
        const val TYPE: String = "type"
        const val USER_ID: String = "userId"
        const val TASK: String = "task"
        const val PROCESS_DEFINITION: String = "processDefinition"
        const val TENANT_ID: String = "tenantId"

        @JvmStatic
        fun byTaskId(taskId: String) = Specification<CamundaIdentityLink> { root, _, cb ->
            cb.equal(root.get<CamundaTask>(TASK).get<String>(TASK_ID), taskId)
        }

        @JvmStatic
        fun byType(type: String) = Specification<CamundaIdentityLink> { root, _, cb ->
            cb.equal(root.get<String>(TYPE), type)
        }

    }
}