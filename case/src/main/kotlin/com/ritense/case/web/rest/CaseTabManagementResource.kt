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
import com.ritense.case.service.CaseTabService
import com.ritense.case.web.rest.dto.CaseTabDto
import com.ritense.case.web.rest.dto.CaseTabUpdateDto
import com.ritense.valtimo.contract.domain.ValtimoMediaType.APPLICATION_JSON_UTF8_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping

@RequestMapping("/api/management", produces = [APPLICATION_JSON_UTF8_VALUE])
class CaseTabManagementResource(
    private val caseTabService: CaseTabService
) {

    @PostMapping("/v1/case-definition/{caseDefinitionName}/tab")
    fun createCaseTab(
        @PathVariable caseDefinitionName: String,
        @RequestBody caseTab: CaseTabDto
    ): ResponseEntity<CaseTabDto> {
        return ResponseEntity.ok(runWithoutAuthorization {
            caseTabService.createCaseTab(caseDefinitionName, caseTab)
        })
    }

    @PutMapping("/v1/case-definition/{caseDefinitionName}/tab/{tabKey}")
    fun updateCaseTab(
        @PathVariable caseDefinitionName: String,
        @PathVariable tabKey: String,
        @RequestBody caseTab: CaseTabUpdateDto
    ): ResponseEntity<Unit> {
        runWithoutAuthorization { caseTabService.updateCaseTab(caseDefinitionName, tabKey, caseTab) }
        return ResponseEntity.noContent().build()
    }

    @DeleteMapping("/v1/case-definition/{caseDefinitionName}/tab/{tabKey}")
    fun deleteCaseTab(
        @PathVariable caseDefinitionName: String,
        @PathVariable tabKey: String
    ): ResponseEntity<Unit> {
        runWithoutAuthorization { caseTabService.deleteCaseTab(caseDefinitionName, tabKey) }
        return ResponseEntity.noContent().build()
    }

}
