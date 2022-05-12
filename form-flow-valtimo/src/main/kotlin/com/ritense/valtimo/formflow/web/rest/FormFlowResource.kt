/*
 * Copyright 2015-2022 Ritense BV, the Netherlands.
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

package com.ritense.valtimo.formflow.web.rest

import com.fasterxml.jackson.databind.JsonNode
import com.ritense.form.service.FormDefinitionService
import com.ritense.formflow.domain.instance.FormFlowInstanceId
import com.ritense.formflow.domain.instance.FormFlowStepInstanceId
import com.ritense.formflow.service.FormFlowService
import com.ritense.valtimo.formflow.web.rest.dto.FormFlow
import com.ritense.valtimo.formflow.web.rest.dto.FormFlowStep
import com.ritense.valtimo.formflow.web.rest.dto.FormTypeProperties
import com.ritense.valtimo.formflow.web.rest.result.CompleteStepResult
import com.ritense.valtimo.formflow.web.rest.result.FormFlowStepResult
import com.ritense.valtimo.formflow.web.rest.result.GetFormFlowStateResult
import org.json.JSONObject
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID
import javax.transaction.Transactional

@RestController
@RequestMapping(value = ["/api/form-flow"])
class FormFlowResource(
    private val formFlowService: FormFlowService,
    private val formDefinitionService: FormDefinitionService
) {
    @GetMapping("/{formFlowId}")
    fun createInstance(
        @PathVariable(name = "formFlowId") formFlowId: String
    ): ResponseEntity<FormFlow> {
        return ResponseEntity.ok(getStepDto())
    }

    @PostMapping("/{formFlowId}/step/{stepInstanceId}")
    fun completeStep(
        @PathVariable(name = "formFlowId") formFlowId: String,
        @PathVariable(name = "stepInstanceId") stepInstanceId: String,
        @RequestBody submissionData: JsonNode?
    ): ResponseEntity<FormFlow> {
        return ResponseEntity.ok(getStepDto())
    }

    @GetMapping("/{formFlowInstanceId}", produces = [MediaType.APPLICATION_JSON_VALUE])
    @Transactional
    fun getFormFlowState(
        @PathVariable(name = "formFlowInstanceId") instanceId: String,
    ): ResponseEntity<GetFormFlowStateResult>? {
        val instance = formFlowService.getByInstanceIdIfExists(
            FormFlowInstanceId.existingId(UUID.fromString(instanceId))
        ) ?: return ResponseEntity
            .badRequest()
            .body(GetFormFlowStateResult(null, null, "No form flow instance can be found for the given instance id"))

        val stepInstance = instance.getCurrentStep()

        return ResponseEntity.ok(
            GetFormFlowStateResult(
                instance.id.id,
                FormFlowStepResult(
                    stepInstance.id.id,
                    stepInstance.definition.type.name,
                    formFlowService.getTypeProperties(stepInstance)
                )
            )
        )
    }

    @PostMapping("/{formFlowId}/step/{stepInstanceId}", produces = [MediaType.APPLICATION_JSON_VALUE])
    @Transactional
    fun completeStep(
        @PathVariable(name = "formFlowId") formFlowId: String,
        @PathVariable(name = "stepInstanceId") stepInstanceId: String,
        @RequestBody submissionData: JSONObject?
    ): ResponseEntity<CompleteStepResult> {
        val instance = formFlowService.getByInstanceIdIfExists(
            FormFlowInstanceId.existingId(UUID.fromString(formFlowId))
        )!!

        val stepInstance = instance.complete(
            FormFlowStepInstanceId.existingId(UUID.fromString(stepInstanceId)),
            submissionData ?: JSONObject()
        )!!

        return ResponseEntity.ok(
            CompleteStepResult(
                instance.id.id,
                FormFlowStepResult(
                    stepInstance.id.id,
                    stepInstance.definition.type.name,
                    formFlowService.getTypeProperties(stepInstance)
                )
            )
        )
    }
    private fun getStepDto(): FormFlow {
        return FormFlow(
            UUID.randomUUID(),
            FormFlowStep(
                UUID.randomUUID(),
                "form",
                FormTypeProperties(
                    formDefinitionService
                        .getFormDefinitionByName("user-task-lening-aanvragen")
                        .get()
                        .formDefinition
                )
            )
        )
    }
}
