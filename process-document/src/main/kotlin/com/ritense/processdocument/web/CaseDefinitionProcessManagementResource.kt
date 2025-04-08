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

package com.ritense.processdocument.web

import com.ritense.logging.LoggableResource
import com.ritense.processdocument.domain.CaseDefinitionProcess
import com.ritense.processdocument.domain.impl.request.DocumentDefinitionProcessLinkResponse
import com.ritense.processdocument.domain.impl.request.DocumentDefinitionProcessRequest
import com.ritense.processdocument.service.CaseDefinitionProcessLinkService
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import com.ritense.valtimo.contract.case_.CaseDefinitionId
import com.ritense.valtimo.contract.domain.ValtimoMediaType
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@SkipComponentScan
@RequestMapping(value = ["/api/management"], produces = [ValtimoMediaType.APPLICATION_JSON_UTF8_VALUE])
class CaseDefinitionProcessManagementResource(
    private val caseDefinitionProcessLinkService: CaseDefinitionProcessLinkService
) {

    @GetMapping("/v1/case-definition/{caseDefinitionKey}/version/{caseDefinitionVersionTag}/feature-process/{type}")
    fun getDocumentDefinitionProcess(
        @LoggableResource("caseDefinitionKey") @PathVariable caseDefinitionKey: String,
        @LoggableResource("caseDefinitionVersionTag") @PathVariable caseDefinitionVersionTag: String,
        @PathVariable("type") type: String,
    ): ResponseEntity<CaseDefinitionProcess> {
        val caseDefinitionId = CaseDefinitionId(caseDefinitionKey, caseDefinitionVersionTag)
        val result = caseDefinitionProcessLinkService.getDocumentDefinitionProcess(caseDefinitionId, type)
        return ResponseEntity.ok<CaseDefinitionProcess>(result)
    }

    @PutMapping("/v1/case-definition/{caseDefinitionKey}/version/{caseDefinitionVersionTag}/feature-process")
    fun putDocumentDefinitionProcess(
        @LoggableResource("caseDefinitionKey") @PathVariable caseDefinitionKey: String,
        @LoggableResource("caseDefinitionVersionTag") @PathVariable caseDefinitionVersionTag: String,
        @RequestBody request: @Valid DocumentDefinitionProcessRequest
    ): ResponseEntity<DocumentDefinitionProcessLinkResponse> {
        val caseDefinitionId = CaseDefinitionId(caseDefinitionKey, caseDefinitionVersionTag)
        val response: DocumentDefinitionProcessLinkResponse =
            caseDefinitionProcessLinkService.saveDocumentDefinitionProcess(caseDefinitionId, request)
        return ResponseEntity.ok<DocumentDefinitionProcessLinkResponse>(response)
    }

    @DeleteMapping("/v1/case-definition/{caseDefinitionKey}/version/{caseDefinitionVersionTag}/feature-process/{type}")
    fun deleteDocumentDefinitionProcess(
        @LoggableResource("caseDefinitionKey") @PathVariable caseDefinitionKey: String,
        @LoggableResource("caseDefinitionVersionTag") @PathVariable caseDefinitionVersionTag: String,
        @PathVariable("type") type: String,
    ): ResponseEntity<Void> {
        val caseDefinitionId = CaseDefinitionId(caseDefinitionKey, caseDefinitionVersionTag)
        caseDefinitionProcessLinkService.deleteDocumentDefinitionProcess(caseDefinitionId, type)
        return ResponseEntity.ok().build()
    }
}