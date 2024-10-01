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

package com.ritense.case_.widget.table

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.treeToValue
import com.jayway.jsonpath.Configuration
import com.jayway.jsonpath.JsonPath
import com.jayway.jsonpath.Option
import com.ritense.case_.domain.tab.CaseWidgetTab
import com.ritense.case_.widget.CaseWidgetDataProvider
import com.ritense.case_.widget.exception.InvalidCollectionException
import com.ritense.case_.widget.exception.InvalidCollectionNodeTypeException
import com.ritense.valueresolver.ValueResolverService
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import java.util.UUID

class TableCaseWidgetDataProvider(
    private val objectMapper: ObjectMapper,
    private val valueResolverService: ValueResolverService
) : CaseWidgetDataProvider<TableCaseWidget> {

    override fun supportedWidgetType() = TableCaseWidget::class.java

    override fun getData(documentId: UUID, widgetTab: CaseWidgetTab, widget: TableCaseWidget, pageable: Pageable): Page<Map<String, Any?>> {
        val resolvedCollection =
            valueResolverService.resolveValues(documentId.toString(), listOf(widget.properties.collection))[widget.properties.collection]
        val collectionNode = objectMapper.valueToTree<JsonNode>(resolvedCollection)

        if(collectionNode.isNull) {
            return PageImpl(emptyList(), pageable, 0)
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

        return PageImpl(result, pageable, collectionNode.size().toLong())
    }


    private companion object {
        val JSONPATH_CONTEXT = JsonPath.using(Configuration.defaultConfiguration().addOptions(Option.SUPPRESS_EXCEPTIONS))
    }
}

