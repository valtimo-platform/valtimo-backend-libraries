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

import com.ritense.document.domain.impl.JsonSchemaDocumentId
import com.ritense.document.service.DocumentService
import com.ritense.processdocument.domain.impl.request.StartProcessForDocumentRequest
import com.ritense.processdocument.service.DocumentDefinitionProcessLinkService
import com.ritense.processdocument.service.ProcessDocumentService
import mu.KotlinLogging
import java.util.UUID

class UploadProcessService(
    private val documentService: DocumentService,
    private val processDocumentService: ProcessDocumentService,
    private val documentDefinitionProcessLinkService: DocumentDefinitionProcessLinkService,
) {

    fun startUploadResourceProcess(caseId: String, resourceId: String) {
        val caseDefinitionName = documentService.get(caseId).definitionId().name()
        val link = documentDefinitionProcessLinkService.getDocumentDefinitionProcessLink(caseDefinitionName, DOCUMENT_UPLOAD)
        if (!link.isPresent) {
            throw IllegalStateException("No upload-process linked to case: $caseDefinitionName")
        }

        val result = processDocumentService.startProcessForDocument(
            StartProcessForDocumentRequest(
                JsonSchemaDocumentId.existingId(UUID.fromString(caseId)),
                link.get().id.processDefinitionKey,
                mapOf(RESOURCE_ID_PROCESS_VAR to resourceId)
            )
        )

        if (result.resultingDocument().isEmpty) {
            var logMessage = "Errors occurred during starting the document-upload process:"
            result.errors().forEach { logMessage += "\n - " + it.asString() }
            logger.error { logMessage }
        }
    }

    companion object {
        private val logger = KotlinLogging.logger {}

        const val RESOURCE_ID_PROCESS_VAR = "resourceId"
        const val DOCUMENT_UPLOAD = "DOCUMENT_UPLOAD"
    }
}
