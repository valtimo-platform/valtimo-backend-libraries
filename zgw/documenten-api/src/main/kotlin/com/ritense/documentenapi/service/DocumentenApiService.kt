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
import com.ritense.document.service.JsonSchemaDocumentDefinitionActionProvider.Companion.VIEW
import com.ritense.document.service.impl.JsonSchemaDocumentDefinitionService
import com.ritense.documentenapi.DocumentenApiPlugin
import com.ritense.documentenapi.client.DocumentInformatieObject
import com.ritense.documentenapi.client.PatchDocumentRequest
import com.ritense.documentenapi.domain.DocumentenApiColumn
import com.ritense.documentenapi.domain.DocumentenApiColumnKey
import com.ritense.documentenapi.repository.DocumentenApiColumnRepository
import com.ritense.documentenapi.web.rest.dto.DocumentSearchRequest
import com.ritense.documentenapi.web.rest.dto.DocumentenApiDocumentDto
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
    ): Page<DocumentenApiDocumentDto> {
        val documentDefinitionName = valtimoDocumentService.get(documentId.toString()).definitionId().name()
        val pluginConfigurations = detectPluginConfigurations(documentDefinitionName)
        if (pluginConfigurations.size != 1) {
            throw IllegalStateException("Expected exactly one plugin configuration for case definition '$documentDefinitionName', but found ${pluginConfigurations.size}")
        }
        val pluginConfigurationId = pluginConfigurations.first().id.id
        val documentApiPlugin: DocumentenApiPlugin = pluginService.createInstance(pluginConfigurationId)
        return documentApiPlugin.getInformatieObjecten(documentSearchRequest, pageable)
            .map { mapDocumentenApiDocument(it, pluginConfigurationId.toString()) }
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

    fun getColumns(caseDefinitionName: String): List<DocumentenApiColumn> {
        val documentDefinition = documentDefinitionService.findLatestByName(caseDefinitionName)
            .orElseThrow { IllegalArgumentException("Unknown case-definition '$caseDefinitionName'") }
        authorizationService.requirePermission(
            EntityAuthorizationRequest(
                JsonSchemaDocumentDefinition::class.java,
                VIEW,
                documentDefinition
            )
        )

        return documentenApiColumnRepository.findAllByIdCaseDefinitionNameOrderByOrder(caseDefinitionName)
    }

    fun updateColumnOrder(columns: List<DocumentenApiColumn>): List<DocumentenApiColumn> {
        denyAuthorization()
        require(columns.isNotEmpty()) { "Failed to sort empty Document API columns" }
        val caseDefinitionName = columns[0].id.caseDefinitionName
        documentDefinitionService.findLatestByName(caseDefinitionName)
            .orElseThrow { IllegalArgumentException("Unknown case-definition '$caseDefinitionName'") }
        val existingColumns = this.getColumns(caseDefinitionName)
        require(existingColumns.size == columns.size) { "Incorrect number of Documenten API columns" }
        val newColumns = columns.map { column ->
            existingColumns.find { it.id.key == column.id.key }
                ?.copy(order = column.order)
                ?: throw IllegalStateException("Failed to find column with key ${column.id.key}")
        }
        return documentenApiColumnRepository.saveAll(newColumns)
    }

    fun createOrUpdateColumn(column: DocumentenApiColumn): DocumentenApiColumn {
        documentDefinitionService.findLatestByName(column.id.caseDefinitionName)
            .orElseThrow { IllegalArgumentException("Unknown case-definition '${column.id.caseDefinitionName}'") }
        denyAuthorization()
        val order = documentenApiColumnRepository.findByIdCaseDefinitionNameAndIdKey(
            column.id.caseDefinitionName,
            column.id.key
        )?.order ?: documentenApiColumnRepository.countAllByIdCaseDefinitionName(column.id.caseDefinitionName).toInt()

        if (column.defaultSort != null) {
            check(column.id.key.sortable) { "Documenten API column '${column.id.key}' is not sortable" }
            check(!documentenApiColumnRepository.findAllByIdCaseDefinitionNameOrderByOrder(column.id.caseDefinitionName)
                .any { it.id != column.id && it.defaultSort != null }) { "Documenten API columns can not have default sorting on multiple columns" }
        }

        return documentenApiColumnRepository.save(column.copy(order = order))
    }

    fun deleteColumn(caseDefinitionName: String, columnKey: String) {
        denyAuthorization()
        val documentenApiColumnKey = DocumentenApiColumnKey.from(columnKey)
            ?: throw IllegalStateException("Unknown column '$columnKey'")
        documentenApiColumnRepository.deleteByIdCaseDefinitionNameAndIdKey(caseDefinitionName, documentenApiColumnKey)
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

    fun mapDocumentenApiDocument(
        informatieObject: DocumentInformatieObject,
        pluginConfigurationId: String
    ): DocumentenApiDocumentDto {
        return DocumentenApiDocumentDto(
            fileId = UUID.fromString(informatieObject.url.path.substringAfterLast("/")),
            pluginConfigurationId = UUID.fromString(pluginConfigurationId),
            bestandsnaam = informatieObject.bestandsnaam,
            bestandsomvang = informatieObject.bestandsomvang,
            creatiedatum = informatieObject.creatiedatum.atStartOfDay(),
            auteur = informatieObject.auteur,
            titel = informatieObject.titel,
            status = informatieObject.status?.key,
            taal = informatieObject.taal,
            identificatie = informatieObject.identificatie,
            beschrijving = informatieObject.beschrijving,
            informatieobjecttype = getInformatieobjecttypeByUri(informatieObject.informatieobjecttype),
            trefwoorden = informatieObject.trefwoorden,
            formaat = informatieObject.formaat,
            verzenddatum = informatieObject.verzenddatum,
            ontvangstdatum = informatieObject.ontvangstdatum,
            vertrouwelijkheidaanduiding = informatieObject.vertrouwelijkheidaanduiding?.key,
            versie = informatieObject.versie,
            indicatieGebruiksrecht = informatieObject.indicatieGebruiksrecht
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
