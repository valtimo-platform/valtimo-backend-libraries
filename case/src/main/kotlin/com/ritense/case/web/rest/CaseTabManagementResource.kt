/*
 * Copyright 2015-2023 Ritense BV, the Netherlands.
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

package com.ritense.case.web.rest

import com.ritense.authorization.AuthorizationContext.Companion.runWithoutAuthorization
import com.ritense.authorization.annotation.RunWithoutAuthorization
import com.ritense.case.service.CaseTabService
import com.ritense.case.web.rest.dto.CaseTabDto
import com.ritense.case.web.rest.dto.CaseTabUpdateDto
import com.ritense.case.web.rest.dto.CaseTabUpdateOrderDto
import com.ritense.valtimo.contract.domain.ValtimoMediaType.APPLICATION_JSON_UTF8_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping

@RequestMapping("/api/management", produces = [APPLICATION_JSON_UTF8_VALUE])
open class CaseTabManagementResource(
    private val caseTabService: CaseTabService
) {
    @RunWithoutAuthorization
    @PostMapping("/v1/case-definition/{caseDefinitionName}/tab")
    open fun createCaseTab(
        @PathVariable caseDefinitionName: String,
        @RequestBody caseTab: CaseTabDto
    ): ResponseEntity<CaseTabDto> {
        return ResponseEntity.ok(caseTabService.createCaseTab(caseDefinitionName, caseTab))
    }

    @RunWithoutAuthorization
    @PutMapping("/v1/case-definition/{caseDefinitionName}/tab")
    open fun updateOrderCaseTab(
        @PathVariable caseDefinitionName: String,
        @RequestBody caseTabDtos: List<CaseTabUpdateOrderDto>
    ): ResponseEntity<List<CaseTabDto>> {
        val caseTabs = caseTabService.updateCaseTabs(caseDefinitionName, caseTabDtos)
            .map { CaseTabDto.of(it) }
        return ResponseEntity.ok(caseTabs)
    }

    @RunWithoutAuthorization
    @PutMapping("/v1/case-definition/{caseDefinitionName}/tab/{tabKey}")
    open fun updateCaseTab(
        @PathVariable caseDefinitionName: String,
        @PathVariable tabKey: String,
        @RequestBody caseTab: CaseTabUpdateDto
    ): ResponseEntity<Unit> {
        caseTabService.updateCaseTab(caseDefinitionName, tabKey, caseTab)
        return ResponseEntity.noContent().build()
    }

    @RunWithoutAuthorization
    @DeleteMapping("/v1/case-definition/{caseDefinitionName}/tab/{tabKey}")
    open fun deleteCaseTab(
        @PathVariable caseDefinitionName: String,
        @PathVariable tabKey: String
    ): ResponseEntity<Unit> {
        caseTabService.deleteCaseTab(caseDefinitionName, tabKey)
        return ResponseEntity.noContent().build()
    }

    @RunWithoutAuthorization
    @GetMapping("/v1/case-definition/{caseDefinitionName}/tab")
    open fun getCaseTabs(
        @PathVariable caseDefinitionName: String
    ): ResponseEntity<List<CaseTabDto>> {
        val caseTabs = caseTabService.getCaseTabs(caseDefinitionName).map { CaseTabDto.of(it) }
        return ResponseEntity.ok(caseTabs)
    }
}
