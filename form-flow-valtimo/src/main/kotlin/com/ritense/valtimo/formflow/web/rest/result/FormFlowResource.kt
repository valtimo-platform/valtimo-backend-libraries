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

package com.ritense.valtimo.formflow.web.rest.result

import com.fasterxml.jackson.databind.JsonNode
import com.ritense.form.service.FormDefinitionService
import com.ritense.valtimo.formflow.web.rest.dto.FormFlow
import com.ritense.valtimo.formflow.web.rest.dto.FormFlowStep
import com.ritense.valtimo.formflow.web.rest.dto.FormTypeProperties
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping(value = ["/api/form-flow"])
class FormFlowResource(
    val formDefinitionService: FormDefinitionService
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