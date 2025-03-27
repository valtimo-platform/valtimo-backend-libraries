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
import com.ritense.authorization.AuthorizationContext.Companion.runWithoutAuthorization
import com.ritense.authorization.AuthorizationService
import com.ritense.authorization.request.EntityAuthorizationRequest
import com.ritense.document.domain.impl.JsonSchemaDocumentId
import com.ritense.document.service.impl.JsonSchemaDocumentService
import com.ritense.formviewmodel.viewmodel.ViewModel
import com.ritense.formviewmodel.viewmodel.ViewModelLoaderFactory
import com.ritense.processlink.domain.ActivityTypeWithEventName
import com.ritense.processlink.domain.ProcessLink
import com.ritense.processlink.service.ProcessLinkService
import com.ritense.valtimo.camunda.authorization.CamundaTaskActionProvider.Companion.VIEW
import com.ritense.valtimo.camunda.domain.CamundaTask
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import com.ritense.valtimo.service.CamundaTaskService
import org.springframework.stereotype.Service
import java.util.UUID
import kotlin.reflect.KClass

@Service
@SkipComponentScan
class FormViewModelService(
    private val objectMapper: ObjectMapper,
    private val viewModelLoaderFactory: ViewModelLoaderFactory,
    private val camundaTaskService: CamundaTaskService,
    private val authorizationService: AuthorizationService,
    private val processAuthorizationService: ProcessAuthorizationService,
    private val processLinkService: ProcessLinkService,
    private val documentService: JsonSchemaDocumentService,
) {

    @Deprecated("Deprecated since 12.6.0", replaceWith = ReplaceWith("getStartFormViewModel(processDefinitionKey)"))
    fun getStartFormViewModel(
        formName: String,
        processDefinitionKey: String
    ) = getStartFormViewModel(processDefinitionKey)

    fun getStartFormViewModel(
        processDefinitionKey: String,
    ): ViewModel? {
        return getStartFormViewModel(processDefinitionKey, null)
    }

    fun getStartFormViewModel(
        processDefinitionKey: String,
        documentId: UUID?,
    ): ViewModel? {
        val document = documentId?.let {
            runWithoutAuthorization {
                documentService.getDocumentBy(JsonSchemaDocumentId.existingId(documentId))
            }
        }

        processAuthorizationService.checkStartProcessAuthorization(processDefinitionKey, document)

        val processLink = runWithoutAuthorization {
             getStartEventProcessLink(processDefinitionKey)
        } ?: return null

        val modelLoader = viewModelLoaderFactory.getViewModelLoader(processLink)

        return modelLoader?.load(task = null, document = document)
    }

    @Deprecated("Deprecated since 12.6.0", replaceWith = ReplaceWith("getUserTaskFormViewModel(taskInstanceId)"))
    fun getUserTaskFormViewModel(
        formName: String,
        taskInstanceId: String
    ) = getUserTaskFormViewModel(taskInstanceId)

    fun getUserTaskFormViewModel(
        taskInstanceId: String
    ): ViewModel? {
        val task = camundaTaskService.findTaskById(taskInstanceId)
        authorizationService.requirePermission(
            EntityAuthorizationRequest(CamundaTask::class.java, VIEW, task)
        )

        val processLink = runWithoutAuthorization {
            getUserTaskProcessLink(task)
        }?: return null

        val loader = viewModelLoaderFactory.getViewModelLoader(processLink)

        return loader?.load(task = task, document = null)

    }

    @Deprecated("Deprecated since 12.6.0", replaceWith = ReplaceWith("updateStartFormViewModel(processDefinitionKey, submission, page)"))
    fun updateStartFormViewModel(
        formName: String,
        processDefinitionKey: String,
        submission: ObjectNode
    ) = updateStartFormViewModel(processDefinitionKey, submission, null)

    @Deprecated("Deprecated since 12.6.0", replaceWith = ReplaceWith("updateStartFormViewModel(processDefinitionKey, submission, page)"))
    fun updateStartFormViewModel(
        formName: String,
        processDefinitionKey: String,
        submission: ObjectNode,
        page: Int?,
        isWizard: Boolean?
    ) = updateStartFormViewModel(processDefinitionKey, submission, page)

    fun updateStartFormViewModel(
        processDefinitionKey: String,
        submission: ObjectNode,
        page: Int?,
        documentId: UUID? = null,
    ): ViewModel? {

        val document = documentId?.let {
            runWithoutAuthorization {
                documentService.getDocumentBy(JsonSchemaDocumentId.existingId(documentId))
            }
        }

        processAuthorizationService.checkStartProcessAuthorization(processDefinitionKey, document)

        val processLink =  runWithoutAuthorization {
            getStartEventProcessLink(processDefinitionKey)
        } ?: return null

        val viewModelLoader = viewModelLoaderFactory.getViewModelLoader(processLink) ?: return null
        val viewModel = parseViewModel(submission, viewModelLoader.getViewModelType())

        return viewModel.update(page = page, document = document)
    }

    @Deprecated("Deprecated since 12.6.0", replaceWith = ReplaceWith("updateStartFormViewModel(taskInstanceId, submission, page)"))
    fun updateUserTaskFormViewModel(
        formName: String,
        taskInstanceId: String,
        submission: ObjectNode
    ) = updateUserTaskFormViewModel(taskInstanceId, submission, null)

    @Deprecated("Deprecated since 12.6.0", replaceWith = ReplaceWith("updateStartFormViewModel(taskInstanceId, submission, page)"))
    fun updateUserTaskFormViewModel(
        formName: String,
        taskInstanceId: String,
        submission: ObjectNode,
        page: Int?,
        isWizard: Boolean?
    ) =  updateUserTaskFormViewModel(taskInstanceId, submission, page)

    fun updateUserTaskFormViewModel(
        taskInstanceId: String,
        submission: ObjectNode,
        page: Int?
    ): ViewModel? {
        val task = camundaTaskService.findTaskById(taskInstanceId)
        authorizationService.requirePermission(
            EntityAuthorizationRequest(CamundaTask::class.java, VIEW, task)
        )

        val processLink = runWithoutAuthorization {
            getUserTaskProcessLink(task)
        } ?: return null

        val viewModelLoader = viewModelLoaderFactory.getViewModelLoader(processLink) ?: return null
        val viewModel = parseViewModel(submission, viewModelLoader.getViewModelType())

        return viewModel.update(task = task, page = page, document = null)
    }

    fun <T : ViewModel> parseViewModel(
        submission: ObjectNode,
        viewModelType: KClass<out T>
    ): ViewModel {
        // When a field is not present in the ViewModel what then? A: it's ignored
        return objectMapper.convertValue(submission, viewModelType.java)
    }

    private fun getStartEventProcessLink(processDefinitionKey: String) = processLinkService.getProcessLinksByProcessDefinitionKey(
        processDefinitionKey
    ).firstOrNull { it.activityType === ActivityTypeWithEventName.START_EVENT_START }

    private fun getUserTaskProcessLink(task: CamundaTask): ProcessLink? {
        return task.processDefinition?.let { processDefinition ->
            processLinkService.getProcessLinksByProcessDefinitionKey(
                processDefinition.key,
            ).firstOrNull {
                it.activityType == ActivityTypeWithEventName.USER_TASK_CREATE && it.activityId == task.taskDefinitionKey
            }
        }
    }
}