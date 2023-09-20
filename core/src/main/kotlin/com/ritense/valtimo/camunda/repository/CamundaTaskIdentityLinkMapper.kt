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

import com.ritense.authorization.AuthorizationEntityMapper
import com.ritense.authorization.AuthorizationEntityMapperResult
import com.ritense.valtimo.camunda.domain.CamundaIdentityLink
import com.ritense.valtimo.camunda.domain.CamundaTask
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Root

class CamundaTaskIdentityLinkMapper() : AuthorizationEntityMapper<CamundaIdentityLink, CamundaTask> {
    override fun mapRelated(entity: CamundaIdentityLink): List<CamundaTask> {
        return entity.task?.let { listOf(it) } ?: listOf()
    }

    override fun mapQuery(
        root: Root<CamundaIdentityLink>,
        query: CriteriaQuery<*>,
        criteriaBuilder: CriteriaBuilder
    ): AuthorizationEntityMapperResult<CamundaTask> {
        val taskRoot: Root<CamundaTask> = query.from(CamundaTask::class.java)

        val groupList = query.groupList.toMutableList()
        groupList.add(root.get<CamundaTask>("task"))
        query.groupBy(groupList)

        return AuthorizationEntityMapperResult(
            taskRoot,
            query,
            criteriaBuilder.equal(root.get<CamundaTask>("task").get<String>("id"), taskRoot.get<String>("id"))
        )
    }

    override fun supports(fromClass: Class<*>, toClass: Class<*>): Boolean {
        return fromClass == CamundaIdentityLink::class.java && toClass == CamundaTask::class.java
    }
}