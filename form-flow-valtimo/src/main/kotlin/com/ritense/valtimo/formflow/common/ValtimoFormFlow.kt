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

package com.ritense.valtimo.formflow.common

import com.fasterxml.jackson.core.JsonPointer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.formflow.expression.FormFlowBean
import com.ritense.valtimo.contract.json.Mapper
import com.ritense.valueresolver.ValueResolverService
import org.camunda.bpm.engine.TaskService
import org.springframework.transaction.annotation.Transactional

@FormFlowBean
open class ValtimoFormFlow(
    private val taskService: TaskService,
    private val objectMapper: ObjectMapper,
    private val valueResolverService: ValueResolverService,
) {

    /**
     * Completes a Camunda user task
     *
     * @param additionalProperties provided by Form Flow
     */
    @Transactional
    open fun completeTask(additionalProperties: Map<String, Any>) {
        return completeTask(additionalProperties, null)
    }

    /**
     * Completes a Camunda user task and save the submission in the document
     *
     * @param additionalProperties provided by Form Flow
     * @param submissionData the data that was submitted at the end of a Form Flow step
     */
    @Transactional
    open fun completeTask(additionalProperties: Map<String, Any>, submissionData: JsonNode?) {
        return completeTask(additionalProperties, submissionData, mapOf("doc:/submission" to ""))
    }

    /**
     * Completes a Camunda user task and save the submission in a place defined in submissionSavePath
     *
     * @param additionalProperties provided by Form Flow
     * @param submissionData the data that was submitted at the end of a Form Flow step
     * @param submissionSavePath where the submission data should be saved. The key should be the save location, the value it the path in the submissionData.
     */
    @Transactional
    open fun completeTask(
        additionalProperties: Map<String, Any>,
        submissionData: JsonNode?,
        submissionSavePath: Map<String, String>
    ) {
        if (submissionData != null) {
            val processInstanceId = additionalProperties["processInstanceId"] as String
            val submissionValues = submissionSavePath.entries.associate { it.key to getValue(submissionData, it.value) }
            valueResolverService.handleValues(processInstanceId, null, submissionValues)
        }

        taskService.complete(additionalProperties["taskInstanceId"] as String)
    }

    private fun getValue(data: JsonNode, path: String): Any {
        val valueNode = data.at(JsonPointer.valueOf(path))
        if (valueNode.isMissingNode) {
            throw RuntimeException("Missing data on path '$path'")
        }
        return objectMapper.treeToValue(valueNode, Object::class.java)
    }
}
