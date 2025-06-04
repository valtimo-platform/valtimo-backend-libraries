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
import com.ritense.document.domain.impl.JsonSchemaDocument
import com.ritense.document.domain.impl.JsonSchemaDocumentDefinition
import com.ritense.document.service.DocumentService
import com.ritense.document.service.JsonSchemaDocumentDefinitionActionProvider.Companion.VIEW
import com.ritense.document.service.impl.JsonSchemaDocumentDefinitionService
import com.ritense.documentenapi.DocumentenApiPlugin
import com.ritense.documentenapi.client.DocumentInformatieObject
import com.ritense.documentenapi.client.PatchDocumentRequest
import com.ritense.documentenapi.domain.DocumentenApiColumn
import com.ritense.documentenapi.domain.DocumentenApiColumnKey
import com.ritense.documentenapi.domain.DocumentenApiUploadField
import com.ritense.documentenapi.domain.DocumentenApiUploadFieldId
import com.ritense.documentenapi.domain.DocumentenApiUploadFieldKey
import com.ritense.documentenapi.repository.DocumentenApiColumnRepository
import com.ritense.documentenapi.repository.DocumentenApiUploadFieldRepository
import com.ritense.documentenapi.web.rest.dto.DocumentSearchRequest
import com.ritense.documentenapi.web.rest.dto.DocumentenApiUploadFieldDto
import com.ritense.documentenapi.web.rest.dto.ModifyDocumentRequest
import com.ritense.documentenapi.web.rest.dto.RelatedFileDto
import com.ritense.logging.LoggableResource
import com.ritense.plugin.domain.PluginConfiguration
import com.ritense.plugin.domain.PluginConfigurationId
import com.ritense.plugin.service.PluginService
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import com.ritense.valueresolver.ValueResolverService
import com.ritense.zgw.LoggingConstants.DOCUMENTEN_API
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.io.InputStream
import java.net.URI
import java.util.UUID

@Transactional
@Service
@SkipComponentScan
class DocumentenApiService(
    private val pluginService: PluginService,
    private val catalogiService: CatalogiService,
    private val documentenApiColumnRepository: DocumentenApiColumnRepository,
    private val documentenApiUploadFieldRepository: DocumentenApiUploadFieldRepository,
    private val authorizationService: AuthorizationService,
    private val valtimoDocumentService: DocumentService,
    private val documentDefinitionService: JsonSchemaDocumentDefinitionService,
    private val documentenApiVersionService: DocumentenApiVersionService,
    private val valueResolverService: ValueResolverService
) {

    fun downloadInformatieObject(
        @LoggableResource(resourceType = PluginConfigurationId::class) pluginConfigurationId: String,
        @LoggableResource(resourceTypeName = DOCUMENTEN_API.ENKELVOUDIG_INFORMATIE_OBJECT) documentId: String
    ): InputStream {
        logger.info { "Download informatie object $documentId" }
        val documentApiPlugin = pluginService.createInstance<DocumentenApiPlugin>(pluginConfigurationId)
        return documentApiPlugin.downloadInformatieObject(documentId)
    }

    fun getInformatieObject(
        @LoggableResource(resourceType = PluginConfigurationId::class) pluginConfigurationId: String,
        @LoggableResource(resourceTypeName = DOCUMENTEN_API.ENKELVOUDIG_INFORMATIE_OBJECT) documentId: String
    ): DocumentInformatieObject {
        logger.debug { "Get informatie object $documentId" }
        val documentApiPlugin = pluginService.createInstance<DocumentenApiPlugin>(pluginConfigurationId)
        return documentApiPlugin.getInformatieObject(documentId)
    }

    fun getCaseInformatieObjecten(
        @LoggableResource(resourceType = JsonSchemaDocument::class) documentId: UUID,
        documentSearchRequest: DocumentSearchRequest,
        pageable: Pageable
    ): Page<DocumentInformatieObject> {
        val documentDefinitionName = valtimoDocumentService.get(documentId.toString()).definitionId().name()
        val (pluginConfiguration, plugin, version) = documentenApiVersionService.getPluginVersion(documentDefinitionName)
        check(pluginConfiguration != null && plugin != null) {
            "No Documenten API plugin configured for Case definition '$documentDefinitionName'"
        }
        check(version != null && version.supportsSortableColumns() && version.supportsFilterableColumns()) {
            "File sorting and filtering not supported on Documenten API plugin version '$version'"
        }
        pageable.sort.forEach { sortColumn ->
            check(version.sortableColumns.contains(sortColumn.property))
        }
        logger.debug { "Get Case Informatie Objecten $documentSearchRequest" }
        return plugin.getInformatieObjecten(documentSearchRequest, pageable)
    }

    fun modifyInformatieObject(
        @LoggableResource(resourceType = PluginConfigurationId::class) pluginConfigurationId: String,
        @LoggableResource(resourceTypeName = DOCUMENTEN_API.ENKELVOUDIG_INFORMATIE_OBJECT) documentId: String,
        modifyDocumentRequest: ModifyDocumentRequest
    ): RelatedFile? {
        val documentApiPlugin: DocumentenApiPlugin = pluginService.createInstance(pluginConfigurationId)
        if (modifyDocumentRequest.trefwoorden?.isNotEmpty() == true) {
            val version = documentenApiVersionService.getVersionByTag(documentApiPlugin.apiVersion)
            check(version.supportsTrefwoorden) {
                val pluginConfiguration = pluginService.getPluginConfiguration(
                    PluginConfigurationId.existingId(pluginConfigurationId)
                )
                "Documenten API plugin '${pluginConfiguration.title}' doesn't support 'trefwoorden'"
            }
        }
        logger.info { "Create Informatie Object Url $documentId" }
        val documentUrl = documentApiPlugin.createInformatieObjectUrl(documentId)
        logger.info { "Modify Informatie Object $documentUrl $modifyDocumentRequest " }
        val informatieObject = documentApiPlugin.modifyInformatieObject(
            documentUrl,
            PatchDocumentRequest(modifyDocumentRequest)
        )
        return getRelatedFiles(informatieObject, pluginConfigurationId)
    }

    fun deleteInformatieObject(
        @LoggableResource(resourceTypeName = DOCUMENTEN_API.ENKELVOUDIG_INFORMATIE_OBJECT) documentUrl: URI
    ) {
        val documentApiPlugin: DocumentenApiPlugin = pluginService.createInstance(
            DocumentenApiPlugin::class.java,
            DocumentenApiPlugin.findConfigurationByUrl(documentUrl)
        ) ?: throw IllegalArgumentException("Trying to delete informatie object by url, but could not find ${DocumentenApiPlugin::class.simpleName} instance for informatieobjectUrl $documentUrl")
        documentApiPlugin.deleteInformatieObject(documentUrl)
    }

    fun deleteInformatieObject(
        @LoggableResource(resourceType = PluginConfigurationId::class) pluginConfigurationId: String,
        @LoggableResource(resourceTypeName = DOCUMENTEN_API.ENKELVOUDIG_INFORMATIE_OBJECT) documentId: String
    ) {
        val documentApiPlugin: DocumentenApiPlugin = pluginService.createInstance(pluginConfigurationId)
        val documentUrl = documentApiPlugin.createInformatieObjectUrl(documentId)
        documentApiPlugin.deleteInformatieObject(documentUrl)
    }

    fun getColumns(
        @LoggableResource("documentDefinitionName") caseDefinitionName: String
    ): List<DocumentenApiColumn> {
        logger.debug { "Get columns $caseDefinitionName" }
        val documentDefinition = documentDefinitionService.findActiveByName(caseDefinitionName)
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

    fun getAllColumnKeys(
        @LoggableResource("documentDefinitionName") caseDefinitionName: String
    ): List<DocumentenApiColumnKey> {
        logger.debug { "Get all column keys $caseDefinitionName" }
        val version = documentenApiVersionService.getVersion(caseDefinitionName)
        val columnKeys = DocumentenApiColumnKey.entries.sortedBy { it.name }
        return if (!version.supportsTrefwoorden) {
            columnKeys.filter { it != DocumentenApiColumnKey.TREFWOORDEN }
        } else {
            columnKeys
        }
    }

    fun updateColumnOrder(columns: List<DocumentenApiColumn>): List<DocumentenApiColumn> {
        logger.info { "Update column order $columns" }
        denyAuthorization()
        require(columns.isNotEmpty()) { "Failed to sort empty Document API columns" }
        val caseDefinitionName = columns[0].id.caseDefinitionName
        require(documentDefinitionService.existsByName(caseDefinitionName)) {
            "Unknown case-definition '$caseDefinitionName'"
        }
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
        logger.info { "Create or updateColumn $column" }
        require(documentDefinitionService.existsByName(column.id.caseDefinitionName)) {
            "Unknown case-definition '${column.id.caseDefinitionName}'"
        }
        denyAuthorization()
        val order = documentenApiColumnRepository.findByIdCaseDefinitionNameAndIdKey(
            column.id.caseDefinitionName,
            column.id.key
        )?.order ?: documentenApiColumnRepository.countAllByIdCaseDefinitionName(column.id.caseDefinitionName).toInt()

        if (column.defaultSort != null) {
            check(!documentenApiColumnRepository.findAllByIdCaseDefinitionNameOrderByOrder(column.id.caseDefinitionName)
                .any { it.id != column.id && it.defaultSort != null }) { "Documenten API can not have default sorting on multiple columns" }
        }

        return documentenApiColumnRepository.save(column.copy(order = order))
    }

    fun deleteColumn(
        @LoggableResource("documentDefinitionName") caseDefinitionName: String,
        columnKey: String
    ) {
        logger.info { "Delete column $caseDefinitionName $columnKey" }
        denyAuthorization()
        val documentenApiColumnKey = DocumentenApiColumnKey.fromProperty(columnKey)
            ?: throw IllegalStateException("Unknown column '$columnKey'")
        documentenApiColumnRepository.deleteByIdCaseDefinitionNameAndIdKey(caseDefinitionName, documentenApiColumnKey)
    }

    fun updateUploadField(uploadField: DocumentenApiUploadField): DocumentenApiUploadField {
        logger.info { "Create or update Documenten API UploadField $uploadField" }
        denyAuthorization()
        require(documentDefinitionService.existsByName(uploadField.id.caseDefinitionName)) {
            "Unknown case-definition '${uploadField.id.caseDefinitionName}'"
        }
        return documentenApiUploadFieldRepository.save(uploadField)
    }

    fun getUploadFields(caseDefinitionName: String): List<DocumentenApiUploadField> {
        logger.debug { "Get Documenten API UploadFields" }
        denyAuthorization()
        val fields = documentenApiUploadFieldRepository.findAllByIdCaseDefinitionName(caseDefinitionName)
        val missingFields = DocumentenApiUploadFieldKey.entries
            .filter { key -> fields.none { it.id.key == key } }
            .map { DocumentenApiUploadField(id = DocumentenApiUploadFieldId(caseDefinitionName, it)) }
        return fields + missingFields
    }

    fun getResolvedUploadFields(valtimoDocumentId: String): List<DocumentenApiUploadFieldDto> {
        logger.debug { "Get Resolved Documenten API UploadFields" }
        val valtimoDocument = valtimoDocumentService[valtimoDocumentId]
        val uploadFields = runWithoutAuthorization { getUploadFields(valtimoDocument.definitionId().name()) }
        val defaultValues = uploadFields.map { it.defaultValue }
        val resolvedDefaultValues = valueResolverService.resolveValues(valtimoDocumentId, defaultValues)
        return uploadFields
            .map { DocumentenApiUploadFieldDto.of(it, resolvedDefaultValues[it.defaultValue] as String?) }
    }

    private fun getRelatedFiles(
        informatieObject: DocumentInformatieObject,
        @LoggableResource(resourceType = PluginConfiguration::class) pluginConfigurationId: String
    ): RelatedFileDto {
        logger.debug { "Get related files" }
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
        )
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}
