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

package com.ritense.valtimo.operaton.authorization

import com.ritense.authorization.AuthorizationEntityMapper
import com.ritense.authorization.AuthorizationEntityMapperResult
import com.ritense.valtimo.operaton.domain.OperatonExecution
import com.ritense.valtimo.operaton.domain.OperatonProcessDefinition
import jakarta.persistence.criteria.AbstractQuery
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.Root

class OperatonExecutionProcessDefinitionMapper : AuthorizationEntityMapper<OperatonExecution, OperatonProcessDefinition> {
    override fun mapRelated(entity: OperatonExecution): List<OperatonProcessDefinition> {
        return listOf(entity.processDefinition!!)
    }

    override fun mapQuery(root: Root<OperatonExecution>, query: AbstractQuery<*>, criteriaBuilder: CriteriaBuilder): AuthorizationEntityMapperResult<OperatonProcessDefinition> {
        val processDefinitionRoot: Root<OperatonProcessDefinition> = query.from(OperatonProcessDefinition::class.java)
        val groupList = query.groupList.toMutableList()
        groupList.add(root.get<OperatonProcessDefinition>("processDefinition").get<String>("id"))
        query.groupBy(groupList)

        return AuthorizationEntityMapperResult(
            processDefinitionRoot,
            query,
            criteriaBuilder.equal(
                root.get<OperatonProcessDefinition>("processDefinition").get<String>("id"),
                processDefinitionRoot.get<String>("id")
            )
        )
    }

    override fun supports(fromClass: Class<*>, toClass: Class<*>): Boolean {
        return fromClass == OperatonExecution::class.java && toClass == OperatonProcessDefinition::class.java
    }
}