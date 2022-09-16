/*
 * Copyright 2015-2022 Ritense BV, the Netherlands.
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

package com.ritense.zakenapi.uploadprocess

import com.ritense.processdocument.domain.impl.CamundaProcessDefinitionKey
import com.ritense.processdocument.service.ProcessDocumentAssociationService
import com.ritense.zakenapi.uploadprocess.ResourceUploadedEventListener.Companion.UPLOAD_DOCUMENT_PROCESS_DEFINITION_KEY
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/uploadprocess")
class UploadProcessResource(
    private val processDocumentAssociationService: ProcessDocumentAssociationService,
) {

    @GetMapping("/case/{caseDefinitionKey}/check-link")
    fun checkCaseProcessLink(
        @PathVariable caseDefinitionKey: String
    ): ResponseEntity<CheckLinkResponse> {
        val processDocumentDefinitions = processDocumentAssociationService.findAllProcessDocumentDefinitions(
            CamundaProcessDefinitionKey(
                UPLOAD_DOCUMENT_PROCESS_DEFINITION_KEY
            )
        )

        val processCaseLinkExists = processDocumentDefinitions.any {
            caseDefinitionKey == it.processDocumentDefinitionId().documentDefinitionId().name()
        }
        return ResponseEntity.ok(CheckLinkResponse(processCaseLinkExists))
    }
}
