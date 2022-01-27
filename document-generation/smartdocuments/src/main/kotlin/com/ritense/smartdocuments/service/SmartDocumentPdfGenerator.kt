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

package com.ritense.smartdocuments.service

import com.fasterxml.jackson.core.JsonPointer
import com.ritense.connector.service.ConnectorService
import com.ritense.document.domain.Document
import com.ritense.document.domain.impl.JsonSchemaRelatedFile
import com.ritense.document.service.DocumentService
import com.ritense.document.service.DocumentVariableService
import com.ritense.documentgeneration.domain.GeneratedDocument
import com.ritense.documentgeneration.domain.placeholders.TemplatePlaceholders
import com.ritense.documentgeneration.domain.templatedata.TemplateData
import com.ritense.documentgeneration.domain.templatedata.TemplateDataField
import com.ritense.documentgeneration.service.PdfDocumentGenerator
import com.ritense.resource.service.ResourceService
import com.ritense.resource.service.request.RawFileUploadRequest
import com.ritense.valtimo.contract.audit.utils.AuditHelper
import com.ritense.valtimo.contract.documentgeneration.event.DossierDocumentGeneratedEvent
import com.ritense.valtimo.contract.utils.RequestHelper
import com.ritense.valtimo.contract.utils.SecurityUtils
import com.ritense.smartdocuments.connector.SmartDocumentsConnector
import org.springframework.context.ApplicationEventPublisher
import org.springframework.http.MediaType
import org.springframework.http.MediaType.APPLICATION_PDF
import java.time.LocalDateTime
import java.util.UUID
import javax.ws.rs.NotSupportedException

class SmartDocumentPdfGenerator(
    private val connectorService: ConnectorService,
    private val documentService: DocumentService,
    private val resourceService: ResourceService,
    private val applicationEventPublisher: ApplicationEventPublisher,
    private val documentVariableService: DocumentVariableService,
) : PdfDocumentGenerator {

    override fun getTemplatePlaceholders(templateName: String): TemplatePlaceholders {
        throw NotSupportedException("SmartDocuments doesn't have API call for this")
    }

    override fun generateDocument(templateName: String, templateData: TemplateData): GeneratedDocument {
        return getSmartDocumentsConnector().generateDocument(templateName, templateData, APPLICATION_PDF)
    }

    override fun getDocumentMediaType(): MediaType {
        return APPLICATION_PDF
    }

    fun generateAndStoreDocument(templateIdentifier: String, document: Document, placeholders: List<JsonPointer>) {
        val generatedDocument = generateDocument(templateIdentifier, document, placeholders)
        val uploadRequest = RawFileUploadRequest(
            generatedDocument.name,
            generatedDocument.extension,
            generatedDocument.size,
            generatedDocument.contentType,
            generatedDocument.asByteArray
        )
        val key = String.format("generated-documents/%s", generatedDocument.name)
        val resource = resourceService.store(key, uploadRequest)
        documentService.assignRelatedFile(
            document.id(),
            JsonSchemaRelatedFile.from(resource).withCreatedBy(SecurityUtils.getCurrentUserLogin())
        )
    }

    private fun generateDocument(templateIdentifier: String, document: Document, placeholders: List<JsonPointer>): GeneratedDocument {
        val generatedDocument = generateDocument(templateIdentifier, getTemplateData(document, placeholders))
        applicationEventPublisher.publishEvent(
            DossierDocumentGeneratedEvent(
                UUID.randomUUID(),
                RequestHelper.getOrigin(),
                LocalDateTime.now(),
                AuditHelper.getActor(),
                templateIdentifier,
                document.id().toString()
            )
        )
        return generatedDocument
    }

    private fun getTemplateData(document: Document, templatePlaceholders: TemplatePlaceholders): TemplateData {
        // TODO: Improve in #33405
        val builder = TemplateData.builder()
        for(placeholder in placeholders) {
            builder.addDataField(
                TemplateDataField(
                    "placeholder.last()",
                    documentVariableService.getTextOrReturnEmptyString(document, "/achternaamAanvrager")
                )
            )
        }

            return builder.build()
    }

    private fun getSmartDocumentsConnector(): SmartDocumentsConnector {
        return connectorService.loadByClassName(SmartDocumentsConnector::class.java)
    }
}