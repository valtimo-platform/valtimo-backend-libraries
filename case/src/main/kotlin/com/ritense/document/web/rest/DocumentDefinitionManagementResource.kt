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

package com.ritense.document.web.rest

import com.ritense.authorization.AuthorizationContext.Companion.runWithoutAuthorization
import com.ritense.document.domain.DocumentDefinition
import com.ritense.document.service.DocumentDefinitionService
import com.ritense.document.service.request.DocumentDefinitionCreateRequest
import com.ritense.document.service.result.DeployDocumentDefinitionResult
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import com.ritense.valtimo.contract.case_.CaseDefinitionId
import com.ritense.valtimo.contract.domain.ValtimoMediaType
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@SkipComponentScan
@RequestMapping("/api/management", produces = [ValtimoMediaType.APPLICATION_JSON_UTF8_VALUE])
class DocumentDefinitionManagementResource(
    private val documentDefinitionService: DocumentDefinitionService
) {

    @GetMapping("/v1/case-definition/{caseDefinitionKey}/version/{versionTag}/document-definition")
    fun getDocumentDefinition(
        @PathVariable("caseDefinitionKey") caseDefinitionKey: String,
        @PathVariable("versionTag") versionTag: String,
    ): ResponseEntity<out DocumentDefinition> {
        val caseDefinitionId = CaseDefinitionId(caseDefinitionKey, versionTag)
        val documentDefinition = runWithoutAuthorization {
            documentDefinitionService.findByCaseDefinitionId(caseDefinitionId)
        }
        return ResponseEntity.of(documentDefinition)
    }

    @PutMapping("/v1/case-definition/{caseDefinitionKey}/version/{versionTag}/document-definition")
    fun putDocumentDefinition(
        @PathVariable("caseDefinitionKey") caseDefinitionKey: String,
        @PathVariable("versionTag") versionTag: String,
        @Valid @RequestBody request: DocumentDefinitionCreateRequest
    ): ResponseEntity<DeployDocumentDefinitionResult> {
        val caseDefinitionId = CaseDefinitionId(caseDefinitionKey, versionTag)
        val result: DeployDocumentDefinitionResult = runWithoutAuthorization {
            documentDefinitionService.deploy(
                request.definition,
                caseDefinitionId
            )
        }
        val httpStatus = if (result.documentDefinition() != null) HttpStatus.OK else HttpStatus.BAD_REQUEST
        return ResponseEntity.status(httpStatus).body<DeployDocumentDefinitionResult>(result)
    }
}