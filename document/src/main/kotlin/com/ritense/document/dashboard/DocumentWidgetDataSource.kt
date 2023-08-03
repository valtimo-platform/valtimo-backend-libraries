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
import com.ritense.document.domain.search.AdvancedSearchRequest.OtherFilter
import com.ritense.document.repository.impl.JsonSchemaDocumentRepository
import com.ritense.document.repository.impl.specification.JsonSchemaDocumentSpecificationHelper.Companion.byDocumentDefinitionIdName
import com.ritense.document.service.impl.JsonSchemaDocumentSearchService
import com.ritense.valtimo.contract.dashboard.WidgetDataSource
import com.ritense.valtimo.contract.database.QueryDialectHelper
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.Expression
import javax.persistence.criteria.Path
import javax.persistence.criteria.Predicate
import javax.persistence.criteria.Root

class DocumentWidgetDataSource(
    private val documentRepository: JsonSchemaDocumentRepository,
    private val queryDialectHelper: QueryDialectHelper
) {

    @WidgetDataSource("case-count", "Case count")
    fun getCaseCount(caseCountDataSourceProperties: DocumentCountDataSourceProperties): DocumentCountDataResult {
        val spec = byDocumentDefinitionIdName(caseCountDataSourceProperties.documentDefinition)
//            .and { root, _, criteriaBuilder ->
//                criteriaBuilder.and(
//                    *caseCountDataSourceProperties.queryConditions.map {
//                        createConditionPredicate(root, it, criteriaBuilder)
//                    }.toTypedArray()
//                )
//            }

        val count = documentRepository.count(spec)
        return DocumentCountDataResult(count)
    }

    private fun <T: Comparable<T> >createConditionPredicate(
        root: Root<JsonSchemaDocument>,
        it: QueryCondition<T>,
        criteriaBuilder: CriteriaBuilder
    ): Predicate {
        //TODO: handle doc: and case: prefixes
        val path = createDatabaseObjectPath("content.content", root)
        return it.queryOperator.toPredicate(
            criteriaBuilder,
            queryDialectHelper.getJsonValueExpression(
                criteriaBuilder, path, it.queryPath,
                it.queryValue::class.java as Class<T>
            )
            /** TODO: I don't know about this type */
            ,
            it.queryValue
        )
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
//
//    private fun <T : Comparable<T>?> getValueExpressionForDocPrefix(
//        cb: CriteriaBuilder,
//        documentRoot: Root<JsonSchemaDocument>,
//        path: String
//    ): Expression<T>? {
//        val jsonPath = "$." + path.substring(JsonSchemaDocumentSearchService.DOC_PREFIX.length)
//        return queryDialectHelper.getJsonValueExpression(
//            cb,
//            documentRoot.get<Any>(JsonSchemaDocumentSearchService.CONTENT)
//                .get<Any>(JsonSchemaDocumentSearchService.CONTENT),
//            jsonPath,
//            searchCriteria.getDataType()
//        )
//    }

//    private fun <T : Comparable<T>?> getValueExpressionForCasePrefix(
//        documentRoot: Root<JsonSchemaDocument>,
//        searchCriteria: OtherFilter
//    ): Expression<T>? {
//        val documentColumnName = searchCriteria.path.substring(JsonSchemaDocumentSearchService.CASE_PREFIX.length)
//        return documentRoot.get<Any>(documentColumnName).`as`(searchCriteria.getDataType())
//    }
}