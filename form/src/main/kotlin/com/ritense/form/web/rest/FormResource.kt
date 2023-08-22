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

package com.ritense.form.web.rest

import com.fasterxml.jackson.databind.JsonNode
import com.ritense.form.service.FormDefinitionService
import com.ritense.form.service.FormSubmissionService
import com.ritense.form.service.PrefillFormService
import com.ritense.form.web.rest.dto.FormSubmissionResult
import com.ritense.valtimo.contract.domain.ValtimoMediaType.APPLICATION_JSON_UTF8_VALUE
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api", produces = [APPLICATION_JSON_UTF8_VALUE])
class FormResource(
    private val formSubmissionService: FormSubmissionService,
    private val prefillFormService: PrefillFormService,
    private val formDefinitionService: FormDefinitionService,
) {

    @PostMapping("/v1/process-link/{processLinkId}/form/submission")
    fun handleSubmission(
        @PathVariable processLinkId: UUID,
        @RequestParam(required = false) documentDefinitionName: String?,
        @RequestParam(required = false) documentId: String?,
        @RequestParam(required = false) taskInstanceId: String?,
        @RequestBody submission: JsonNode
    ): ResponseEntity<FormSubmissionResult> {
        return applyResult(
            formSubmissionService.handleSubmission(
                processLinkId,
                submission,
                documentDefinitionName,
                documentId,
                taskInstanceId,
            )
        )
    }

    @GetMapping("/v1/process-link/form-definition/{formKey}")
    fun getFormDefinitionByFormKey(
        @PathVariable formKey: String,
        @RequestParam(required = false) documentId: UUID?,
    ): ResponseEntity<JsonNode> {
        val formDefinition = formDefinitionService.getFormDefinitionByName(formKey).orElse(null)

        return if (formDefinition != null) {
            ResponseEntity.ok(
                prefillFormService.getPrefilledFormDefinition(formDefinition.id, documentId).formDefinition
            )
        } else {
            ResponseEntity.notFound().build()
        }
    }

    fun <T : FormSubmissionResult?> applyResult(result: T): ResponseEntity<T> {
        val httpStatus = if (result!!.errors().isEmpty()) HttpStatus.OK else HttpStatus.BAD_REQUEST
        return ResponseEntity.status(httpStatus).body(result)
    }
}
