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

package com.ritense.processdocument.operaton.authorization

import com.ritense.authorization.AuthorizationEntityMapper
import com.ritense.authorization.AuthorizationEntityMapperResult
import com.ritense.authorization.utils.QueryUtils
import com.ritense.document.domain.impl.JsonSchemaDocument
import com.ritense.document.domain.impl.JsonSchemaDocumentId
import com.ritense.document.repository.impl.JsonSchemaDocumentRepository
import com.ritense.processdocument.domain.impl.OperatonProcessInstanceId
import com.ritense.processdocument.repository.ProcessDocumentInstanceRepository
import com.ritense.valtimo.operaton.domain.OperatonExecution
import com.ritense.valtimo.operaton.domain.OperatonTask
import com.ritense.valtimo.operaton.repository.OperatonHistoricProcessInstanceSpecificationHelper.Companion.BUSINESS_KEY
import com.ritense.valtimo.operaton.repository.OperatonTaskSpecificationHelper.Companion.ID
import com.ritense.valtimo.operaton.repository.OperatonTaskSpecificationHelper.Companion.PROCESS_INSTANCE
import com.ritense.valtimo.contract.database.QueryDialectHelper
import jakarta.persistence.criteria.AbstractQuery
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.Root

class OperatonTaskDocumentMapper(
    private val processDocumentInstanceRepository: ProcessDocumentInstanceRepository,
    private val documentRepository: JsonSchemaDocumentRepository,
    private val queryDialectHelper: QueryDialectHelper
) : AuthorizationEntityMapper<OperatonTask, JsonSchemaDocument> {

    override fun mapRelated(entity: OperatonTask): List<JsonSchemaDocument> {
        val processInstanceId = OperatonProcessInstanceId(entity.getProcessInstanceId())
        val processDocumentInstance = processDocumentInstanceRepository.findByProcessInstanceId(processInstanceId).get()
        val document = documentRepository.findById(processDocumentInstance.processDocumentInstanceId().documentId()).get()
        return listOf(document)
    }

    override fun mapQuery(
        root: Root<OperatonTask>,
        query: AbstractQuery<*>,
        criteriaBuilder: CriteriaBuilder
    ): AuthorizationEntityMapperResult<JsonSchemaDocument> {
        val documentRoot = query.from(JsonSchemaDocument::class.java)
        val processBusinessKey = root.get<OperatonExecution>(PROCESS_INSTANCE).get<String>(BUSINESS_KEY)
        if (!QueryUtils.isCountQuery(query)) {
            query.groupBy(query.groupList + root.get<String>(ID))
        }
        val documentId = queryDialectHelper.uuidToString(
            criteriaBuilder,
            documentRoot.get<JsonSchemaDocumentId>(ID).get(ID)
        )

        return AuthorizationEntityMapperResult(
            documentRoot,
            query,
            criteriaBuilder.equal(processBusinessKey, documentId)
        )
    }

    override fun supports(fromClass: Class<*>, toClass: Class<*>): Boolean {
        return fromClass == OperatonTask::class.java && toClass == JsonSchemaDocument::class.java
    }
}