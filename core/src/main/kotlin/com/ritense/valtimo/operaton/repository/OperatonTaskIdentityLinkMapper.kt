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

package com.ritense.valtimo.operaton.repository

import com.ritense.authorization.AuthorizationEntityMapper
import com.ritense.authorization.AuthorizationEntityMapperResult
import com.ritense.valtimo.operaton.domain.OperatonIdentityLink
import com.ritense.valtimo.operaton.domain.OperatonTask
import com.ritense.valtimo.operaton.repository.OperatonIdentityLinkSpecificationHelper.Companion.ID
import com.ritense.valtimo.operaton.repository.OperatonIdentityLinkSpecificationHelper.Companion.TASK
import jakarta.persistence.criteria.AbstractQuery
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.Root

class OperatonTaskIdentityLinkMapper : AuthorizationEntityMapper<OperatonTask, OperatonIdentityLink> {
    override fun mapRelated(entity: OperatonTask): List<OperatonIdentityLink> {
        return entity.identityLinks
    }

    override fun mapQuery(
        root: Root<OperatonTask>,
        query: AbstractQuery<*>,
        criteriaBuilder: CriteriaBuilder
    ): AuthorizationEntityMapperResult<OperatonIdentityLink> {
        val subquery = query.subquery(String::class.java)
        val subRoot = subquery.from(OperatonIdentityLink::class.java)
        subquery.select(subRoot.get<OperatonTask>(TASK).get(ID))

        return AuthorizationEntityMapperResult(
            subRoot,
            subquery,
            criteriaBuilder.`in`(root.get<Any>(ID)).value(subquery),
        )
    }

    override fun supports(fromClass: Class<*>, toClass: Class<*>): Boolean {
        return fromClass == OperatonTask::class.java && toClass == OperatonIdentityLink::class.java
    }
}