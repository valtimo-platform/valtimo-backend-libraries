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
import com.ritense.valtimo.camunda.repository.CamundaIdentityLinkSpecificationHelper.Companion.ID
import com.ritense.valtimo.camunda.repository.CamundaIdentityLinkSpecificationHelper.Companion.TASK
import javax.persistence.criteria.AbstractQuery
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.Root

class CamundaTaskIdentityLinkMapper : AuthorizationEntityMapper<CamundaTask, CamundaIdentityLink> {
    override fun mapRelated(entity: CamundaTask): List<CamundaIdentityLink> {
        return entity.identityLinks
    }

    override fun mapQuery(
        root: Root<CamundaTask>,
        query: AbstractQuery<*>,
        criteriaBuilder: CriteriaBuilder
    ): AuthorizationEntityMapperResult<CamundaIdentityLink> {
        val ilQuery = query.subquery(String::class.java)
        val subRoot = ilQuery.from(CamundaIdentityLink::class.java)
        ilQuery.select(subRoot.get<CamundaTask>(TASK).get(ID))

        return AuthorizationEntityMapperResult(
            subRoot,
            ilQuery,
            criteriaBuilder.`in`(root.get<Any>(ID)).value(ilQuery),
        )
    }

    override fun supports(fromClass: Class<*>, toClass: Class<*>): Boolean {
        return fromClass == CamundaTask::class.java && toClass == CamundaIdentityLink::class.java
    }
}