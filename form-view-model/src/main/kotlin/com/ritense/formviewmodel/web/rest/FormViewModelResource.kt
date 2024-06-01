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
import com.ritense.authorization.AuthorizationService
import com.ritense.authorization.request.EntityAuthorizationRequest
import com.ritense.formviewmodel.service.FormViewModelService
import com.ritense.formviewmodel.service.FormViewModelSubmissionService
import com.ritense.formviewmodel.viewmodel.ViewModel
import com.ritense.formviewmodel.viewmodel.ViewModelLoaderFactory
import com.ritense.valtimo.camunda.authorization.CamundaTaskActionProvider.Companion.COMPLETE
import com.ritense.valtimo.camunda.authorization.CamundaTaskActionProvider.Companion.VIEW
import com.ritense.valtimo.camunda.domain.CamundaTask
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import com.ritense.valtimo.contract.domain.ValtimoMediaType.APPLICATION_JSON_UTF8_VALUE
import com.ritense.valtimo.service.CamundaTaskService
import org.springframework.http.ResponseEntity
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@SkipComponentScan
@RequestMapping("/api/v1/form/view-model", produces = [APPLICATION_JSON_UTF8_VALUE])
@Transactional
class FormViewModelResource(
    private val formViewModelService: FormViewModelService
) {

    @GetMapping("/start-form")
    fun getStartFormViewModel(
        @RequestParam formName: String
    ): ResponseEntity<ViewModel?> {
        val viewModel = viewModelLoaderFactory.getViewModelLoader(formName)?.load()
        return if (viewModel != null) {
            ResponseEntity.ok(viewModel)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @GetMapping("/user-task")
    fun getUserTaskFormViewModel(
        @RequestParam formName: String,
        @RequestParam taskInstanceId: String
    ): ResponseEntity<ViewModel?> {
        val task = taskInstanceId.let {
            camundaTaskService.findTaskById(it).also { foundTask ->
                authorizationService.requirePermission(
                    EntityAuthorizationRequest(CamundaTask::class.java, VIEW, foundTask)
                )
            }
        }
        val viewModel = viewModelLoaderFactory.getViewModelLoader(formName)?.load(task)
        return if (viewModel != null) {
            ResponseEntity.ok(viewModel)
        } else {
            ResponseEntity.notFound().build()
        }
        return formViewModelService.getFormViewModel(
            formName = formName,
            taskInstanceId = taskInstanceId
        )?.let {
            ResponseEntity.ok(it)
        } ?: ResponseEntity.notFound().build()
    }

    @PostMapping("/start-form")
    fun updateStartFormViewModel(
        @RequestParam formName: String,
        @RequestBody submission: ObjectNode
    ): ResponseEntity<ViewModel> {
        return formViewModelService.updateViewModel(
            formName = formName,
            taskInstanceId = taskInstanceId,
            submission = submission
        )?.let {
            ResponseEntity.ok(it)
        } ?: ResponseEntity.notFound().build()
        val viewModelLoader =
            viewModelLoaderFactory.getViewModelLoader(formName) ?: return ResponseEntity.notFound().build()
        val viewModelType = viewModelLoader.getViewModelType()
        val updatedViewModel = formViewModelService.parseViewModel(submission, viewModelType).update()
        return ResponseEntity.ok(updatedViewModel)
    }

    @PostMapping("/user-task")
    fun updateUserTaskFormViewModel(
        @RequestParam formName: String,
        @RequestParam taskInstanceId: String,
        @RequestBody submission: ObjectNode
    ): ResponseEntity<ViewModel> {
        val task = taskInstanceId.let {
            camundaTaskService.findTaskById(it).also { foundTask ->
                authorizationService.requirePermission(
                    EntityAuthorizationRequest(CamundaTask::class.java, VIEW, foundTask)
                )
            }
        }
        val viewModelLoader =
            viewModelLoaderFactory.getViewModelLoader(formName) ?: return ResponseEntity.notFound().build()
        val viewModelType = viewModelLoader.getViewModelType()
        val updatedViewModel = formViewModelService.parseViewModel(submission, viewModelType).update(task)
        return ResponseEntity.ok(updatedViewModel)
    }

    @PostMapping("/submit/user-task")
    fun submitTask(
        @RequestParam formName: String,
        @RequestParam taskInstanceId: String,
        @RequestBody submission: ObjectNode
    ): ResponseEntity<Void> {
        formViewModelService.submit(
        val task = camundaTaskService.findTaskById(taskInstanceId)
        authorizationService.requirePermission(
            EntityAuthorizationRequest(CamundaTask::class.java, COMPLETE, task)
        )
        formViewModelSubmissionService.handleUserTaskSubmission(
            formName = formName,
            taskInstanceId = taskInstanceId,
            submission = submission
        )
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/submit/start-form")
    @Transactional
    fun submitStartForm(
        @RequestParam formName: String,
        @RequestParam processDefinitionKey: String,
        @RequestBody submission: ObjectNode
    ): ResponseEntity<Void> {
        formViewModelSubmissionService.handleStartFormSubmission(
            formName = formName,
            processDefinitionKey = processDefinitionKey,
            submission = submission,
        )
        return ResponseEntity.noContent().build()
    }

}