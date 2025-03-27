/*
 * Copyright 2015-2024 Ritense BV, the Netherlands.
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

package com.ritense.formviewmodel.web.rest

import com.fasterxml.jackson.databind.node.ObjectNode
import com.ritense.formviewmodel.service.FormViewModelService
import com.ritense.formviewmodel.service.FormViewModelSubmissionService
import com.ritense.formviewmodel.viewmodel.ViewModel
import com.ritense.formviewmodel.web.rest.dto.StartFormSubmissionResult
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import com.ritense.valtimo.contract.domain.ValtimoMediaType.APPLICATION_JSON_UTF8_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@SkipComponentScan
@RequestMapping("/api/v1/form/view-model", produces = [APPLICATION_JSON_UTF8_VALUE])
@Transactional
class FormViewModelResource(
    private val formViewModelService: FormViewModelService,
    private val formViewModelSubmissionService: FormViewModelSubmissionService
) {

    @GetMapping("/start-form")
    fun getStartFormViewModel(
        @RequestParam processDefinitionKey: String,
        @RequestParam(required = false) documentId: UUID?
    ): ResponseEntity<ViewModel?> {
        val viewModel = formViewModelService.getStartFormViewModel(
            processDefinitionKey = processDefinitionKey,
            documentId = documentId
        )
        return if (viewModel != null) {
            ResponseEntity.ok(viewModel)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @GetMapping("/user-task")
    fun getUserTaskFormViewModel(
        @RequestParam taskInstanceId: String
    ): ResponseEntity<ViewModel?> {
        return formViewModelService.getUserTaskFormViewModel(
            taskInstanceId = taskInstanceId,
        )?.let {
            ResponseEntity.ok(it)
        } ?: ResponseEntity.notFound().build()
    }

    @PostMapping("/start-form")
    fun updateStartFormViewModel(
        @RequestParam processDefinitionKey: String,
        @RequestParam(required = false) page: Int? = null,
        @RequestParam(required = false) documentId: UUID? = null,
        @RequestBody submission: ObjectNode
    ): ResponseEntity<ViewModel> {
        return formViewModelService.updateStartFormViewModel(
            processDefinitionKey = processDefinitionKey,
            submission = submission,
            page = page,
            documentId = documentId,
        )?.let {
            ResponseEntity.ok(it)
        } ?: ResponseEntity.notFound().build()
    }

    @PostMapping("/user-task")
    fun updateUserTaskFormViewModel(
        @RequestParam taskInstanceId: String,
        @RequestParam(required = false) page: Int? = null,
        @RequestBody submission: ObjectNode,
    ): ResponseEntity<ViewModel> {
        return formViewModelService.updateUserTaskFormViewModel(
            taskInstanceId = taskInstanceId,
            page = page,
            submission = submission
        )?.let {
            ResponseEntity.ok(it)
        } ?: ResponseEntity.notFound().build()
    }

    @PostMapping("/submit/user-task")
    fun submitTask(
        @RequestParam taskInstanceId: String,
        @RequestBody submission: ObjectNode
    ): ResponseEntity<Void> {
        formViewModelSubmissionService.handleUserTaskSubmission(
            taskInstanceId = taskInstanceId,
            submission = submission
        )
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/submit/start-form")
    fun submitStartForm(
        @RequestParam processDefinitionKey: String,
        @RequestParam documentDefinitionName: String,
        @RequestParam(required = false) documentId: UUID?,
        @RequestBody submission: ObjectNode
    ): ResponseEntity<StartFormSubmissionResult> {
        val result = formViewModelSubmissionService.handleStartFormSubmission(
            processDefinitionKey = processDefinitionKey,
            documentDefinitionName = documentDefinitionName,
            submission = submission,
            documentId = documentId,
        )
        return ResponseEntity.ok(result)
    }

}