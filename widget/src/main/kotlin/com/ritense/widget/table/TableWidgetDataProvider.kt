/*
 * Copyright 2015-2025 Ritense BV, the Netherlands.
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

package com.ritense.widget.table

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.treeToValue
import com.jayway.jsonpath.Configuration
import com.jayway.jsonpath.JsonPath
import com.jayway.jsonpath.Option
import com.ritense.valueresolver.ValueResolverPropertyKey.Companion.PAGEABLE
import com.ritense.valueresolver.ValueResolverService
import com.ritense.widget.PageWithData
import com.ritense.widget.WidgetDataProvider
import com.ritense.widget.domain.RedirectWidgetAction
import com.ritense.widget.exception.InvalidCollectionException
import com.ritense.widget.exception.InvalidCollectionNodeTypeException
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable

class TableWidgetDataProvider(
    private val objectMapper: ObjectMapper,
    private val valueResolverService: ValueResolverService,
) : WidgetDataProvider<TableWidget> {

    override fun supportedWidgetType() = TableWidget::class.java

    override fun getData(widget: TableWidget, properties: Map<String, Any>): PageWithData<Map<String, Any?>> {
        val pageable = properties[PAGEABLE] as Pageable? ?: Pageable.ofSize(5)

        val resolvedValues = valueResolverService.resolveValues(
            properties,
            widget.getUnresolvedActionValues() + widget.properties.collection
        )

        val redirectPathData = widget.getWidgetActions<RedirectWidgetAction>()
            .associate { action -> action.redirectPath to action.getResolvedRedirectPath(resolvedValues) }

        val collectionNode = objectMapper.valueToTree<JsonNode>(resolvedValues[widget.properties.collection])

        if (collectionNode.isNull) {
            return PageWithData(PageImpl(emptyList(), pageable, 0), redirectPathData)
        }

        if (!collectionNode.isArray) {
            throw InvalidCollectionException()
        }

        val pagedCollection = collectionNode.chunked(
            pageable.pageSize
        )

        val result = pagedCollection.getOrElse(pageable.pageNumber, defaultValue = { _ -> listOf() })
            .onEachIndexed { index, node ->
                if (!node.isContainerNode) {
                    throw InvalidCollectionNodeTypeException(index)
                }
            }.map { child ->
                widget.properties.columns.associate { column ->
                    val value = if (column.value.startsWith("$")) {
                        JSONPATH_CONTEXT.parse(child.toString()).read<Any>(column.value)
                    } else {
                        val pointer = if (column.value.startsWith("/")) column.value else "/${column.value}"
                        val valueNode = child.at(pointer)

                        if (valueNode.isValueNode && !valueNode.isNull) {
                            objectMapper.treeToValue<Any?>(valueNode)
                        } else {
                            null
                        }
                    }

                    column.key to value
                }
            }

        return PageWithData(PageImpl(result, pageable, collectionNode.size().toLong()), redirectPathData)
    }

    private companion object {
        val JSONPATH_CONTEXT =
            JsonPath.using(Configuration.defaultConfiguration().addOptions(Option.SUPPRESS_EXCEPTIONS))
    }
}

