/*
 * Copyright 2015-2023 Ritense BV, the Netherlands.
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

import com.ritense.authorization.AuthorizationContext.Companion.runWithoutAuthorization
import com.ritense.document.domain.Document.Id
import com.ritense.plugin.annotation.Plugin
import com.ritense.plugin.annotation.PluginAction
import com.ritense.plugin.annotation.PluginActionProperty
import com.ritense.plugin.annotation.PluginProperty
import com.ritense.plugin.domain.ActivityType
import com.ritense.processdocument.service.ProcessDocumentService
import com.ritense.resource.domain.MetadataType
import com.ritense.resource.service.TemporaryResourceStorageService
import com.ritense.smartdocuments.client.SmartDocumentsClient
import com.ritense.smartdocuments.connector.SmartDocumentsConnectorProperties
import com.ritense.smartdocuments.domain.*
import com.ritense.smartdocuments.dto.SmartDocumentsPropertiesDto
import com.ritense.valtimo.contract.audit.utils.AuditHelper
import com.ritense.valtimo.contract.documentgeneration.event.DossierDocumentGeneratedEvent
import com.ritense.valtimo.contract.utils.RequestHelper
import com.ritense.valueresolver.ValueResolverService
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.hibernate.validator.constraints.URL
import org.springframework.context.ApplicationEventPublisher
import java.time.LocalDateTime
import java.util.UUID

@Plugin(
    key = "smartdocuments",
    title = "SmartDocuments Plugin",
    description = "Generate documents with smart templates."
)
class SmartDocumentsPlugin(
    private val processDocumentService: ProcessDocumentService,
    private val applicationEventPublisher: ApplicationEventPublisher,
    private val smartDocumentsClient: SmartDocumentsClient,
    private val valueResolverService: ValueResolverService,
    private val temporaryResourceStorageService: TemporaryResourceStorageService,
) {

    @URL
    @PluginProperty(key = "url", required = true, secret = false)
    lateinit var url: String

    @PluginProperty(key = "username", required = true, secret = false)
    lateinit var username: String

    @PluginProperty(key = "password", required = true, secret = true)
    lateinit var password: String

    @PluginAction(
        key = "generate-document",
        title = "Generate document",
        description = "Generates a document of a given type based on a template with data from a case.",
        activityTypes = [ActivityType.SERVICE_TASK_START]
    )
    fun generate(
        execution: DelegateExecution,
        @PluginActionProperty templateGroup: String,
        @PluginActionProperty templateName: String,
        @PluginActionProperty format: String,
        @PluginActionProperty templateData: Array<TemplateDataEntry>,
        @PluginActionProperty resultingDocumentProcessVariableName: String,
    ) {
        val document = runWithoutAuthorization {
            processDocumentService.getDocument(execution)
        }
        val resolvedTemplateData = resolveTemplateData(templateData, execution)
        val generatedDocument = generateDocument(templateGroup, templateName, resolvedTemplateData, DocumentFormatOption.valueOf(format))
        publishDossierDocumentGeneratedEvent(document.id(), templateName)
        val resourceId = generatedDocument.use {
            saveGeneratedDocumentToTempFile(generatedDocument)
        }
        execution.setVariable(resultingDocumentProcessVariableName, resourceId)
    }

    @PluginAction(
        key = "get-template-names",
        title = "Get Template Names",
        description = "Fetch the template names of a template group.",
        activityTypes = [ActivityType.SERVICE_TASK_START]
    )
    fun getTemplateNames(
        execution: DelegateExecution,
        @PluginActionProperty templateGroupName: String,
        @PluginActionProperty resultingTemplateNameListProcessVariableName: String
    ) {
        val pluginProperties = SmartDocumentsPropertiesDto(
            username = username,
            password = password,
            url = url
        )

        val documentsStructure = smartDocumentsClient.getDocumentStructure(pluginProperties)

        val templateNameList = if (documentsStructure != null) {
            val templateGroup = findTemplateGroupByName(
                templateGroups = documentsStructure.templatesStructure.templateGroups,
                groupName = templateGroupName
            )
                templateGroup?.templates?.map { it.name } ?: emptyList()
        } else {
            emptyList()
        }

        execution.setVariable(resultingTemplateNameListProcessVariableName, templateNameList)
    }

    private fun findTemplateGroupByName(
        templateGroups: List<TemplateGroup>,
        groupName: String
    ): TemplateGroup? {
        for (group in templateGroups) {
            if (group.name == groupName) {
                return group
            }

            val foundInChildGroups = findTemplateGroupByName(group.templateGroups, groupName)
            if (foundInChildGroups != null) {
                return foundInChildGroups
            }
        }
        return null
    }

    private fun saveGeneratedDocumentToTempFile(generatedDocument: FileStreamResponse): String {
        val metadata = mapOf(MetadataType.FILE_NAME.key to generatedDocument.filename)
        return temporaryResourceStorageService.store(generatedDocument.documentData, metadata)
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
        templateData: Map<String, Any?>,
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

    private fun resolveTemplateData(
        templateData: Array<TemplateDataEntry>,
        execution: DelegateExecution
    ): Map<String, Any?> {
        val placeHolderValueMap = valueResolverService.resolveValues(
            execution.processInstanceId,
            execution,
            templateData.map { it.value }.toList()
        )
        return templateData.associate { it.key to placeHolderValueMap.getOrDefault(it.value, null) }
    }
}
