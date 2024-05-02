package com.ritense.formviewmodel.web.rest

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.ritense.authorization.AuthorizationService
import com.ritense.authorization.request.EntityAuthorizationRequest
import com.ritense.formviewmodel.domain.ViewModel
import com.ritense.formviewmodel.domain.factory.ViewModelLoaderFactory
import com.ritense.formviewmodel.error.FormException
import com.ritense.formviewmodel.event.OnFormSubmittedEventHandler
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
import kotlin.reflect.KClass

@RestController
@SkipComponentScan
@RequestMapping("/api/v1/form/view-model", produces = [ValtimoMediaType.APPLICATION_JSON_UTF8_VALUE])
class FormViewModelResource(
    private val viewModelLoaderFactory: ViewModelLoaderFactory,
    private val eventHandlers: List<OnFormSubmittedEventHandler<*>>,
    private val camundaTaskService: CamundaTaskService,
    private val authorizationService: AuthorizationService
) {

    @GetMapping
    fun getFormViewModel(
        @RequestParam(required = true) formId: String,
        @RequestParam(required = true) taskInstanceId: String
    ): ResponseEntity<ViewModel?> {
        return ResponseEntity.ok(
            viewModelLoaderFactory.getViewModelLoader(formId)?.onLoad(taskInstanceId)
        )
    }

    @PostMapping
    fun updateFormViewModel(
        @RequestParam(required = true) formId: String,
        @RequestParam(required = true) taskInstanceId: String,
        @RequestBody formViewModel: String
    ): ResponseEntity<ViewModel> {
        return ResponseEntity.ok(
            parseViewModel(formViewModel, viewModelLoaderFactory.getViewModelLoader(formId)?.getViewModelType()!!).update()
        )
    }

    @PostMapping("/submit")
    fun submitFormViewModel(
        @RequestParam(required = true) formId: String,
        @RequestParam(required = true) taskInstanceId: String,
        @RequestBody formViewModel: String
    ): ResponseEntity<FormError> {
        val type = viewModelLoaderFactory.getViewModelLoader(formId)?.getViewModelType()!!
        val viewModel = parseViewModel(formViewModel, type)
        val eventHandler = eventHandlers.find { it.supports(formId) }!!
        try {
            camundaTaskService.findTaskById(taskInstanceId)
            authorizationService.requirePermission(
                EntityAuthorizationRequest(
                    CamundaTask::class.java,
                    CamundaTaskActionProvider.COMPLETE,
                    camundaTaskService.findTaskById(taskInstanceId)
                )
            )
            handleViewModel(eventHandler, viewModel, taskInstanceId, type)
            return ResponseEntity.ok().build()
        } catch(e: FormException) {
            return ResponseEntity.ok(FormError(e.message!!, e.component))
        } catch(e: Exception) {
            return ResponseEntity.ok(FormError(e.message!!))
        }
    }

    private inline fun <reified T : ViewModel>parseViewModel(formViewModel: String, viewModelType: KClass<out T>): ViewModel {
        return jacksonObjectMapper().readValue(formViewModel, viewModelType.java)
    }

    fun <T : ViewModel>handleViewModel(eventHandler: OnFormSubmittedEventHandler<*>, viewModel: ViewModel, taskInstanceId: String, viewModelType: KClass<out T>) {
        val castedEventHandler = eventHandler as OnFormSubmittedEventHandler<T>
        castedEventHandler.handle(viewModel as T, taskInstanceId)
        camundaTaskService.complete(taskInstanceId)
    }
}