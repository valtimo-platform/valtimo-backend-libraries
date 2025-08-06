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

package com.ritense.iko

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ContainerNode
import com.ritense.iko.plugin.IkoPlugin
import com.ritense.plugin.service.PluginService
import com.ritense.valtimo.contract.iko.Comparator
import com.ritense.valtimo.contract.iko.DataFilter
import com.ritense.valtimo.contract.iko.IkoRepository
import com.ritense.valtimo.contract.iko.PropertyField
import com.ritense.valtimo.contract.iko.PropertyField.Companion.PROPERTY_FIELD_TYPE_DROPDOWN
import com.ritense.valtimo.contract.iko.PropertyField.Companion.PROPERTY_FIELD_TYPE_KEY_VALUE_LIST
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable

class IkoServerRepository(
    private val pluginService: PluginService,
) : IkoRepository {

    override fun getType() = "iko"

    override fun getIkoRepositoryConfigPropertyFields(): List<PropertyField> {
        val dropdownList = pluginService.findPluginConfigurations(IkoPlugin::class.java)
            .map { it.id.toString() to it.title }

        return listOf(
            PropertyField(PLUGIN_CONFIGURATION, PROPERTY_FIELD_TYPE_DROPDOWN, dropdownList = dropdownList)
        )
    }

    override fun getDataAggregatePropertyFields(): List<PropertyField> = listOf(
        PropertyField(
            ENDPOINT_PATH,
            tooltip = "The last few path segments of the IKO API URL. i.e. 'bag/adressen'"
        ),
        PropertyField(
            AGGREGATED_DATA_PROFILE_NAME,
            tooltip = "The name of the aggregated data profile. i.e. 'personen'"
        ),
        PropertyField(
            ENDPOINT_QUERY_PARAMETERS,
            PROPERTY_FIELD_TYPE_KEY_VALUE_LIST,
            tooltip = "Additional query parameters for the IKO API URL. i.e. 'type=ZoekMetGeslachtsnaamEnGeboortedatum'"
        ),
    )

    override fun getDataRequestPropertyFields(): List<PropertyField> = listOf(
        PropertyField(
            ENDPOINT_QUERY_PARAMETERS,
            PROPERTY_FIELD_TYPE_KEY_VALUE_LIST,
            tooltip = "Additional query parameters for the IKO API URL. i.e. 'type=ZoekMetGeslachtsnaamEnGeboortedatum'"
        ),
    )

    override fun findAll(
        config: Map<String, Any?>,
        filters: List<DataFilter>,
        pageable: Pageable
    ): Page<JsonNode> {
        require(filters.all { it.comparator == Comparator.EQUAL_TO })
        val configuredFilterMap = (config[ENDPOINT_QUERY_PARAMETERS] as Map<String, String>?) ?: emptyMap()
        val filterMap = configuredFilterMap + filters.associate {
            val filterKey = it.property.substringAfter(':').substringBefore('=')
            val filterValue = it.property.substringAfter('=', "") + it.value.toString()
            filterKey to filterValue
        }

        val data = getPlugin(config).search(
            endpointPath = config[ENDPOINT_PATH].toString(),
            filters = filterMap,
        )

        val arrayData = breathFirstSearch(data) { it is ArrayNode } as ArrayNode?
        val dataList = arrayData?.toList() ?: listOf(data)
        return PageImpl(dataList, pageable, dataList.size.toLong())
    }

    override fun findById(config: Map<String, Any?>, id: Any): JsonNode {
        val aggregatedDataProfileName = config[AGGREGATED_DATA_PROFILE_NAME] as String?

        return if (!aggregatedDataProfileName.isNullOrBlank()) {
            getPlugin(config).getByAggregatedDataProfileId(
                aggregatedDataProfileName = aggregatedDataProfileName,
                id = id.toString()
            )
        } else {
            getPlugin(config).getByEndpointId(
                endpointPath = config[ENDPOINT_PATH].toString(),
                id = id.toString()
            )
        }
    }

    private fun getPlugin(config: Map<String, Any?>): IkoPlugin {
        return pluginService.createInstance(config[PLUGIN_CONFIGURATION].toString())
    }

    private fun breathFirstSearch(node: JsonNode, exitCondition: (JsonNode) -> Boolean): JsonNode? {
        val queue: ArrayDeque<JsonNode> = ArrayDeque()
        queue.add(node)

        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()
            if (exitCondition(current)) {
                return current
            }

            if (current is ContainerNode<*>) {
                current.forEach { child ->
                    queue.add(child)
                }
            }
        }

        return null
    }

    companion object {
        const val PLUGIN_CONFIGURATION = "pluginConfiguration"
        const val ENDPOINT_PATH = "endpointPath"
        const val AGGREGATED_DATA_PROFILE_NAME = "aggregatedDataProfileName"
        const val ENDPOINT_QUERY_PARAMETERS = "endpointQueryParameters"
    }
}