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
import com.ritense.document.repository.impl.JsonSchemaDocumentRepository
import com.ritense.document.repository.impl.specification.JsonSchemaDocumentSpecificationHelper.Companion.byDocumentDefinitionIdName
import com.ritense.valtimo.contract.dashboard.WidgetDataSource
import com.ritense.valtimo.contract.database.QueryDialectHelper
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.Predicate
import javax.persistence.criteria.Root

class DocumentWidgetDataSource(
    private val documentRepository: JsonSchemaDocumentRepository,
    private val queryDialectHelper: QueryDialectHelper
) {

    @WidgetDataSource("case-count", "Case count")
    fun getCaseCount(caseCountDataSourceProperties: DocumentCountDataSourceProperties): DocumentCountDataResult {
        val spec = byDocumentDefinitionIdName(caseCountDataSourceProperties.documentDefinition)
            .and { root, _, criteriaBuilder ->
                criteriaBuilder.and(
                    *caseCountDataSourceProperties.queryConditions.map {
                        createConditionPredicate(root, it, criteriaBuilder)
                    }.toTypedArray()
                )
            }

        val count = documentRepository.count(spec)
        return DocumentCountDataResult(count)
    }

    private fun <T: Comparable<T> >createConditionPredicate(
        root: Root<JsonSchemaDocument>,
        it: QueryCondition<T>,
        criteriaBuilder: CriteriaBuilder
    ): Predicate {
        val valueClass = it.queryValue::class.java as Class<T>
        //Prefix defaults to doc: when no prefix is given
        val pathPrefix = "${it.queryPath.substringBefore(":", "doc")}:"
        val expression =  when (pathPrefix){
            CASE_PREFIX -> {
                root.get<Any>(it.queryPath.substringAfter(CASE_PREFIX)).`as`(valueClass)
            }
            else -> {
                queryDialectHelper.getJsonValueExpression(
                    criteriaBuilder,
                    root.get<Any>("content").get<Any>("content"),
                    "$." + it.queryPath.substringAfter(DOC_PREFIX),
                    valueClass
                )
            }
        }

        return it.queryOperator.toPredicate(
            criteriaBuilder,
            expression,
            it.queryValue
        )
    }

    companion object {
        private const val DOC_PREFIX =  "doc:"
        private const val CASE_PREFIX =  "case:"
    }
}