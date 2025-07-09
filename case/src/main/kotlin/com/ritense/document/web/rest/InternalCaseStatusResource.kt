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

package com.ritense.document.web.rest

import com.ritense.authorization.annotation.RunWithoutAuthorization
import com.ritense.document.service.InternalCaseStatusService
import com.ritense.document.web.rest.dto.InternalCaseStatusCreateRequestDto
import com.ritense.document.web.rest.dto.InternalCaseStatusResponseDto
import com.ritense.document.web.rest.dto.InternalCaseStatusUpdateOrderRequestDto
import com.ritense.document.web.rest.dto.InternalCaseStatusUpdateRequestDto
import com.ritense.logging.LoggableResource
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import com.ritense.valtimo.contract.domain.ValtimoMediaType
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@SkipComponentScan
@RequestMapping("/api", produces = [ValtimoMediaType.APPLICATION_JSON_UTF8_VALUE])
class InternalCaseStatusResource(
    private val internalCaseStatusService: InternalCaseStatusService
) {

    @GetMapping("/v1/case-definition/{caseDefinitionName}/internal-status")
    fun getInternalCaseStatuses(
        @LoggableResource("documentDefinitionName") @PathVariable caseDefinitionName: String
    ): ResponseEntity<List<InternalCaseStatusResponseDto>> {
        val internalCaseStatuses = internalCaseStatusService.getInternalCaseStatuses(caseDefinitionName)
        return ResponseEntity.ok(internalCaseStatuses.map { InternalCaseStatusResponseDto(it) })
    }

    @RunWithoutAuthorization
    @GetMapping("/management/v1/case-definition/{caseDefinitionName}/internal-status")
    fun getInternalCaseStatusesForManagement(
        @LoggableResource("documentDefinitionName") @PathVariable caseDefinitionName: String
    ): ResponseEntity<List<InternalCaseStatusResponseDto>> {
        val internalCaseStatuses = internalCaseStatusService.getInternalCaseStatuses(caseDefinitionName)
        return ResponseEntity.ok(internalCaseStatuses.map { InternalCaseStatusResponseDto(it) })
    }

    @RunWithoutAuthorization
    @PostMapping("/management/v1/case-definition/{caseDefinitionName}/internal-status")
    fun createInternalCaseStatus(
        @LoggableResource("documentDefinitionName") @PathVariable caseDefinitionName: String,
        @Valid @RequestBody internalCaseStatusCreateRequest: InternalCaseStatusCreateRequestDto
    ): ResponseEntity<InternalCaseStatusResponseDto> {
        return try {
            ResponseEntity.ok(
                    InternalCaseStatusResponseDto(internalCaseStatusService.create(
                        caseDefinitionName,
                        internalCaseStatusCreateRequest
                    )
                )
            )
        } catch(e: Exception) {
            ResponseEntity.status(HttpStatus.CONFLICT).build()
        }
    }

    @RunWithoutAuthorization
    @PutMapping("/management/v1/case-definition/{caseDefinitionName}/internal-status")
    fun editInternalCaseStatuses(
        @LoggableResource("documentDefinitionName") @PathVariable caseDefinitionName: String,
        @Valid @RequestBody requestDtos: List<InternalCaseStatusUpdateOrderRequestDto>
    ): ResponseEntity<List<InternalCaseStatusResponseDto>> {
        val internalCaseStatuses = internalCaseStatusService.update(caseDefinitionName, requestDtos)
        return ResponseEntity.ok(internalCaseStatuses.map { InternalCaseStatusResponseDto(it) })
    }

    @RunWithoutAuthorization
    @PutMapping("/management/v1/case-definition/{caseDefinitionName}/internal-status/{internalStatusKey}")
    fun updateInternalCaseStatus(
        @LoggableResource("documentDefinitionName") @PathVariable caseDefinitionName: String,
        @PathVariable internalStatusKey: String,
        @Valid @RequestBody requestDto: InternalCaseStatusUpdateRequestDto
    ): ResponseEntity<Unit> {
        internalCaseStatusService.update(caseDefinitionName, internalStatusKey, requestDto)
        return ResponseEntity.noContent().build()
    }

    @RunWithoutAuthorization
    @DeleteMapping("/management/v1/case-definition/{caseDefinitionName}/internal-status/{internalStatusKey}")
    fun deleteInternalCaseStatus(
        @LoggableResource("documentDefinitionName") @PathVariable caseDefinitionName: String,
        @PathVariable internalStatusKey: String,
    ) {
        internalCaseStatusService.delete(caseDefinitionName, internalStatusKey)
    }
}