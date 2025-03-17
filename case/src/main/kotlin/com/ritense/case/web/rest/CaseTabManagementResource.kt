/*
 * Copyright 2015-2025 Ritense BV, the Netherlands.
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

import com.ritense.authorization.annotation.RunWithoutAuthorization
import com.ritense.case.service.CaseTabService
import com.ritense.case.service.exception.TabAlreadyExistsException
import com.ritense.case.web.rest.dto.CaseTabDto
import com.ritense.case.web.rest.dto.CaseTabUpdateDto
import com.ritense.case.web.rest.dto.CaseTabUpdateOrderDto
import com.ritense.case.web.rest.dto.CaseTabWithMetadataDto
import com.ritense.logging.LoggableResource
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import com.ritense.valtimo.contract.authentication.UserManagementService
import com.ritense.valtimo.contract.case_.CaseDefinitionId
import com.ritense.valtimo.contract.domain.ValtimoMediaType.APPLICATION_JSON_UTF8_VALUE
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@SkipComponentScan
@RequestMapping("/api/management", produces = [APPLICATION_JSON_UTF8_VALUE])
class CaseTabManagementResource(
    private val caseTabService: CaseTabService,
    private val userManagementService: UserManagementService,
) {
    @RunWithoutAuthorization
    @PostMapping("/v1/case-definition/{caseDefinitionKey}/version/{caseDefinitionVersionTag}/tab")
    fun createCaseTab(
        @LoggableResource("caseDefinitionKey") @PathVariable caseDefinitionKey: String,
        @LoggableResource("caseDefinitionVersionTag") @PathVariable caseDefinitionVersionTag: String,
        @RequestBody caseTabDto: CaseTabDto
    ): ResponseEntity<CaseTabWithMetadataDto> {
        return try {
            val caseTab = caseTabService.createCaseTab(
                CaseDefinitionId.of(caseDefinitionKey, caseDefinitionVersionTag),
                caseTabDto
            )
            ResponseEntity.ok(CaseTabWithMetadataDto.of(caseTab, userManagementService))
        } catch (ex: TabAlreadyExistsException) {
            ResponseEntity.status(HttpStatus.CONFLICT).build()
        }
    }

    @RunWithoutAuthorization
    @PutMapping("/v1/case-definition/{caseDefinitionKey}/version/{caseDefinitionVersionTag}/tab")
    fun updateOrderCaseTab(
        @LoggableResource("caseDefinitionKey") @PathVariable caseDefinitionKey: String,
        @LoggableResource("caseDefinitionVersionTag") @PathVariable caseDefinitionVersionTag: String,
        @RequestBody caseTabDtos: List<CaseTabUpdateOrderDto>
    ): ResponseEntity<List<CaseTabWithMetadataDto>> {
        val caseTabs = caseTabService.updateCaseTabs(
            CaseDefinitionId.of(caseDefinitionKey, caseDefinitionVersionTag),
            caseTabDtos
        )
            .map { CaseTabWithMetadataDto.of(it, userManagementService) }
        return ResponseEntity.ok(caseTabs)
    }

    @RunWithoutAuthorization
    @PutMapping("/v1/case-definition/{caseDefinitionKey}/version/{caseDefinitionVersionTag}/tab/{tabKey}")
    fun updateCaseTab(
        @LoggableResource("caseDefinitionKey") @PathVariable caseDefinitionKey: String,
        @LoggableResource("caseDefinitionVersionTag") @PathVariable caseDefinitionVersionTag: String,
        @PathVariable tabKey: String,
        @RequestBody caseTab: CaseTabUpdateDto
    ): ResponseEntity<Unit> {
        caseTabService.updateCaseTab(
            CaseDefinitionId.of(caseDefinitionKey, caseDefinitionVersionTag),
            tabKey,
            caseTab
        )
        return ResponseEntity.noContent().build()
    }

    @RunWithoutAuthorization
    @DeleteMapping("/v1/case-definition/{caseDefinitionKey}/version/{caseDefinitionVersionTag}/tab/{tabKey}")
    fun deleteCaseTab(
        @LoggableResource("caseDefinitionKey") @PathVariable caseDefinitionKey: String,
        @LoggableResource("caseDefinitionVersionTag") @PathVariable caseDefinitionVersionTag: String,
        @PathVariable tabKey: String
    ): ResponseEntity<Unit> {
        caseTabService.deleteCaseTab(CaseDefinitionId.of(caseDefinitionKey, caseDefinitionVersionTag), tabKey)
        return ResponseEntity.noContent().build()
    }

    @RunWithoutAuthorization
    @GetMapping("/v1/case-definition/{caseDefinitionKey}/version/{caseDefinitionVersionTag}/tab")
    fun getCaseTabs(
        @LoggableResource("caseDefinitionName") @PathVariable caseDefinitionKey: String,
        @LoggableResource("caseDefinitionVersionTag") @PathVariable caseDefinitionVersionTag: String,
    ): ResponseEntity<List<CaseTabWithMetadataDto>> {
        val caseTabs = caseTabService.getCaseTabs(CaseDefinitionId.of(caseDefinitionKey, caseDefinitionVersionTag))
            .map { CaseTabWithMetadataDto.of(it, userManagementService) }
        return ResponseEntity.ok(caseTabs)
    }

    @RunWithoutAuthorization
    @GetMapping("/v1/case-definition/{caseDefinitionKey}/version/{caseDefinitionVersionTag}/tab/{tabKey}")
    fun getCaseTab(
        @LoggableResource("caseDefinitionKey") @PathVariable caseDefinitionKey: String,
        @LoggableResource("caseDefinitionVersionTag") @PathVariable caseDefinitionVersionTag: String,
        @PathVariable tabKey: String
    ): ResponseEntity<CaseTabWithMetadataDto> {
        val caseTab =
            caseTabService.getCaseTab(CaseDefinitionId.of(caseDefinitionKey, caseDefinitionVersionTag), tabKey)
        return ResponseEntity.ok(CaseTabWithMetadataDto.of(caseTab, userManagementService))
    }
}
