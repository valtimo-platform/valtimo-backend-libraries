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

package com.ritense.objectenapi.ikorepository

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.ritense.objectenapi.ObjectenApiPlugin
import com.ritense.plugin.service.PluginService
import com.ritense.valtimo.contract.iko.DataFilter
import com.ritense.valtimo.contract.iko.IkoRepository
import com.ritense.valtimo.contract.iko.PropertyField
import com.ritense.valtimo.contract.iko.PropertyField.Companion.PROPERTY_FIELD_TYPE_DROPDOWN
import com.ritense.valtimo.contract.iko.PropertyField.Companion.PROPERTY_FIELD_TYPE_INTEGER
import com.ritense.valtimo.contract.iko.PropertyField.Companion.PROPERTY_FIELD_TYPE_URL
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI

class ObjectenApiIkoRepository(
    private val pluginService: PluginService,
    private val objectMapper: ObjectMapper,
) : IkoRepository {

    override fun getType() = "objectenApi"

    override fun getIkoRepositoryConfigPropertyFields(): List<PropertyField> {
        val dropdownList = pluginService.findPluginConfigurations(ObjectenApiPlugin::class.java)
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

    override fun getDataAggregatePropertyFields(): List<PropertyField> = listOf(
        PropertyField(OBJECTTYPEN_API_URL, PROPERTY_FIELD_TYPE_URL),
        PropertyField(OBJECT_TYPE_VERSION, PROPERTY_FIELD_TYPE_INTEGER),
    )

    override fun findAll(
        config: Map<String, Any?>,
        filters: List<DataFilter>,
        pageable: Pageable
    ): Page<JsonNode> {
        val searchString = filters.joinToString(",") { filter ->
            var property = filter.property.substringAfterLast(':')
            require(property.startsWith("/record/data/"))
            property = property.substringAfter("/record/data/")
                .replace("/", "__")
            val operator = com.ritense.objectenapi.client.Comparator.entries
                .single { it.name == filter.comparator.name }
            "${property}__${operator.value}__${filter.value}"
        }

        val (objecttypesApiUrl, objecttypeId) = config[OBJECTTYPEN_API_URL].toString().split("/objecttypes/")

        val objectList = getPlugin(config).getObjectsByObjectTypeIdWithSearchParams(
            objecttypesApiUrl = URI(objecttypesApiUrl),
            objecttypeId = objecttypeId,
            searchString = searchString,
            pageable = pageable,
        )

        val jsonObjectList = objectMapper.valueToTree<ArrayNode>(objectList.results)
        return PageImpl(jsonObjectList.toList(), pageable, objectList.count.toLong())
    }

    override fun findById(config: Map<String, Any?>, id: Any): JsonNode {
        val plugin = getPlugin(config)
        val objectUrl = UriComponentsBuilder.newInstance()
            .uri(plugin.url)
            .pathSegment("objects")
            .pathSegment(id.toString())
            .toUriString()

        val objectWrapper = getPlugin(config).getObject(
            objectUrl = URI(objectUrl),
        )

        return objectMapper.valueToTree(objectWrapper)
    }

    private fun getPlugin(config: Map<String, Any?>): ObjectenApiPlugin {
        return pluginService.createInstance(config[PLUGIN_CONFIGURATION].toString())
    }

    companion object {
        private const val PLUGIN_CONFIGURATION = "pluginConfiguration"
        private const val OBJECTTYPEN_API_URL = "objecttypenApiUrl"
        private const val OBJECT_TYPE_VERSION = "objectTypeVersion"
    }
}