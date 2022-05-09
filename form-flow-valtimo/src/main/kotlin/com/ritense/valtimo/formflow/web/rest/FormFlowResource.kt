package com.ritense.valtimo.formflow.web.rest

import com.ritense.formflow.domain.instance.FormFlowInstanceId
import com.ritense.formflow.service.FormFlowService
import com.ritense.valtimo.formflow.web.rest.result.FormFlowStepResult
import com.ritense.valtimo.formflow.web.rest.result.GetFormFlowStateResult
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID
import javax.transaction.Transactional

@RestController
@RequestMapping(value = ["/api/form-flow"])
class FormFlowResource(
    private val formFlowService: FormFlowService
) {

    @GetMapping("/{formFlowInstanceId}", produces = [MediaType.APPLICATION_JSON_VALUE])
    @Transactional
    fun getFormFlowState(
        @PathVariable(name = "formFlowInstanceId") instanceId: String,
    ): ResponseEntity<GetFormFlowStateResult> {
        val instance = formFlowService.getInstanceById(
            FormFlowInstanceId.existingId(UUID.fromString(instanceId))
        )

        val stepInstance = instance.getCurrentStep()

        return ResponseEntity.ok(
            GetFormFlowStateResult(
                instance.id.id,
                FormFlowStepResult(
                    stepInstance.id.id,
                    stepInstance.definition.type.name,
                    stepInstance.definition.type.properties
                )
            )
        )
    }
}