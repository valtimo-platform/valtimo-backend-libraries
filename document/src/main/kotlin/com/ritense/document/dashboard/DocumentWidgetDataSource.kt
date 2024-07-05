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

package com.ritense.document.dashboard

import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.document.domain.impl.JsonSchemaDocument
import com.ritense.document.repository.impl.JsonSchemaDocumentRepository
import com.ritense.document.repository.impl.specification.JsonSchemaDocumentSpecificationHelper.Companion.byDocumentDefinitionIdName
import com.ritense.valtimo.contract.dashboard.WidgetDataSource
import com.ritense.valtimo.contract.database.QueryDialectHelper
import jakarta.persistence.EntityManager
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.Expression
import jakarta.persistence.criteria.Path
import jakarta.persistence.criteria.Predicate
import jakarta.persistence.criteria.Root


class DocumentWidgetDataSource(
    private val documentRepository: JsonSchemaDocumentRepository,
    private val queryDialectHelper: QueryDialectHelper,
    private val objectMapper: ObjectMapper,
    private val entityManager: EntityManager
) {

    @WidgetDataSource("case-count", "Case count")
    fun getCaseCount(caseCountDataSourceProperties: DocumentCountDataSourceProperties): DocumentCountDataResult {
        val byCaseSpec = byDocumentDefinitionIdName(caseCountDataSourceProperties.documentDefinition)
        val spec = byCaseSpec.and { root, _, criteriaBuilder ->
            criteriaBuilder.and(
                *caseCountDataSourceProperties.queryConditions?.map {
                    createConditionPredicate(root, it, criteriaBuilder)
                }?.toTypedArray() ?: arrayOf()
            )
        }

        val count = documentRepository.count(spec)
        val total = documentRepository.count(byCaseSpec)
        return DocumentCountDataResult(count, total)
    }

    @WidgetDataSource("case-counts", "Case counts")
    fun getCaseCounts(caseCountsDataSourceProperties: DocumentCountsDataSourceProperties): DocumentCountsDataResult {
        val items: List<DocumentCountsItem> = caseCountsDataSourceProperties.queryItems.map {
            val spec = byDocumentDefinitionIdName(caseCountsDataSourceProperties.documentDefinition)
                .and { root, _, criteriaBuilder ->
                    criteriaBuilder.and(
                        *it.queryConditions?.map {
                            createConditionPredicate(root, it, criteriaBuilder)
                        }?.toTypedArray() ?: arrayOf()
                    )
                }

            val count = documentRepository.count(spec)


            DocumentCountsItem(it.label, count)
        }

        return DocumentCountsDataResult(items)
    }

    @WidgetDataSource("case-group-by", "Case group by")
    fun getCaseGroupBy(caseGroupByDataSourceProperties: DocumentGroupByDataSourceProperties): DocumentGroupByDataResult {
        val criteriaBuilder: CriteriaBuilder = entityManager.getCriteriaBuilder()
        val query = criteriaBuilder.createQuery(DocumentGroupByItem::class.java)
        val root: Root<JsonSchemaDocument> = query.from(JsonSchemaDocument::class.java)
        val expression = getPathExpression(caseGroupByDataSourceProperties.path, root, criteriaBuilder)
        val conditions = caseGroupByDataSourceProperties.queryConditions?.map {
            createConditionPredicate(root, it, criteriaBuilder)
        }?.toTypedArray() ?: arrayOf()

        query
            .where(*conditions)
            .multiselect(
                expression,
                criteriaBuilder.count(root),
            )
            .groupBy(expression)

        val results = entityManager.createQuery(query).resultList

        return DocumentGroupByDataResult(values = results)
    }

    private fun getPathExpression(path: String, root: Root<JsonSchemaDocument>, criteriaBuilder: CriteriaBuilder): Expression<out Any> {
        val pathPrefix = "${path.substringBefore(":", "doc")}:"
        val valueClass = String::class.java
        val expression = when (pathPrefix) {
            CASE_PREFIX -> {
                var expr = root as Path<*>
                path.substringAfter(CASE_PREFIX).split('.').forEach {
                    expr = expr.get<Any>(it)
                }
                expr
            }

            else -> {
                queryDialectHelper.getJsonValueExpression(
                    criteriaBuilder,
                    root.get<Any>("content").get<Any>("content"),
                    "$." + path.substringAfter(DOC_PREFIX),
                    valueClass
                )
            }
        }

        return expression;
    }

    private fun <T : Comparable<T>> createConditionPredicate(
        root: Root<JsonSchemaDocument>,
        it: QueryCondition<T>,
        criteriaBuilder: CriteriaBuilder
    ): Predicate {
        val valueClass = it.queryValue::class.java as Class<T>
        //Prefix defaults to doc: when no prefix is given
        val pathPrefix = "${it.queryPath.substringBefore(":", "doc")}:"
        val expression = when (pathPrefix) {
            CASE_PREFIX -> {
                var expr = root as Path<*>
                it.queryPath.substringAfter(CASE_PREFIX).split('.').forEach {
                    expr = expr.get<Any>(it)
                }
                expr.`as`(valueClass)
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

        val queryValue = if (it.queryValue == "\${null}") {
            null
        } else {
            it.queryValue
        }

        return it.queryOperator.toPredicate(
            criteriaBuilder,
            expression,
            queryValue
        )
    }

    companion object {
        private const val DOC_PREFIX = "doc:"
        private const val CASE_PREFIX = "case:"
    }
}