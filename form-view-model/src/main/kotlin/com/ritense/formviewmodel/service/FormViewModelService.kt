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

package com.ritense.formviewmodel.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.ritense.authorization.AuthorizationService
import com.ritense.authorization.request.EntityAuthorizationRequest
import com.ritense.formviewmodel.viewmodel.ViewModel
import com.ritense.formviewmodel.viewmodel.ViewModelLoaderFactory
import com.ritense.valtimo.camunda.authorization.CamundaTaskActionProvider
import com.ritense.valtimo.camunda.domain.CamundaTask
import com.ritense.valtimo.service.CamundaTaskService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import kotlin.reflect.KClass

class FormViewModelService(
    val objectMapper: ObjectMapper,
    private val viewModelLoaderFactory: ViewModelLoaderFactory,
    private val camundaTaskService: CamundaTaskService,
    private val authorizationService: AuthorizationService,
    private val formViewModelSubmissionService: FormViewModelSubmissionService
) {
    fun getFormViewModel(
        formName: String,
        taskInstanceId: String
    ): ViewModel? {
        val task = camundaTaskService.findTaskById(taskInstanceId)
        authorizationService.requirePermission(
            EntityAuthorizationRequest(CamundaTask::class.java, CamundaTaskActionProvider.VIEW, task)
        )
        return viewModelLoaderFactory.getViewModelLoader(formName)?.load(task)
    }

    fun updateViewModel(
        formName: String,
        taskInstanceId: String,
        submission: ObjectNode
    ) : ViewModel? {
        val task = camundaTaskService.findTaskById(taskInstanceId)
        authorizationService.requirePermission(
            EntityAuthorizationRequest(CamundaTask::class.java, CamundaTaskActionProvider.VIEW, task)
        )
        val viewModelLoader =
            viewModelLoaderFactory.getViewModelLoader(formName) ?: return null
        val viewModelType = viewModelLoader.getViewModelType()
        return parseViewModel(submission, viewModelType).update(task)
    }

    fun submit(
        formName: String,
        taskInstanceId: String,
        submission: ObjectNode
    ) {
        val task = camundaTaskService.findTaskById(taskInstanceId)
        authorizationService.requirePermission(
            EntityAuthorizationRequest(CamundaTask::class.java, CamundaTaskActionProvider.COMPLETE, task)
        )
        formViewModelSubmissionService.handleSubmission(
            formName = formName,
            submission = submission,
            task = task
        )
    }

    inline fun <reified T : ViewModel> parseViewModel(
        submission: ObjectNode,
        viewModelType: KClass<out T>
    ): ViewModel {
        // When a field is not present in the ViewModel what then? A: it's ignored
        return objectMapper.convertValue(submission, viewModelType.java)
    }

}