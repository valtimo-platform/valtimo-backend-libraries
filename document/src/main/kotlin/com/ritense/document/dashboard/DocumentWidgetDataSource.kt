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

package com.ritense.document.dashboard

import com.ritense.document.domain.impl.JsonSchemaDocument
import com.ritense.document.repository.DocumentRepository
import com.ritense.document.repository.impl.JsonSchemaDocumentRepository
import com.ritense.document.repository.impl.specification.JsonSchemaDocumentSpecificationHelper
import com.ritense.document.repository.impl.specification.JsonSchemaDocumentSpecificationHelper.Companion.byDocumentDefinitionIdName
import com.ritense.valtimo.contract.dashboard.WidgetDataSource
import com.ritense.valtimo.contract.dashboard.dto.DashboardWidgetSingleDto
import com.ritense.valtimo.contract.database.QueryDialectHelper
import javax.persistence.criteria.Path
import javax.persistence.criteria.Root

class DocumentWidgetDataSource(
    private val documentRepository: JsonSchemaDocumentRepository,
    private val queryDialectHelper: QueryDialectHelper
) {

    @WidgetDataSource("case-count", "Case count")
    fun getCaseCount(caseCountDataSourceProperties: DocumentCountDataSourceProperties): DashboardWidgetSingleDto {
        val spec = byDocumentDefinitionIdName(caseCountDataSourceProperties.documentDefinition)
        spec.and { root, _, criteriaBuilder ->
            criteriaBuilder.and(
                *caseCountDataSourceProperties.queryConditions.map {
                    val path = root.get<String>("content.content")
                    it.queryOperator.toPredicate(
                        criteriaBuilder,
                        queryDialectHelper.getJsonValueExpression(criteriaBuilder, path, it.queryPath,
                            String::class.java) /** TODO: I don't know about this type */,
                        it.queryValue
                    )
                }.toTypedArray()
            )
        }

        val count = documentRepository.count(spec)
        return DashboardWidgetSingleDto(count, count)
    }

    private fun <T> createDatabaseObjectPath(field: String, root: Root<T>): Path<Any>? {
        var path: Path<Any>? = null
        field.split('.').forEach {
            path = if (path == null) {
                root.get(it)
            } else {
                path!!.get(it)
            }
        }

        return path
    }
}