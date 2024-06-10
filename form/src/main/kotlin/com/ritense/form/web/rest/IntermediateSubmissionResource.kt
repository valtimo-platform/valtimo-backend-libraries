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