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

import com.ritense.document.domain.impl.JsonSchemaDocument
import com.ritense.document.repository.impl.JsonSchemaDocumentRepository
import com.ritense.document.repository.impl.specification.JsonSchemaDocumentSpecificationHelper.Companion.byDocumentDefinitionIdName
import com.ritense.valtimo.contract.dashboard.QueryCondition
import com.ritense.valtimo.contract.dashboard.WidgetDataSource
import com.ritense.valtimo.contract.database.QueryDialectHelper
import com.ritense.valtimo.contract.repository.ExpressionOperator
import jakarta.persistence.EntityManager
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.Expression
import jakarta.persistence.criteria.Path
import jakarta.persistence.criteria.Root

class DocumentWidgetDataSource(
    private val documentRepository: JsonSchemaDocumentRepository,
    private val queryDialectHelper: QueryDialectHelper,
    private val entityManager: EntityManager
) {

    @WidgetDataSource("case-count", "Case count")
    fun getCaseCount(caseCountDataSourceProperties: DocumentCountDataSourceProperties): DocumentCountDataResult {
        val byCaseSpec = byDocumentDefinitionIdName(caseCountDataSourceProperties.documentDefinition)
        val spec = byCaseSpec.and { root, _, criteriaBuilder ->
            criteriaBuilder.and(
                *caseCountDataSourceProperties.queryConditions?.map {
                    it.toPredicate(root, criteriaBuilder, this::getPathExpression)
                }?.toTypedArray() ?: arrayOf()
            )
        }

        val count = documentRepository.count(spec)
        val total = documentRepository.count(byCaseSpec)
        return DocumentCountDataResult(count, total)
    }

    @WidgetDataSource("case-counts", "Case counts")
    fun getCaseCounts(caseCountsDataSourceProperties: DocumentCountsDataSourceProperties): DocumentCountsDataResult {
        val items: List<DocumentCountsItem> = caseCountsDataSourceProperties.queryItems.map { queryItem ->
            val spec = byDocumentDefinitionIdName(caseCountsDataSourceProperties.documentDefinition)
                .and { root, _, criteriaBuilder ->
                    criteriaBuilder.and(
                        *queryItem.queryConditions.map {
                            it.toPredicate(root, criteriaBuilder, this::getPathExpression)
                        }.toTypedArray()
                    )
                }

            val count = documentRepository.count(spec)


            DocumentCountsItem(queryItem.label, count)
        }

        return DocumentCountsDataResult(items)
    }

    @WidgetDataSource("case-group-by", "Case group by")
    fun getCaseGroupBy(caseGroupByDataSourceProperties: DocumentGroupByDataSourceProperties): DocumentGroupByDataResult {
        val criteriaBuilder: CriteriaBuilder = entityManager.criteriaBuilder
        val query = criteriaBuilder.createQuery(DocumentGroupByItem::class.java)
        val root: Root<JsonSchemaDocument> = query.from(JsonSchemaDocument::class.java)
        val docPredicate = criteriaBuilder.equal(
            root.get<Any>("documentDefinitionId").get<String>("name"),
            caseGroupByDataSourceProperties.documentDefinition
        )
        val pathIsNotNullPredicate = QueryCondition(
            caseGroupByDataSourceProperties.path,
            ExpressionOperator.NOT_EQUAL_TO,
            "\${null}"
        ).toPredicate(
            root, criteriaBuilder, this::getPathExpression
        )
        // todo: fix null values through getJsonValueExpression
        val pathIsNotNullStringPredicate =
            QueryCondition(caseGroupByDataSourceProperties.path, ExpressionOperator.NOT_EQUAL_TO, "null").toPredicate(
                root, criteriaBuilder, this::getPathExpression
            )
        val conditionPredicates = caseGroupByDataSourceProperties.queryConditions?.map {
            it.toPredicate(root, criteriaBuilder, this::getPathExpression)
        }?.toTypedArray() ?: arrayOf()
        val combinedPredicates =
            arrayOf(docPredicate, pathIsNotNullPredicate, pathIsNotNullStringPredicate, *conditionPredicates)
        val groupByExpression =
            getPathExpression(String::class.java, caseGroupByDataSourceProperties.path, root, criteriaBuilder)

        query
            .where(*combinedPredicates)
            .multiselect(
                groupByExpression,
                criteriaBuilder.count(root),
            )
            .groupBy(groupByExpression)

        val resultList = entityManager.createQuery(query).resultList
        val result: List<DocumentGroupByItem>;

        if (caseGroupByDataSourceProperties.enum.isNullOrEmpty()) {
            result = resultList
        } else {
            result = resultList.map {
                val enumValue: String? = caseGroupByDataSourceProperties.enum[it.label]
                if (enumValue.isNullOrEmpty()) {
                    it
                } else {
                    it.label = enumValue
                    it
                }
            }
        }

        return DocumentGroupByDataResult(values = result)
    }

    private fun <T> getPathExpression(
        valueClass: Class<T>,
        path: String,
        root: Root<*>,
        criteriaBuilder: CriteriaBuilder
    ): Expression<T> {
        // Prefix defaults to doc: when no prefix is given
        val pathPrefix = "${path.substringBefore(":", "doc")}:"
        val expression = when (pathPrefix) {
            CASE_PREFIX -> {
                var expr = root as Path<*>
                path.substringAfter(CASE_PREFIX).split('.').forEach {
                    expr = expr.get<Any>(it)
                }
                expr.`as`(valueClass)
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

    companion object {
        private const val DOC_PREFIX = "doc:"
        private const val CASE_PREFIX = "case:"
    }
}