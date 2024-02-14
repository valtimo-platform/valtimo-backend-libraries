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

package com.ritense.smartdocuments.service

import com.ritense.authorization.AuthorizationContext
import com.ritense.connector.service.ConnectorService
import com.ritense.document.domain.Document
import com.ritense.document.domain.impl.JsonSchemaRelatedFile
import com.ritense.document.service.DocumentService
import com.ritense.documentgeneration.domain.GeneratedDocument
import com.ritense.resource.service.ResourceService
import com.ritense.resource.service.request.ByteArrayMultipartFile
import com.ritense.smartdocuments.connector.SmartDocumentsConnector
import com.ritense.smartdocuments.domain.DocumentFormatOption
import com.ritense.valtimo.contract.audit.utils.AuditHelper
import com.ritense.valtimo.contract.documentgeneration.event.DossierDocumentGeneratedEvent
import com.ritense.valtimo.contract.utils.RequestHelper
import com.ritense.valtimo.contract.utils.SecurityUtils
import java.time.LocalDateTime
import java.util.UUID
import org.springframework.context.ApplicationEventPublisher

@Deprecated("Since 12.0.0")
class SmartDocumentGenerator(
    private val connectorService: ConnectorService,
    private val documentService: DocumentService,
    private val resourceService: ResourceService,
    private val applicationEventPublisher: ApplicationEventPublisher,
) {

    @Deprecated("Since 12.0.0", ReplaceWith("com.ritense.smartdocuments.plugin.SmartDocumentsPlugin"))
    fun generateAndStoreDocument(
        documentId: Document.Id,
        templateGroup: String,
        templateId: String,
        templateData: Map<String, Any>,
        format: DocumentFormatOption
    ) {
        val generatedDocument = generateDocument(documentId, templateGroup, templateId, templateData, format)

        val document = AuthorizationContext.runWithoutAuthorization { documentService.findBy(documentId) }
            .orElseThrow { NullPointerException("Document $documentId needed for generation of document not found") }

        val resource = resourceService.store(
            document.definitionId().name(),
            generatedDocument.name,
            ByteArrayMultipartFile(
                generatedDocument.asByteArray,
                generatedDocument.name,
                format.mediaType
            )
        )

        val relatedFile = JsonSchemaRelatedFile.from(resource).withCreatedBy(SecurityUtils.getCurrentUserLogin())
        AuthorizationContext.runWithoutAuthorization { documentService.assignRelatedFile(documentId, relatedFile) }
    }

    private fun generateDocument(
        documentId: Document.Id,
        templateGroup: String,
        templateId: String,
        templateData: Map<String, Any>,
        format: DocumentFormatOption
    ): GeneratedDocument {
        val generatedDocument = generateDocument(templateGroup, templateId, templateData, format)
        applicationEventPublisher.publishEvent(
            DossierDocumentGeneratedEvent(
                UUID.randomUUID(),
                RequestHelper.getOrigin(),
                LocalDateTime.now(),
                AuditHelper.getActor(),
                templateId,
                documentId.toString()
            )
        )
        return generatedDocument
    }

    private fun generateDocument(
        templateGroup: String,
        templateId: String,
        templateData: Map<String, Any>,
        format: DocumentFormatOption
    ): GeneratedDocument {
        return getSmartDocumentsConnector().generateDocument(templateGroup, templateId, templateData, format)
    }

    private fun getSmartDocumentsConnector(): SmartDocumentsConnector {
        return connectorService.loadByClassName(SmartDocumentsConnector::class.java)
    }
}