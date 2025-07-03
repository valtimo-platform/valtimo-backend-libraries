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

package com.ritense.valtimo.contract.iko

import com.fasterxml.jackson.databind.JsonNode
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface IkoConnector {

    fun getType(): String

    fun getIkoConnectorPropertyFields(): List<PropertyField> = emptyList()

    fun getDataAggregatePropertyFields(): List<PropertyField> = emptyList()

    fun getDataRequestPropertyFields(): List<PropertyField> = emptyList()

    fun findAll(config: Map<String, Any?>, filters: List<DataFilter>, pageable: Pageable): Page<JsonNode>

    fun findAll(config: Map<String, Any?>, filters: List<DataFilter>): Page<JsonNode> =
        findAll(config, filters, Pageable.ofSize(10))
}