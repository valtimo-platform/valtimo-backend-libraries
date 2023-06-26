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

import com.ritense.valtimo.camunda.domain.CamundaTask
import org.springframework.data.jpa.domain.Specification

object CamundaTaskSpecificationHelper {
    fun byAssignee(assignee: String) = Specification<CamundaTask> { root, _, criteriaBuilder ->
        criteriaBuilder.equal(root.get<Any>("assignee"), assignee)
    }

    fun byUnassigned() = Specification<CamundaTask> { root, _, criteriaBuilder ->
        criteriaBuilder.equal(root.get<Any>("assignee"), null)
    }

    fun byCandidateGroups(candidateGroups: List<String>) = Specification<CamundaTask> { root, _, criteriaBuilder ->
        criteriaBuilder.and(
            criteriaBuilder.equal(root.get<Any>("identityLinks").get<Any>("type"), "candidate"),
            root.get<Any>("identityLinks").get<Any>("groupId").`in`(candidateGroups)
        )
    }

    fun byProcessDefinitionKeys(processDefinitionKeys: Collection<String>) = Specification<CamundaTask> { root, _, _ ->
        root.get<Any>("processDefinition").get<Any>("key").`in`(processDefinitionKeys)
    }
}