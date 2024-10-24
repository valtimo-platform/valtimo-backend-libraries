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

package com.ritense.case.domain.casedefinition

import com.ritense.authorization.AuthorizationContext.Companion.runWithoutAuthorization
import com.ritense.authorization.AuthorizationEntityMapper
import com.ritense.authorization.AuthorizationEntityMapperResult
import com.ritense.case.domain.CaseTab
import com.ritense.case.repository.CaseDefinitionRepository
import jakarta.persistence.criteria.AbstractQuery
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.Root
import java.util.UUID

class CaseTabCaseDefinitionMapper(
    private val caseDefinitionRepository: CaseDefinitionRepository
) : AuthorizationEntityMapper<CaseTab, CaseDefinition> {

    override fun mapRelated(entity: CaseTab): List<CaseDefinition> {
        return runWithoutAuthorization {
            listOf(caseDefinitionRepository.getReferenceById(entity.caseDefinitionId!!))
        }
    }

    override fun mapQuery(
        root: Root<CaseTab>,
        query: AbstractQuery<*>,
        criteriaBuilder: CriteriaBuilder
    ): AuthorizationEntityMapperResult<CaseDefinition> {
        val definitionRoot: Root<CaseDefinition> = query.from(CaseDefinition::class.java)
        return AuthorizationEntityMapperResult(
            root = definitionRoot,
            query = query,
            joinPredicate = criteriaBuilder.equal(
                root.get<UUID>("caseDefinitionId"),
                definitionRoot.get<UUID>("id")
            )
        )
    }

    override fun supports(fromClass: Class<*>, toClass: Class<*>): Boolean {
        return fromClass == CaseTab::class.java && toClass == CaseDefinition::class.java
    }

}