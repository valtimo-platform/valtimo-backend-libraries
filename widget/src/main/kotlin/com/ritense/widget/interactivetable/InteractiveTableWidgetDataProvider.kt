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

package com.ritense.widget.interactivetable

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.treeToValue
import com.jayway.jsonpath.Configuration
import com.jayway.jsonpath.JsonPath
import com.jayway.jsonpath.Option
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import com.ritense.valueresolver.ValueResolverPropertyKey.Companion.PAGEABLE
import com.ritense.valueresolver.ValueResolverService
import com.ritense.widget.WidgetDataProvider
import com.ritense.widget.exception.InvalidCollectionException
import com.ritense.widget.exception.InvalidCollectionNodeTypeException
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component

@Component
@SkipComponentScan
class InteractiveTableWidgetDataProvider(
    private val objectMapper: ObjectMapper,
    private val valueResolverService: ValueResolverService,
) : WidgetDataProvider<InteractiveTableWidget> {

    override fun supportedWidgetType() = InteractiveTableWidget::class.java

    override fun getData(
        widget: InteractiveTableWidget,
        properties: Map<String, Any>
    ): InteractiveTableWidgetDataResult {
        val pageable = properties[PAGEABLE] as Pageable? ?: Pageable.ofSize(widget.properties.defaultPageSize)

        val resolvedValues = valueResolverService.resolveValues(
            properties,
            widget.getUnresolvedValues()
        )

        val collectionNode = objectMapper.valueToTree<JsonNode>(resolvedValues[widget.properties.collection])
        val exposedValues = widget.getExposedValues { path -> resolvedValues[path] }

        if (collectionNode.isNull) {
            return InteractiveTableWidgetDataResult(
                resolved = exposedValues,
                table = PageImpl(emptyList(), pageable, 0)
            )
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
                InteractiveTableResult(
                    data = widget.properties.columns.associate { column ->
                        column.key to getValueAt(child, column.value)
                    },
                    resolved = widget.properties.rowClickAction?.getExposedValues(
                        { path -> resolvedValues[path] ?: getValueAt(child, path) }
                    ),
                )
            }

        return InteractiveTableWidgetDataResult(
            resolved = exposedValues,
            table = PageImpl(result, pageable, collectionNode.size().toLong())
        )
    }

    private inline fun getValueAt(data: JsonNode, path: String): Any? {
        return if (path.startsWith("$")) {
            JSONPATH_CONTEXT.parse(data.toString()).read<Any?>(path)
        } else {
            val pointer = if (path.startsWith("/")) path else "/${path}"
            val valueNode = data.at(pointer)

            if (valueNode.isValueNode && !valueNode.isNull) {
                objectMapper.treeToValue(valueNode)
            } else {
                null
            }
        }
    }

    private companion object {
        val JSONPATH_CONTEXT =
            JsonPath.using(Configuration.defaultConfiguration().addOptions(Option.SUPPRESS_EXCEPTIONS))
    }
}

