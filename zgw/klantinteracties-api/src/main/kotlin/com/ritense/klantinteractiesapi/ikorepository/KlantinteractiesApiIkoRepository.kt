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

package com.ritense.klantinteractiesapi.ikorepository

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.ritense.klantinteractiesapi.KlantinteractiesApiPlugin
import com.ritense.klantinteractiesapi.client.dto.GetPartijenRequest
import com.ritense.klantinteractiesapi.domain.PartijSoort
import com.ritense.plugin.service.PluginService
import com.ritense.valtimo.contract.iko.Comparator
import com.ritense.valtimo.contract.iko.DataFilter
import com.ritense.valtimo.contract.iko.IkoRepository
import com.ritense.valtimo.contract.iko.PropertyField
import com.ritense.valtimo.contract.iko.PropertyField.Companion.PROPERTY_FIELD_TYPE_DROPDOWN
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import java.util.UUID

class KlantinteractiesApiIkoRepository(
    private val pluginService: PluginService,
    private val objectMapper: ObjectMapper,
) : IkoRepository {

    override fun getType() = "klantinteractiesApi"

    override fun getIkoRepositoryConfigPropertyFields(): List<PropertyField> {
        val dropdownList = pluginService.findPluginConfigurations(KlantinteractiesApiPlugin::class.java)
            .map { it.id.toString() to it.title }

        return listOf(
            PropertyField(PLUGIN_CONFIGURATION, PROPERTY_FIELD_TYPE_DROPDOWN, dropdownList = dropdownList)
        )
    }

    override fun getDataAggregatePropertyFields(): List<PropertyField> = listOf(
        PropertyField(
            PARTIJ_SOORT,
            PROPERTY_FIELD_TYPE_DROPDOWN,
            dropdownList = listOf(
                "persoon" to "Persoon",
                "organisatie" to "Organisatie",
                "contactpersoon" to "Contactpersoon"
            )
        ),
    )

    override fun findAll(
        config: Map<String, Any?>,
        filters: List<DataFilter>,
        pageable: Pageable
    ): Page<JsonNode> {
        val filters = filters.associate { filter ->
            require(filter.comparator == Comparator.EQUAL_TO)
            val property = filter.property.substringAfterLast(':')
                .replace("/", "__")
            property to filter.value.toString()
        }

        val partijenPage = getPlugin(config).getPartijen(
            request = GetPartijenRequest(
                soortPartij = PartijSoort.valueOf(config["soortPartij"].toString().uppercase()),
                filters = filters
            ),
            pageable = pageable,
        )

        val jsonPartijenList = objectMapper.valueToTree<ArrayNode>(partijenPage.results)
        return PageImpl(jsonPartijenList.toList(), pageable, partijenPage.count.toLong())
    }

    override fun findById(config: Map<String, Any?>, id: Any): JsonNode {
        val plugin = getPlugin(config)
        val partij = getPlugin(config).getPartij(
            partijUrl = plugin.getParijUrl(UUID.fromString(id.toString())),
        )
        return objectMapper.valueToTree(partij)
    }

    private fun getPlugin(config: Map<String, Any?>): KlantinteractiesApiPlugin {
        return pluginService.createInstance(config[PLUGIN_CONFIGURATION].toString())
    }

    companion object {
        private const val PLUGIN_CONFIGURATION = "pluginConfiguration"
        private const val PARTIJ_SOORT = "partijSoort"
    }
}