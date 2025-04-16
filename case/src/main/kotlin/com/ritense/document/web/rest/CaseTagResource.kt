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
import com.ritense.document.exception.CaseTagInUseException
import com.ritense.document.service.CaseTagService
import com.ritense.document.web.rest.dto.CaseTagCreateRequestDto
import com.ritense.document.web.rest.dto.CaseTagResponseDto
import com.ritense.document.web.rest.dto.CaseTagUpdateRequestDto
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
import org.springframework.web.server.ResponseStatusException

@RestController
@SkipComponentScan
@RequestMapping("/api", produces = [ValtimoMediaType.APPLICATION_JSON_UTF8_VALUE])
class CaseTagResource(
    private val caseTagService: CaseTagService
) {

    @GetMapping("/v1/case-definition/{caseDefinitionName}/case-tag")
    fun getCaseTags(
        @LoggableResource("documentDefinitionName") @PathVariable caseDefinitionName: String
    ): ResponseEntity<List<CaseTagResponseDto>> {
        val caseTags = caseTagService.getCaseTags(caseDefinitionName)
        return ResponseEntity.ok(caseTags.map { CaseTagResponseDto(it) }.sortedBy { it.order })
    }

    @RunWithoutAuthorization
    @GetMapping("/management/v1/case-definition/{caseDefinitionName}/case-tag")
    fun getCaseTagForManagement(
        @LoggableResource("documentDefinitionName") @PathVariable caseDefinitionName: String
    ): ResponseEntity<List<CaseTagResponseDto>> {
        val caseTags = caseTagService.getCaseTags(caseDefinitionName)
        return ResponseEntity.ok(caseTags.map { CaseTagResponseDto(it) }.sortedBy { it.order })
    }

    @RunWithoutAuthorization
    @PostMapping("/management/v1/case-definition/{caseDefinitionName}/case-tag")
    fun createCaseTag(
        @LoggableResource("documentDefinitionName") @PathVariable caseDefinitionName: String,
        @Valid @RequestBody caseTagCreateRequestDto: CaseTagCreateRequestDto
    ): ResponseEntity<CaseTagResponseDto> {
        return try {
            ResponseEntity.ok(
                CaseTagResponseDto(
                    caseTagService.create(
                        caseDefinitionName,
                        caseTagCreateRequestDto
                    )
                )
            )
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.CONFLICT).build()
        }
    }

    @RunWithoutAuthorization
    @PutMapping("/management/v1/case-definition/{caseDefinitionName}/case-tag")
    fun editCaseTags(
        @LoggableResource("documentDefinitionName") @PathVariable caseDefinitionName: String,
        @Valid @RequestBody requestDtos: List<CaseTagUpdateRequestDto>
    ): ResponseEntity<List<CaseTagResponseDto>> {
        val caseTags = caseTagService.update(caseDefinitionName, requestDtos)
        return ResponseEntity.ok(caseTags.map { CaseTagResponseDto(it) }.sortedBy { it.order })
    }

    @RunWithoutAuthorization
    @PutMapping("/management/v1/case-definition/{caseDefinitionName}/case-tag/{caseTagKey}")
    fun updateCaseTag(
        @LoggableResource("documentDefinitionName") @PathVariable caseDefinitionName: String,
        @PathVariable caseTagKey: String,
        @Valid @RequestBody requestDto: CaseTagUpdateRequestDto
    ): ResponseEntity<Unit> {
        caseTagService.update(caseDefinitionName, caseTagKey, requestDto)
        return ResponseEntity.noContent().build()
    }

    @RunWithoutAuthorization
    @DeleteMapping("/management/v1/case-definition/{caseDefinitionName}/case-tag/{caseTagKey}")
    fun deleteCaseTag(
        @LoggableResource("documentDefinitionName") @PathVariable caseDefinitionName: String,
        @PathVariable caseTagKey: String,
    ) {
        try {
            caseTagService.delete(caseDefinitionName, caseTagKey)
        } catch (ex: CaseTagInUseException) {
            throw ResponseStatusException(
                HttpStatus.CONFLICT,
                ex.message
            )
        }
    }
}