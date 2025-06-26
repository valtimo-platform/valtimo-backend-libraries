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

import com.ritense.iko.service.IkoDataAggregateService
import com.ritense.valueresolver.ValueResolverFactory
import io.github.oshai.kotlinlogging.KotlinLogging
import org.camunda.bpm.engine.delegate.VariableScope
import java.util.function.Function

class IkoValueResolverFactory(
    private val ikoDataAggregateService: IkoDataAggregateService,
) : ValueResolverFactory {

    override fun supportedPrefix(): String {
        return "iko"
    }

    override fun createResolver(
        processInstanceId: String,
        variableScope: VariableScope
    ): Function<String, Any?> {
        return createResolver()
    }

    override fun createResolver(documentId: String): Function<String, Any?> {
        return createResolver()
    }

    fun createResolver(): Function<String, Any?> {
        return Function { requestedValue ->
            val (dataAggregateKey, jsonPointer) = getDataAggregateKeyAndJsonPointer(requestedValue)
            ikoDataAggregateService.findData(dataAggregateKey).at(jsonPointer)
        }
    }

    override fun handleValues(
        processInstanceId: String,
        variableScope: VariableScope?,
        values: Map<String, Any?>
    ) {
        throw NotImplementedError()
    }

    private fun getDataAggregateKeyAndJsonPointer(requestedValue: String): Pair<String, String> {
        val parts = requestedValue.split(":")
        require(parts[0] == "iko")
        return Pair(parts[1], parts[2])
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}