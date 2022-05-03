/*
 *  Copyright 2015-2022 Ritense BV, the Netherlands.
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

package com.ritense.valtimo.formflow.web.rest

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.ritense.formflow.domain.instance.FormFlowInstanceId
import com.ritense.formflow.domain.instance.FormFlowStepInstanceId
import com.ritense.formflow.service.FormFlowService
import com.ritense.valtimo.formflow.web.rest.result.CompleteStepResult
import com.ritense.valtimo.formflow.web.rest.result.CreateInstanceResult
import org.json.JSONObject
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID
import javax.transaction.Transactional
import org.springframework.web.bind.annotation.RequestParam

@RestController
@RequestMapping(value = ["/api/form-flow/demo"])
class FormFlowDemoResource(
    val formFlowService: FormFlowService
) {

    @PostMapping("/definition/{definitionKey}/instance")
    fun createInstance(
        @PathVariable(name = "definitionKey") definitionKey: String,
        @RequestParam(name = "openFirstStep", defaultValue = "false") openFirstStep: Boolean = false,
        @RequestBody additionalParameters: MutableMap<String, Any>?
    ): ResponseEntity<CreateInstanceResult> {
        val latestDefinition = formFlowService.findLatestDefinitionByKey(definitionKey)
        val createdInstance = latestDefinition!!.createInstance(additionalParameters?: mutableMapOf())

        if(openFirstStep) {
            createdInstance.getCurrentStep().open()
        }
        formFlowService.save(createdInstance)

        return ResponseEntity.ok(
            CreateInstanceResult(
                createdInstance.id,
                createdInstance.currentFormFlowStepInstanceId,
                createdInstance.getCurrentStep().stepKey)
        )
    }

    @PostMapping("instance/{instanceId}/step/{stepId}/complete")
    @Transactional
    fun completeStep(
        @PathVariable(name = "instanceId") instanceId: String,
        @PathVariable(name = "stepId") stepId: String,
        @RequestParam(name = "openNext", defaultValue = "false") openNext: Boolean = false,
        @RequestBody submissionData: JsonNode?
    ): ResponseEntity<CompleteStepResult> {

        val instance = formFlowService.getInstanceById(
            FormFlowInstanceId.existingId(UUID.fromString(instanceId))
        )

        val formFlowStepInstance = instance.complete(
            FormFlowStepInstanceId.existingId(UUID.fromString(stepId)),
            JSONObject((submissionData?:JsonNodeFactory.instance.objectNode()).toString())
        )

        if(openNext) {
            formFlowStepInstance?.open()
        }

        formFlowService.save(instance)
        return ResponseEntity.ok(
            CompleteStepResult(instance.id, formFlowStepInstance?.id, formFlowStepInstance?.stepKey))
    }
}