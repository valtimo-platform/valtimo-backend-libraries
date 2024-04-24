package com.ritense.formviewmodel.web.rest

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.ritense.formviewmodel.domain.ViewModel
import com.ritense.formviewmodel.domain.factory.ViewModelLoaderFactory
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import com.ritense.valtimo.contract.domain.ValtimoMediaType
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
    private val viewModelLoaderFactory: ViewModelLoaderFactory
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
        val castedViewModel = castViewModel(formViewModel, viewModelLoaderFactory.getViewModelLoader(formId)?.getViewModelType()!!)
        return ResponseEntity.ok(
            castedViewModel.update(castedViewModel)
        )
    }

    private inline fun <reified T : ViewModel>castViewModel(formViewModel: String, viewModelType: KClass<out T>): ViewModel {
        return jacksonObjectMapper().readValue(formViewModel, viewModelType.java)
    }
}