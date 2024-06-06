package com.ritense.form.web.rest

import com.ritense.form.service.IntermediateSubmissionService
import com.ritense.form.web.rest.dto.IntermediateSaveRequest
import com.ritense.form.web.rest.dto.IntermediateSubmission
import com.ritense.form.web.rest.dto.toResponse
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import com.ritense.valtimo.contract.domain.ValtimoMediaType.APPLICATION_JSON_UTF8_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@SkipComponentScan
@RequestMapping(value = ["/api/v1/form/intermediate/submission"], produces = [APPLICATION_JSON_UTF8_VALUE])
@Transactional
class IntermediateSubmissionResource(
    private val intermediateSubmissionService: IntermediateSubmissionService
) {

    @GetMapping
    fun getIntermediateSubmission(@RequestParam taskInstanceId: String): ResponseEntity<IntermediateSubmission> {
        val intermediateSubmission = intermediateSubmissionService.get(taskInstanceId)
        return intermediateSubmission?.let { ResponseEntity.ok(it.toResponse()) } ?: ResponseEntity.notFound().build()
    }

    @PostMapping
    fun storeIntermediateSubmission(@RequestBody request: IntermediateSaveRequest): ResponseEntity<IntermediateSubmission> {
        val intermediateSubmission = intermediateSubmissionService.store(
            submission = request.submission,
            taskInstanceId = request.taskInstanceId
        )
        return ResponseEntity.ok(intermediateSubmission.toResponse())
    }

    @DeleteMapping
    fun clearIntermediateSubmission(@RequestParam taskInstanceId: String): ResponseEntity<Void> {
        intermediateSubmissionService.clear(
            taskInstanceId = taskInstanceId
        )
        return ResponseEntity.ok().build()
    }

}