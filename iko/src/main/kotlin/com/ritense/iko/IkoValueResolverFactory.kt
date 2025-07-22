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
import com.ritense.valueresolver.ValueResolverContextKey.Companion.ID
import com.ritense.valueresolver.ValueResolverContextKey.Companion.IKO_DATA_AGGREGATE_KEY
import com.ritense.valueresolver.ValueResolverContextKey.Companion.IKO_DATA_REQUEST_KEY
import org.springframework.data.domain.Pageable
import java.util.function.Function

class IkoValueResolverFactory(
    private val ikoDataAggregateService: IkoDataAggregateService,
    private val ikoDataRequestService: IkoDataRequestService,
    private val ikoSearchFieldService: IkoSearchFieldService,
    private val objectMapper: ObjectMapper,
) {

    fun supportedPrefix(): String {
        return "iko"
    }

    fun createResolver(context: Map<String, Any?>, pageable: Pageable): Function<String, Any?> {
        val ikoDataAggregateKey = context[IKO_DATA_AGGREGATE_KEY]?.toString()
        val id = context[ID]?.toString()
        if (ikoDataAggregateKey != null && id != null) {
            val data = ikoDataAggregateService.getDataById(ikoDataAggregateKey, id)
            return Function { jsonPointer ->
                data.at(jsonPointer)
            }
        }

        val ikoDataRequestKey = context[IKO_DATA_REQUEST_KEY]?.toString()
        val (dataRequest, searchFields) = ikoDataRequestService.findAll(
            key = ikoDataRequestKey,
            ikoDataAggregateKey = ikoDataAggregateKey,
        ).map { dataRequest ->
            dataRequest to ikoSearchFieldService.findAllSearchFieldsByIkoDataRequest(
                ikoDataAggregateKey = dataRequest.id.ikoDataAggregate.key,
                ikoDataRequestKey = dataRequest.id.key
            )
        }
            .filter { (_, searchFields) -> searchFields.all { context.keys.contains(it.key) || !it.required } }
            .maxByOrNull { (_, searchFields) -> searchFields.count { it.required } }
            ?: return Function { jsonPointer ->
                val unresolvedValue = "${supportedPrefix()}:$jsonPointer"
                error("Missing ValueResolver context. For value resolver '$unresolvedValue'. Try '$unresolvedValue?$IKO_DATA_AGGREGATE_KEY=example&$ID=example'")
            }

        val filters = searchFields.map { searchField -> DataFilter(searchField.key, context[searchField.key]) }
        val dataPaged = ikoDataRequestService.searchData(
            key = dataRequest.id.key,
            ikoDataAggregateKey = dataRequest.id.ikoDataAggregate.key,
            filters = filters,
            pageable = pageable
        )
        val data = objectMapper.valueToTree<ArrayNode>(dataPaged.content)
        return Function { jsonPointer ->
            data.at(jsonPointer)
        }
    }
}