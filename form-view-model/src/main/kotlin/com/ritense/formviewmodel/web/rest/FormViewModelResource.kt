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
import com.ritense.formviewmodel.error.FormException
import com.ritense.formviewmodel.service.FormViewModelService
import com.ritense.formviewmodel.service.FormViewModelSubmissionService
import com.ritense.formviewmodel.viewmodel.ViewModel
import com.ritense.formviewmodel.viewmodel.ViewModelLoaderFactory
import com.ritense.formviewmodel.web.rest.dto.FormError
import com.ritense.valtimo.camunda.authorization.CamundaTaskActionProvider
import com.ritense.valtimo.camunda.domain.CamundaTask
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import com.ritense.valtimo.contract.domain.ValtimoMediaType
import com.ritense.valtimo.service.CamundaTaskService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@SkipComponentScan
@RequestMapping("/api/v1/form/view-model", produces = [ValtimoMediaType.APPLICATION_JSON_UTF8_VALUE])
class FormViewModelResource(
    private val viewModelLoaderFactory: ViewModelLoaderFactory,
    private val camundaTaskService: CamundaTaskService,
    private val authorizationService: AuthorizationService,
    private val formViewModelService: FormViewModelService,
    private val formViewModelSubmissionService: FormViewModelSubmissionService
) {

    @GetMapping
    fun getFormViewModel(
        @RequestParam(required = true) formName: String,
        @RequestParam(required = true) taskInstanceId: String
    ): ResponseEntity<ViewModel?> {
        return ResponseEntity.ok(
            viewModelLoaderFactory.getViewModelLoader(formName)?.load(taskInstanceId)
        )
    }

    @PostMapping
    fun updateFormViewModel(
        @RequestParam(required = true) formName: String,
        @RequestParam(required = true) taskInstanceId: String,
        @RequestBody submission: ObjectNode
    ): ResponseEntity<ViewModel> {
        val viewModelType = viewModelLoaderFactory.getViewModelLoader(formName)?.getViewModelType()!!
        return ResponseEntity.ok(
            formViewModelService.parseViewModel(
                submission,
                viewModelType
            ).update()
        )
    }

    @PostMapping("/submit")
    fun submitFormViewModel(
        @RequestParam(required = true) formName: String,
        @RequestParam(required = true) taskInstanceId: String,
        @RequestBody submission: ObjectNode
    ): ResponseEntity<FormError?> {
        try {
            val task = camundaTaskService.findTaskById(taskInstanceId)
            authorizationService.requirePermission(
                EntityAuthorizationRequest(
                    CamundaTask::class.java,
                    CamundaTaskActionProvider.COMPLETE,
                    task
                )
            )
            formViewModelSubmissionService.handleSubmission(
                formName = formName,
                submission = submission,
                task = task
            )
            return ResponseEntity.ok().build()
        } catch (ex: FormException) {
            return ResponseEntity.ok(FormError(ex.message!!, ex.component))
        } catch (ex: Exception) {
            return ResponseEntity.ok(FormError(ex.message!!))
        }
    }

}