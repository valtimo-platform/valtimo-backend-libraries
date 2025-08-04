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

package com.ritense.zakenapi.ikorepository

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.ritense.plugin.service.PluginService
import com.ritense.valtimo.contract.iko.DataFilter
import com.ritense.valtimo.contract.iko.IkoRepository
import com.ritense.valtimo.contract.iko.PropertyField
import com.ritense.valtimo.contract.iko.PropertyField.Companion.PROPERTY_FIELD_TYPE_DROPDOWN
import com.ritense.valtimo.contract.iko.PropertyField.Companion.PROPERTY_FIELD_TYPE_URL
import com.ritense.zakenapi.ZakenApiPlugin
import com.ritense.zakenapi.domain.Comparator
import com.ritense.zakenapi.domain.SearchParameter
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI

class ZakenApiIkoRepository(
    private val pluginService: PluginService,
    private val objectMapper: ObjectMapper,
) : IkoRepository {

    override fun getType() = "zakenApi"

    override fun getIkoRepositoryConfigPropertyFields(): List<PropertyField> {
        val dropdownList = pluginService.findPluginConfigurations(ZakenApiPlugin::class.java)
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
        PropertyField(ZAAKTYPE_URL, PROPERTY_FIELD_TYPE_URL),
    )

    override fun findAll(
        config: Map<String, Any?>,
        filters: List<DataFilter>,
        pageable: Pageable
    ): Page<JsonNode> {
        val searchParameters = filters.map { filter ->
            val property = filter.property.substringAfter(':')
            val operator = Comparator.entries.single { it.name == filter.comparator.name }
            SearchParameter(property, operator, filter.value?.toString())
        } + SearchParameter("zaaktype", Comparator.EQUAL_TO, config[ZAAKTYPE_URL].toString())

        val zaakList = getPlugin(config).searchZaken(
            searchParameters = searchParameters,
            pageable = pageable,
        )

        val jsonZaakList = objectMapper.valueToTree<ArrayNode>(zaakList.results)
        return PageImpl(jsonZaakList.toList(), pageable, zaakList.count.toLong())
    }

    override fun findById(config: Map<String, Any?>, id: Any): JsonNode {
        val plugin = getPlugin(config)
        val zaakUrl = UriComponentsBuilder.fromUri(plugin.url)
            .pathSegment("zaken")
            .pathSegment(id.toString())
            .toUriString()

        val zaakWrapper = getPlugin(config).getZaak(
            zaakUrl = URI(zaakUrl),
        )

        return objectMapper.valueToTree(zaakWrapper)
    }

    private fun getPlugin(config: Map<String, Any?>): ZakenApiPlugin {
        return pluginService.createInstance(config[PLUGIN_CONFIGURATION].toString())
    }

    companion object {
        private const val PLUGIN_CONFIGURATION = "pluginConfiguration"
        private const val ZAAKTYPE_URL = "zaaktypeUrl"
    }
}