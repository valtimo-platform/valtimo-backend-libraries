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

package com.ritense.case.web.rest

import com.ritense.case.service.CaseInstanceService
import com.ritense.case.web.rest.dto.CaseListRowDto
import com.ritense.case.web.rest.dto.CaseDefinitionQuickSearchDto
import com.ritense.document.domain.search.SearchWithConfigRequest
import com.ritense.logging.LoggableResource
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import com.ritense.valtimo.contract.authorization.UserManagementServiceHolder
import com.ritense.valtimo.contract.domain.ValtimoMediaType.APPLICATION_JSON_UTF8_VALUE
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@SkipComponentScan
@RequestMapping("/api", produces = [APPLICATION_JSON_UTF8_VALUE])
class CaseInstanceResource(
    private val service: CaseInstanceService
) {

    @PostMapping("/v1/case/{caseDefinitionName}/search")
    fun search(
        @LoggableResource("documentDefinitionName") @PathVariable(name = "caseDefinitionName") caseDefinitionName: String,
        @RequestBody searchRequest: SearchWithConfigRequest,
        pageable: Pageable
    ): ResponseEntity<Page<CaseListRowDto>> {
        val result = service.search(caseDefinitionName, searchRequest, pageable)
        return ResponseEntity.ok(result)
    }

    @PostMapping("/v1/case/{caseDefinitionName}/stored-quick-search")
    fun saveQuickSearch(
        @LoggableResource("documentDefinitionName") @PathVariable(name = "caseDefinitionName") caseDefinitionName: String,
        @RequestBody request: CaseDefinitionQuickSearchDto,
    ): ResponseEntity<Any> {
        val currentUserId = UserManagementServiceHolder.currentInstance.currentUserId
        service.storeQuickSearch(caseDefinitionName, request, currentUserId)
        return ResponseEntity.ok().build()
    }

    @DeleteMapping("/v1/case/{caseDefinitionName}/stored-quick-search/{title}")
    fun deleteQuickSearch(
        @LoggableResource("documentDefinitionName") @PathVariable(name = "caseDefinitionName") caseDefinitionName: String,
        @PathVariable(name = "title") quickSearchTitle: String,
    ): ResponseEntity<Any> {
        val currentUserId = UserManagementServiceHolder.currentInstance.currentUserId
        service.deleteQuickSearch(caseDefinitionName, currentUserId, quickSearchTitle)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/v1/case/{caseDefinitionName}/stored-quick-search")
    fun getQuickSearchList(
        @LoggableResource("documentDefinitionName") @PathVariable(name = "caseDefinitionName") caseDefinitionName: String
    ): ResponseEntity<List<CaseDefinitionQuickSearchDto>> {
        val currentUserId = UserManagementServiceHolder.currentInstance.currentUserId
        val result = service.getQuickSearchList(caseDefinitionName, currentUserId)
        return ResponseEntity.ok(
            result.map {
                CaseDefinitionQuickSearchDto(
                    it.queryPath,
                    it.title,
                )
            }
        )
    }
}
