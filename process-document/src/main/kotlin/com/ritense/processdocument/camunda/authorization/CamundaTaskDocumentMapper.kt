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

package com.ritense.processdocument.camunda.authorization

import com.ritense.authorization.AuthorizationEntityMapper
import com.ritense.authorization.AuthorizationEntityMapperResult
import com.ritense.document.domain.impl.JsonSchemaDocument
import com.ritense.document.domain.impl.JsonSchemaDocumentId
import com.ritense.valtimo.camunda.domain.CamundaExecution
import com.ritense.valtimo.camunda.domain.CamundaTask
import com.ritense.valtimo.camunda.repository.CamundaHistoricProcessInstanceSpecificationHelper.Companion.BUSINESS_KEY
import com.ritense.valtimo.camunda.repository.CamundaHistoricProcessInstanceSpecificationHelper.Companion.ROOT_PROCESS_INSTANCE_ID
import java.util.UUID
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Root

class CamundaTaskDocumentMapper : AuthorizationEntityMapper<CamundaTask, JsonSchemaDocument> {

    override fun mapRelated(entity: CamundaTask): List<JsonSchemaDocument> {
        TODO("Not yet implemented")
    }

    override fun mapQuery(
        root: Root<CamundaTask>,
        query: CriteriaQuery<*>,
        criteriaBuilder: CriteriaBuilder
    ): AuthorizationEntityMapperResult<JsonSchemaDocument> {
        val documentRoot = query.from(JsonSchemaDocument::class.java)
        val processBusinessKey = root.get<CamundaExecution>(ROOT_PROCESS_INSTANCE_ID).get<String>(BUSINESS_KEY)
        val groupList = query.groupList.toMutableList() + processBusinessKey
        query.groupBy(groupList)

        return AuthorizationEntityMapperResult(
            documentRoot,
            query,
            criteriaBuilder.equal(processBusinessKey, documentRoot.get<JsonSchemaDocumentId>("id").get<UUID>("id"))
        )
    }

    override fun supports(fromClass: Class<*>, toClass: Class<*>): Boolean {
        return fromClass == CamundaTask::class.java && toClass == JsonSchemaDocument::class.java
    }
}