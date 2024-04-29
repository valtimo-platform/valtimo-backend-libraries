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

package com.ritense.documentenapi.service

import com.ritense.authorization.Action
import com.ritense.authorization.AuthorizationContext.Companion.runWithoutAuthorization
import com.ritense.authorization.AuthorizationService
import com.ritense.authorization.request.EntityAuthorizationRequest
import com.ritense.catalogiapi.service.CatalogiService
import com.ritense.document.domain.RelatedFile
import com.ritense.document.domain.impl.JsonSchemaDocumentDefinition
import com.ritense.document.service.DocumentService
import com.ritense.document.service.JsonSchemaDocumentDefinitionActionProvider
import com.ritense.document.service.JsonSchemaDocumentDefinitionActionProvider.Companion.VIEW
import com.ritense.document.service.impl.JsonSchemaDocumentDefinitionService
import com.ritense.documentenapi.DocumentenApiPlugin
import com.ritense.documentenapi.client.DocumentInformatieObject
import com.ritense.documentenapi.client.PatchDocumentRequest
import com.ritense.documentenapi.domain.DocumentenApiColumn
import com.ritense.documentenapi.domain.DocumentenApiColumnId
import com.ritense.documentenapi.domain.DocumentenApiColumnKey
import com.ritense.documentenapi.repository.DocumentenApiColumnRepository
import com.ritense.documentenapi.web.rest.dto.DocumentSearchRequest
import com.ritense.documentenapi.web.rest.dto.ModifyDocumentRequest
import com.ritense.documentenapi.web.rest.dto.RelatedFileDto
import com.ritense.plugin.domain.PluginConfiguration
import com.ritense.plugin.service.PluginService
import com.ritense.processdocument.service.DocumentDefinitionProcessLinkService
import com.ritense.valtimo.camunda.repository.CamundaProcessDefinitionSpecificationHelper.Companion.byKey
import com.ritense.valtimo.camunda.repository.CamundaProcessDefinitionSpecificationHelper.Companion.byLatestVersion
import com.ritense.valtimo.camunda.service.CamundaRepositoryService
import com.ritense.valtimo.processlink.service.PluginProcessLinkService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.transaction.annotation.Transactional
import java.io.InputStream
import java.net.URI
import java.util.UUID

@Transactional
class DocumentenApiService(
    private val pluginService: PluginService,
    private val catalogiService: CatalogiService,
    private val documentenApiColumnRepository: DocumentenApiColumnRepository,
    private val authorizationService: AuthorizationService,
    private val valtimoDocumentService: DocumentService,
    private val documentDefinitionService: JsonSchemaDocumentDefinitionService,
    private val documentDefinitionProcessLinkService: DocumentDefinitionProcessLinkService,
    private val pluginProcessLinkService: PluginProcessLinkService,
    private val camundaRepositoryService: CamundaRepositoryService,
) {
    fun downloadInformatieObject(pluginConfigurationId: String, documentId: String): InputStream {
        val documentApiPlugin: DocumentenApiPlugin = pluginService.createInstance(pluginConfigurationId)
        return documentApiPlugin.downloadInformatieObject(documentId)
    }

    fun getInformatieObject(pluginConfigurationId: String, documentId: String): DocumentInformatieObject {
        val documentApiPlugin: DocumentenApiPlugin = pluginService.createInstance(pluginConfigurationId)
        return documentApiPlugin.getInformatieObject(documentId)
    }

    fun getCaseInformatieObjecten(
        documentId: UUID,
        documentSearchRequest: DocumentSearchRequest,
        pageable: Pageable
    ): Page<RelatedFile> {
        val documentDefinitionName = valtimoDocumentService.get(documentId.toString()).definitionId().name()
        val pluginConfigurations = detectPluginConfigurations(documentDefinitionName)
        if (pluginConfigurations.size != 1) {
            throw IllegalStateException("Expected exactly one plugin configuration for case definition '$documentDefinitionName', but found ${pluginConfigurations.size}")
        }
        val pluginConfigurationId = pluginConfigurations.first().id.id
        val documentApiPlugin: DocumentenApiPlugin = pluginService.createInstance(pluginConfigurationId)
        return documentApiPlugin.getInformatieObjecten(documentSearchRequest, pageable)
            .map { getRelatedFiles(it, pluginConfigurationId.toString()) }
    }

    fun modifyInformatieObject(
        pluginConfigurationId: String,
        documentId: String,
        modifyDocumentRequest: ModifyDocumentRequest
    ): RelatedFile? {
        val documentApiPlugin: DocumentenApiPlugin = pluginService.createInstance(pluginConfigurationId)
        val documentUrl = documentApiPlugin.createInformatieObjectUrl(documentId)
        val informatieObject =
            documentApiPlugin.modifyInformatieObject(documentUrl, PatchDocumentRequest(modifyDocumentRequest))
        return getRelatedFiles(informatieObject, pluginConfigurationId)
    }

    fun deleteInformatieObject(pluginConfigurationId: String, documentId: String) {
        val documentApiPlugin: DocumentenApiPlugin = pluginService.createInstance(pluginConfigurationId)
        val documentUrl = documentApiPlugin.createInformatieObjectUrl(documentId)
        documentApiPlugin.deleteInformatieObject(documentUrl)
    }

    fun getConfiguredColumns(caseDefinitionName: String): List<DocumentenApiColumn> {
        denyAuthorization()
        val savedColumns = documentenApiColumnRepository.findAllByIdCaseDefinitionNameOrderByOrder(caseDefinitionName)

        return savedColumns + DocumentenApiColumnKey.entries
            .filter { columnKey -> !savedColumns.any { it.id.key == columnKey } }
            .mapIndexed { i, columnKey ->
                DocumentenApiColumn(
                    id = DocumentenApiColumnId(caseDefinitionName, columnKey),
                    order = savedColumns.size + i,
                    enabled = false
                )
            }
    }

    fun getColumns(caseDefinitionName: String): List<DocumentenApiColumn> {
        authorizationService.requirePermission(
            EntityAuthorizationRequest(
                JsonSchemaDocumentDefinition::class.java,
                JsonSchemaDocumentDefinitionActionProvider.VIEW,
                documentDefinitionService.findLatestByName(caseDefinitionName).orElseThrow()
            )
        )

        return documentenApiColumnRepository.findAllByIdCaseDefinitionNameAndEnabledIsTrueOrderByOrder(
            caseDefinitionName
        )
    }

    fun updateColumnOrder(columns: List<DocumentenApiColumn>): List<DocumentenApiColumn> {
        denyAuthorization()
        require(columns.isNotEmpty()) { "Failed to sort empty Document API columns" }
        val existingColumns = getConfiguredColumns(columns[0].id.caseDefinitionName)
        require(existingColumns.size == columns.size) { "Incorrect number of Documenten API columns" }
        columns.forEach { column ->
            val existingColumn = existingColumns.find { it.id.key == column.id.key }
                ?: throw IllegalStateException("No Documenten API column exists with key '${column.id.key}' for case definition '${column.id.caseDefinitionName}'")
            require(column.enabled == existingColumn.enabled) { "Error in Documenten API column with key '${column.id.key}'" }
        }
        return documentenApiColumnRepository.saveAll(columns)
    }

    fun updateColumn(column: DocumentenApiColumn): DocumentenApiColumn {
        denyAuthorization()
        val order = documentenApiColumnRepository.findByIdCaseDefinitionNameAndIdKey(
            column.id.caseDefinitionName,
            column.id.key
        )?.order ?: documentenApiColumnRepository.countAllByIdCaseDefinitionName(column.id.caseDefinitionName).toInt()

        return documentenApiColumnRepository.save(column.copy(order = order))
    }

    fun getApiVersions(caseDefinitionName: String): List<String> {
        return runWithoutAuthorization {
            detectPluginConfigurations(caseDefinitionName)
                .mapNotNull { (pluginService.createInstance(it) as DocumentenApiPlugin).apiVersion }
                .toList()
                .sorted()
        }
    }

    fun detectPluginConfigurations(caseDefinitionName: String): List<PluginConfiguration> {
        documentDefinitionService.requirePermission(caseDefinitionName, VIEW)
        val link =
            documentDefinitionProcessLinkService.getDocumentDefinitionProcessLink(caseDefinitionName, "DOCUMENT_UPLOAD")
        if (link.isEmpty) {
            return emptyList()
        }
        val processDefinitionKey = link.get().id.processDefinitionKey
        val detectedConfigurations = runWithoutAuthorization {
            camundaRepositoryService.findLinkedProcessDefinitions(byKey(processDefinitionKey).and(byLatestVersion()))
                .asSequence()
                .flatMap { pluginProcessLinkService.getProcessLinks(it.id) }
                .map { pluginService.getPluginConfiguration(it.pluginConfigurationId) }
                .filter { it.pluginDefinition.key == DocumentenApiPlugin.PLUGIN_KEY }
                .toList()
        }
        return detectedConfigurations
    }

    private fun getRelatedFiles(
        informatieObject: DocumentInformatieObject,
        pluginConfigurationId: String
    ): RelatedFileDto {
        return RelatedFileDto(
            fileId = UUID.fromString(informatieObject.url.path.substringAfterLast("/")),
            fileName = informatieObject.bestandsnaam,
            sizeInBytes = informatieObject.bestandsomvang,
            createdOn = informatieObject.creatiedatum.atStartOfDay(),
            createdBy = informatieObject.auteur,
            author = informatieObject.auteur,
            title = informatieObject.titel,
            status = informatieObject.status?.key,
            language = informatieObject.taal,
            pluginConfigurationId = UUID.fromString(pluginConfigurationId),
            identification = informatieObject.identificatie,
            description = informatieObject.beschrijving,
            informatieobjecttype = getInformatieobjecttypeByUri(informatieObject.informatieobjecttype),
            keywords = informatieObject.trefwoorden,
            format = informatieObject.formaat,
            sendDate = informatieObject.verzenddatum,
            receiptDate = informatieObject.ontvangstdatum,
            confidentialityLevel = informatieObject.vertrouwelijkheidaanduiding?.key,
            version = informatieObject.versie,
            indicationUsageRights = informatieObject.indicatieGebruiksrecht
        )
    }

    private fun getInformatieobjecttypeByUri(uri: String?): String? {
        return uri?.let { catalogiService.getInformatieobjecttype(URI(it))?.omschrijving }
    }

    private fun denyAuthorization() {
        authorizationService.requirePermission(
            EntityAuthorizationRequest(
                JsonSchemaDocumentDefinition::class.java,
                Action.deny()
            )
        );
    }
}
