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
import com.ritense.iko.plugin.IkoPlugin
import com.ritense.plugin.service.PluginService
import com.ritense.valtimo.contract.iko.Comparator
import com.ritense.valtimo.contract.iko.DataFilter
import com.ritense.valtimo.contract.iko.IkoConnector
import com.ritense.valtimo.contract.iko.PropertyField
import com.ritense.valtimo.contract.iko.PropertyField.Companion.PROPERTY_FIELD_TYPE_DROPDOWN
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable

class IkoServerConnector(
    private val pluginService: PluginService,
) : IkoConnector {

    override fun getType() = "iko"

    override fun getIkoConnectorConfigPropertyFields(): List<PropertyField> {
        val dropdownList = pluginService.findPluginConfigurations(IkoPlugin::class.java)
            .map { it.id.toString() to it.title }

        return listOf(
            PropertyField(
                title = PropertyField.toReadableText(PLUGIN_CONFIGURATION),
                key = PLUGIN_CONFIGURATION,
                type = PROPERTY_FIELD_TYPE_DROPDOWN,
                dropdownList = dropdownList
            )
        )
    }

    override fun getDataAggregatePropertyFields(): List<PropertyField> = listOf(PropertyField(SEARCH_PATH))

    override fun getDataRequestPropertyFields(): List<PropertyField> = listOf(PropertyField(SEARCH_TYPE))

    override fun findAll(
        config: Map<String, Any?>,
        filters: List<DataFilter>,
        pageable: Pageable
    ): Page<JsonNode> {
        require(filters.all { it.comparator == Comparator.EQUAL_TO })
        val filterMap = filters.associate {
            it.property
                .substringAfterLast(':')
                .replace("/", "__")
                .trim('_') to it.value.toString()
        }

        val data = getPlugin(config).search(
            searchPath = config[SEARCH_PATH].toString(),
            searchType = config[SEARCH_TYPE]?.toString(),
            filters = filterMap,
        )

        val dataList: List<JsonNode> = if (data is ArrayNode) {
            data.toList()
        } else {
            val lists = data.filter { it is ArrayNode }
            if (lists.size == 1) {
                lists[0].toList()
            } else {
                listOf(data)
            }
        }

        return PageImpl(dataList, pageable, dataList.size.toLong())
    }

    override fun findById(config: Map<String, Any?>, id: Any): JsonNode {
        return getPlugin(config).getById(
            searchPath = config[SEARCH_PATH].toString(),
            id = id.toString()
        )
    }

    private fun getPlugin(config: Map<String, Any?>): IkoPlugin {
        return pluginService.createInstance(config[PLUGIN_CONFIGURATION].toString())
    }

    companion object {
        private const val PLUGIN_CONFIGURATION = "pluginConfiguration"
        private const val SEARCH_PATH = "searchPath"
        const val SEARCH_TYPE = "searchType"
    }
}