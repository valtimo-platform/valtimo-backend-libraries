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

import com.ritense.authorization.AuthorizationEntityMapper
import com.ritense.authorization.AuthorizationEntityMapperResult
import com.ritense.valtimo.camunda.domain.CamundaExecution
import com.ritense.valtimo.camunda.domain.CamundaProcessDefinition
import jakarta.persistence.criteria.AbstractQuery
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.Root

class CamundaExecutionProcessDefinitionMapper : AuthorizationEntityMapper<CamundaExecution, CamundaProcessDefinition> {
    override fun mapRelated(entity: CamundaExecution): List<CamundaProcessDefinition> {
        return listOf(entity.processDefinition!!)
    }

    override fun mapQuery(root: Root<CamundaExecution>, query: AbstractQuery<*>, criteriaBuilder: CriteriaBuilder): AuthorizationEntityMapperResult<CamundaProcessDefinition> {
        val processDefinitionRoot: Root<CamundaProcessDefinition> = query.from(CamundaProcessDefinition::class.java)
        val groupList = query.groupList.toMutableList()
        groupList.add(root.get<CamundaProcessDefinition>("processDefinition").get<String>("id"))
        query.groupBy(groupList)

        return AuthorizationEntityMapperResult(
            processDefinitionRoot,
            query,
            criteriaBuilder.equal(
                root.get<CamundaProcessDefinition>("processDefinition").get<String>("id"),
                processDefinitionRoot.get<String>("id")
            )
        )
    }

    override fun supports(fromClass: Class<*>, toClass: Class<*>): Boolean {
        return fromClass == CamundaExecution::class && toClass == CamundaProcessDefinition::class
    }
}