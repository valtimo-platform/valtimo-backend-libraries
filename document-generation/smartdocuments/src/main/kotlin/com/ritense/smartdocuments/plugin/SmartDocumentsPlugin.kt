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
package com.ritense.smartdocuments.plugin

import com.fasterxml.jackson.core.JsonPointer
import com.ritense.document.domain.Document
import com.ritense.document.domain.Document.Id
import com.ritense.plugin.annotation.Plugin
import com.ritense.plugin.annotation.PluginAction
import com.ritense.plugin.annotation.PluginActionProperty
import com.ritense.plugin.annotation.PluginProperty
import com.ritense.plugin.domain.ActivityType
import com.ritense.processdocument.service.ProcessDocumentService
import com.ritense.smartdocuments.client.SmartDocumentsClient
import com.ritense.smartdocuments.connector.SmartDocumentsConnectorProperties
import com.ritense.smartdocuments.domain.DocumentFormatOption
import com.ritense.smartdocuments.domain.FileStreamResponse
import com.ritense.smartdocuments.domain.GeneratedSmartDocumentFile
import com.ritense.smartdocuments.domain.SmartDocumentsRequest
import com.ritense.valtimo.contract.audit.utils.AuditHelper
import com.ritense.valtimo.contract.documentgeneration.event.DossierDocumentGeneratedEvent
import com.ritense.valtimo.contract.json.Mapper
import com.ritense.valtimo.contract.utils.RequestHelper
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.VariableScope
import org.springframework.context.ApplicationEventPublisher
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler
import java.nio.file.Files
import java.nio.file.Path
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.util.UUID
import kotlin.io.path.deleteIfExists
import kotlin.io.path.outputStream
import kotlin.io.path.pathString

@Plugin(
    key = "smartdocuments",
    title = "SmartDocuments Plugin",
    description = "Generate documents with smart templates."
)
class SmartDocumentsPlugin(
    private val processDocumentService: ProcessDocumentService,
    private val applicationEventPublisher: ApplicationEventPublisher,
    private val smartDocumentsClient: SmartDocumentsClient,
    private val threadPoolTaskScheduler: ThreadPoolTaskScheduler,
) {

    @PluginProperty(key = "url", required = true)
    private lateinit var url: String

    @PluginProperty(key = "username", required = true)
    private lateinit var username: String

    @PluginProperty(key = "password", required = true)
    private lateinit var password: String

    @PluginAction(
        key = "generate-document",
        title = "Generate document",
        description = "Generates a document of a given type based on a template with data from a case.",
        activityTypes = [ActivityType.SERVICE_TASK]
    )
    fun generate(
        execution: DelegateExecution,
        @PluginActionProperty templateGroup: String,
        @PluginActionProperty templateName: String,
        @PluginActionProperty format: DocumentFormatOption,
        @PluginActionProperty templatePlaceholders: Map<String, String>,
        @PluginActionProperty resultingDocumentLocation: String,
    ) {
        val document = processDocumentService.getDocument(execution)
        val templateData = getTemplateData(templatePlaceholders, execution, document)
        val generatedDocument = generateDocument(templateGroup, templateName, templateData, format)
        publishDossierDocumentGeneratedEvent(document.id(), templateName)
        val tempFilePath = saveGeneratedDocumentToTempFile(generatedDocument)
        val generatedSmartDocumentFile = GeneratedSmartDocumentFile(
            generatedDocument.filename,
            generatedDocument.extension,
            tempFilePath.pathString,
        )
        execution.setVariable(
            resultingDocumentLocation,
            Mapper.INSTANCE.get().convertValue(generatedSmartDocumentFile, Map::class.java)
        )
    }

    private fun saveGeneratedDocumentToTempFile(generatedDocument: FileStreamResponse): Path {
        val tempFile = Files.createTempFile("tempSmartDocument", ".${generatedDocument.extension}")
        tempFile.outputStream().use { tempFileOut -> generatedDocument.documentData.copyTo(tempFileOut) }
        threadPoolTaskScheduler.schedule({ tempFile.deleteIfExists() }, Instant.now().plus(Duration.ofMinutes(5)))
        return tempFile.toAbsolutePath()
    }

    private fun publishDossierDocumentGeneratedEvent(
        documentId: Id,
        templateName: String,
    ) {
        applicationEventPublisher.publishEvent(
            DossierDocumentGeneratedEvent(
                UUID.randomUUID(),
                RequestHelper.getOrigin(),
                LocalDateTime.now(),
                AuditHelper.getActor(),
                templateName,
                documentId.toString()
            )
        )
    }

    private fun generateDocument(
        templateGroup: String,
        templateName: String,
        templateData: Map<String, Any>,
        format: DocumentFormatOption
    ): FileStreamResponse {
        val request = SmartDocumentsRequest(
            templateData,
            SmartDocumentsRequest.SmartDocument(
                SmartDocumentsRequest.Selection(
                    templateGroup,
                    templateName
                )
            )
        )
        smartDocumentsClient.setProperties(SmartDocumentsConnectorProperties(url, username, password))
        return smartDocumentsClient.generateDocumentStream(request, format)
    }

    private fun getTemplateData(
        templatePlaceholders: Map<String, String>,
        variableScope: VariableScope,
        document: Document
    ): Map<String, Any> {
        return templatePlaceholders.entries
            .associate { it.key to getPlaceholderValue(it.value, variableScope, document) }
    }

    private fun getPlaceholderValue(value: String, variableScope: VariableScope, document: Document): Any {
        return if (value.startsWith("pv:")) {
            getPlaceholderValueFromProcessVariable(value.substring("pv:".length), variableScope)
        } else if (value.startsWith("doc:")) {
            getPlaceholderValueFromDocument(value.substring("doc:".length), document)
        } else {
            value
        }
    }

    private fun getPlaceholderValueFromProcessVariable(value: String, variableScope: VariableScope): Any {
        return variableScope.variables[value].toString()
    }

    private fun getPlaceholderValueFromDocument(path: String, document: Document): Any {
        val node = document.content().getValueBy(JsonPointer.valueOf(path)).orElse(null)
        return if (node == null || node.isMissingNode || node.isNull) {
            ""
        } else if (node.isValueNode || node.isArray || node.isObject) {
            Mapper.INSTANCE.get().treeToValue(node, Object::class.java)
        } else {
            node.asText()
        }
    }
}
