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

import com.ritense.case.service.CaseTabService
import com.ritense.case.web.rest.dto.CaseTabDto
import com.ritense.document.domain.impl.JsonSchemaDocument
import com.ritense.document.domain.impl.JsonSchemaDocumentId.existingId
import com.ritense.logging.LoggableResource
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import com.ritense.valtimo.contract.domain.ValtimoMediaType.APPLICATION_JSON_UTF8_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import java.util.UUID

@Controller
@SkipComponentScan
@RequestMapping("/api", produces = [APPLICATION_JSON_UTF8_VALUE])
class CaseTabResource(
    private val caseTabService: CaseTabService
) {

    @Deprecated("Since 12.2.0")
    @GetMapping("/v1/case-definition/{caseDefinitionName}/tab")
    fun getCaseTabs(
        @LoggableResource("documentDefinitionName") @PathVariable caseDefinitionName: String
    ): ResponseEntity<List<CaseTabDto>> {
        val tabs = caseTabService.getCaseTabs(caseDefinitionName)
            .map { CaseTabDto.of(it) }
        return ResponseEntity.ok(tabs)
    }

    @GetMapping("/v1/document/{documentId}/tab")
    fun getCaseTabsForDocument(
        @LoggableResource(resourceType = JsonSchemaDocument::class) @PathVariable documentId: UUID
    ): ResponseEntity<List<CaseTabDto>> {
        val tabs = caseTabService.getCaseTabs(existingId(documentId))
            .map { CaseTabDto.of(it) }
        return ResponseEntity.ok(tabs)
    }

}
