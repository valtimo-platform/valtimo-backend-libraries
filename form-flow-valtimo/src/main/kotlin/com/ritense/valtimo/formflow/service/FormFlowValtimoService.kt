/*
 *  Copyright 2015-2024 Ritense BV, the Netherlands.
 *
 *  Licensed under EUPL, Version 1.2 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" basis,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.ritense.valtimo.formflow.service

import com.fasterxml.jackson.core.JsonPointer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.MissingNode
import com.ritense.document.domain.patch.JsonPatchService
import com.ritense.form.domain.FormIoFormDefinition
import com.ritense.form.service.impl.FormIoFormDefinitionService
import com.ritense.formflow.domain.definition.configuration.step.FormStepTypeProperties
import com.ritense.formflow.domain.instance.FormFlowInstance
import com.ritense.logging.withLoggingContext
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import com.ritense.valtimo.contract.json.patch.JsonPatchBuilder
import org.springframework.stereotype.Service
import kotlin.jvm.optionals.getOrNull

@Service
@SkipComponentScan
class FormFlowValtimoService(
    private val formDefinitionService: FormIoFormDefinitionService,
    private val objectMapper: ObjectMapper,
    private val doSubmissionDataFiltering: Boolean
) {
    constructor(
        formDefinitionService: FormIoFormDefinitionService,
        objectMapper: ObjectMapper
    ) : this(formDefinitionService, objectMapper, true)

    fun getVerifiedSubmissionData(submissionData: JsonNode?, formFlowInstance: FormFlowInstance): JsonNode? {
        return withLoggingContext(FormFlowInstance::class, formFlowInstance.id) {
            if (submissionData == null) {
                return@withLoggingContext null
            }

            val currentStepTypeProperties = formFlowInstance.getCurrentStep().definition.type.properties
            if (currentStepTypeProperties !is FormStepTypeProperties || !doSubmissionDataFiltering) {
                return@withLoggingContext submissionData
            }

            val jsonPatchBuilder = JsonPatchBuilder()
            val verifiedSubmissionData = objectMapper.createObjectNode()

            val validJsonPointers = formDefinitionService.getFormDefinitionByName(currentStepTypeProperties.definition)
                .orElseThrow().inputFields
                .mapNotNull { field -> FormIoFormDefinition.getKey(field).getOrNull() }
                .map { fieldKey -> JsonPointer.valueOf("/${fieldKey.replace('.', '/')}") }

            validJsonPointers.forEach { validJsonPointer ->
                val verifiedSubmissionNode = submissionData.at(validJsonPointer)
                if (verifiedSubmissionNode !is MissingNode) {
                    jsonPatchBuilder.addJsonNodeValue(verifiedSubmissionData, validJsonPointer, verifiedSubmissionNode)
                }
            }

            JsonPatchService.apply(jsonPatchBuilder.build(), verifiedSubmissionData)

            verifiedSubmissionData
        }
    }
}