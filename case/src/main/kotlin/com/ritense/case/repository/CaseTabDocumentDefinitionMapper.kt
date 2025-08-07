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

package com.ritense.case.repository

import com.ritense.authorization.AuthorizationContext.Companion.runWithoutAuthorization
import com.ritense.authorization.AuthorizationEntityMapper
import com.ritense.authorization.AuthorizationEntityMapperResult
import com.ritense.case.domain.CaseTab
import com.ritense.case.domain.CaseTabId
import com.ritense.document.domain.impl.JsonSchemaDocumentDefinition
import com.ritense.document.domain.impl.JsonSchemaDocumentDefinitionId
import com.ritense.document.service.DocumentDefinitionService
import com.ritense.valtimo.contract.case_.CaseDefinitionId
import jakarta.persistence.EntityNotFoundException
import jakarta.persistence.criteria.AbstractQuery
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.Root
import kotlin.jvm.optionals.getOrNull

class CaseTabDocumentDefinitionMapper(
    private val documentDefinitionService: DocumentDefinitionService
) : AuthorizationEntityMapper<CaseTab, JsonSchemaDocumentDefinition> {
    override fun mapRelated(entity: CaseTab): List<JsonSchemaDocumentDefinition> {
        return runWithoutAuthorization {
            listOf(
                documentDefinitionService.findByCaseDefinitionId(entity.id.caseDefinitionId)
                    .map { it as JsonSchemaDocumentDefinition }
                    .getOrNull()
                    ?: throw EntityNotFoundException("JsonSchemaDocumentDefinition with name ${entity.id.caseDefinitionId.key} and version tag ${entity.id.caseDefinitionId.versionTag} not found")
            )
        }
    }

    override fun mapQuery(
        root: Root<CaseTab>,
        query: AbstractQuery<*>,
        criteriaBuilder: CriteriaBuilder
    ): AuthorizationEntityMapperResult<JsonSchemaDocumentDefinition> {
        val subquery = query.subquery(Int::class.java)
        val subRoot = subquery.from(JsonSchemaDocumentDefinition::class.java)

        subquery.select(criteriaBuilder.literal(1))
            .where(
                criteriaBuilder.equal(
                    root.get<CaseTabId>("id").get<String>("caseDefinitionId"),
                    subRoot.get<JsonSchemaDocumentDefinitionId>("id")
                        .get<CaseDefinitionId>("caseDefinitionId")
                )
            )

        return AuthorizationEntityMapperResult(
            subRoot,
            subquery,
            criteriaBuilder.exists(subquery)
        )
    }

    override fun supports(fromClass: Class<*>, toClass: Class<*>): Boolean {
        return fromClass == CaseTab::class.java && toClass == JsonSchemaDocumentDefinition::class.java
    }
}