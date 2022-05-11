package com.ritense.valtimo.formflow.web.rest

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.ritense.form.service.FormDefinitionService
import com.ritense.formflow.domain.instance.FormFlowInstanceId
import com.ritense.formflow.domain.instance.FormFlowStepInstance
import com.ritense.formflow.domain.instance.FormFlowStepInstanceId
import com.ritense.formflow.service.FormFlowService
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

        return ResponseEntity.ok(GetFormFlowStateResult(instance.id.id, getStepResult(stepInstance)))
    }

    @PostMapping("/{formFlowId}/step/{stepInstanceId}", produces = [MediaType.APPLICATION_JSON_VALUE])
    @Transactional
    fun completeStep(
        @PathVariable(name = "formFlowId") formFlowId: String,
        @PathVariable(name = "stepInstanceId") stepInstanceId: String,
        @RequestBody submissionData: JsonNode?
    ): ResponseEntity<CompleteStepResult> {
        val instance = formFlowService.getByInstanceIdIfExists(
            FormFlowInstanceId.existingId(UUID.fromString(formFlowId))
        )!!

        val stepInstance = instance.complete(
            FormFlowStepInstanceId.existingId(UUID.fromString(stepInstanceId)),
            JSONObject((submissionData ?: JsonNodeFactory.instance.objectNode()).toString())
        )!!

        return ResponseEntity.ok(CompleteStepResult(instance.id.id, getStepResult(stepInstance)))
    }

    private fun getStepResult(stepInstance: FormFlowStepInstance): FormFlowStepResult {
        return FormFlowStepResult(
            stepInstance.id.id,
            stepInstance.definition.type.name,
            jacksonObjectMapper().createObjectNode()
                .set("definition", jacksonObjectMapper().createObjectNode().put("firstName", "John"))
            // TODO Include the prefilled form when this functionality is available
        )
    }
}
