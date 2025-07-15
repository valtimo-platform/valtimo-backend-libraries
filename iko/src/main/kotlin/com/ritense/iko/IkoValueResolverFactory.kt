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

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.ritense.iko.service.IkoDataAggregateService
import com.ritense.iko.service.IkoDataRequestService
import com.ritense.iko.service.IkoSearchFieldService
import com.ritense.valtimo.contract.iko.DataFilter
import org.springframework.data.domain.Pageable
import java.util.function.Function

class IkoValueResolverFactory(
    private val ikoDataAggregateService: IkoDataAggregateService,
    private val ikoDataRequestService: IkoDataRequestService,
    private val searchFieldService: IkoSearchFieldService,
    private val objectMapper: ObjectMapper,
) {

    fun supportedPrefix(): String {
        return "iko"
    }

    fun createResolver(context: List<Any>, pageable: Pageable): Function<String, Any?> {
        val pairContext = toContextMap(context)
        val ikoDataAggregateKey = pairContext["ikoDataAggregateKey"].toString()
        val id = pairContext["id"]?.toString()
        if (id != null) {
            val data = ikoDataAggregateService.getDataById(ikoDataAggregateKey, id)
            return Function { jsonPointer ->
                data.at(jsonPointer)
            }
        }

        val (dataRequest, searchFields) = ikoDataRequestService.findAll(ikoDataAggregateKey = ikoDataAggregateKey)
            .asSequence().map { dataRequest ->
                dataRequest to searchFieldService.findAllSearchFieldsByIkoDataRequest(
                    ikoDataAggregateKey = ikoDataAggregateKey,
                    ikoDataRequestKey = dataRequest.id.key
                )
            }.first { (_, searchFields) ->
                searchFields.all { pairContext.keys.contains(it.key) || !it.required }
            }

        val filters = searchFields.map { searchField -> DataFilter(searchField.key, pairContext[searchField.key]) }
        val dataPaged = ikoDataRequestService.searchData(dataRequest.id.key, ikoDataAggregateKey, filters, pageable)
        val data = objectMapper.valueToTree<ArrayNode>(dataPaged.content)
        return Function { jsonPointer ->
            data.at(jsonPointer)
        }
    }

    private fun toContextMap(context: List<Any>): Map<String, Any?> {
        return (context.filter { it is Pair<*, *> && it.first is String } as List<Pair<String, Any?>>)
            .associate { it.first to it.second }
    }
}