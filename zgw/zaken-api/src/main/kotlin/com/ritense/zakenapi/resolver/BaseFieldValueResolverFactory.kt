/*
 * Copyright 2015-2023 Ritense BV, the Netherlands.
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

package com.ritense.zakenapi.resolver

import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.processdocument.domain.impl.CamundaProcessInstanceId
import com.ritense.processdocument.service.ProcessDocumentService
import com.ritense.valueresolver.ValueResolverFactory
import org.camunda.bpm.engine.delegate.VariableScope
import java.lang.NullPointerException
import java.util.UUID
import java.util.function.Function

abstract class BaseFieldValueResolverFactory(
    private val objectMapper: ObjectMapper,
    private val processDocumentService: ProcessDocumentService,
) : ValueResolverFactory {

    override fun createResolver(
        processInstanceId: String,
        variableScope: VariableScope
    ): Function<String, Any?> {
        return Function { requestedValue ->
            val documentId =
                processDocumentService.getDocumentId(CamundaProcessInstanceId(processInstanceId), variableScope)
                    .toString()
            getResolvedValue(UUID.fromString(documentId), requestedValue)
        }
    }

    override fun createResolver(documentId: String): Function<String, Any?> {
        return Function { requestedValue ->
            getResolvedValue(UUID.fromString(documentId), requestedValue)
        }
    }

    abstract fun getResolvedValue(documentId: UUID, field: String): Any?

    fun getField(entity: Any, field: String): Any? {
        var currentEntity: Any? = entity
        val fields = field.split('.')
        fields.forEachIndexed { index, value ->
            val declaredField = currentEntity!!.javaClass.getDeclaredField(value)
            declaredField.trySetAccessible()

            // Field.get(obj) does not (always) seem to work according to spec, because it throws a NullPointerException when the value of a property is null
            currentEntity = try {
                declaredField.get(currentEntity)
            } catch (npe: NullPointerException) {
                if (index == fields.size - 1) {
                    null
                } else {
                    throw npe
                }
            }
        }
        return currentEntity
    }
}
