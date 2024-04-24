package com.ritense.formviewmodel.web.rest

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

@RestController
@SkipComponentScan
@RequestMapping("/api/v1/form/view-model", produces = [ValtimoMediaType.APPLICATION_JSON_UTF8_VALUE])
class FormViewModelResource {

    @GetMapping
    fun getFormViewModel(
        @RequestParam(required = true) formId: String,
        @RequestParam(required = true) taskInstanceId: String
    ): ResponseEntity<ViewModel?> {
        return ResponseEntity.ok(
            ViewModelLoaderFactory().getViewModelLoader(formId)?.onLoad(taskInstanceId)
        )
    }

    @PostMapping
    fun updateFormViewModel(
        @RequestParam(required = true) formId: String,
        @RequestParam(required = true) taskInstanceId: String,
        @RequestBody formViewModel: ViewModel
    ): ResponseEntity<ViewModel> {
        return ResponseEntity.ok(
            ViewModelLoaderFactory().getViewModelLoader(formId)?.onLoad(taskInstanceId)?.update(formViewModel)
        )
    }
}