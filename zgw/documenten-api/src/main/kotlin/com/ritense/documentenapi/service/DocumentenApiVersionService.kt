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

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.ritense.authorization.Action
import com.ritense.authorization.AuthorizationContext
import com.ritense.authorization.AuthorizationService
import com.ritense.authorization.request.EntityAuthorizationRequest
import com.ritense.document.domain.impl.JsonSchemaDocument
import com.ritense.document.domain.impl.JsonSchemaDocumentDefinition
import com.ritense.document.service.DocumentDefinitionService
import com.ritense.document.service.DocumentService
import com.ritense.document.service.JsonSchemaDocumentDefinitionActionProvider
import com.ritense.documentenapi.DocumentenApiPlugin
import com.ritense.documentenapi.domain.DocumentenApiVersion
import com.ritense.logging.LoggableResource
import com.ritense.plugin.domain.PluginConfiguration
import com.ritense.plugin.service.PluginService
import com.ritense.processdocument.service.DocumentDefinitionProcessLinkService
import com.ritense.valtimo.camunda.repository.CamundaProcessDefinitionSpecificationHelper
import com.ritense.valtimo.camunda.service.CamundaRepositoryService
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import com.ritense.valtimo.processlink.service.PluginProcessLinkService
import org.springframework.core.io.Resource
import org.springframework.core.io.ResourceLoader
import org.springframework.core.io.support.ResourcePatternUtils
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Transactional
@Service
@SkipComponentScan
class DocumentenApiVersionService(
    private val resourceLoader: ResourceLoader,
    private val objectMapper: ObjectMapper,
    beanDocumentenApiVersions: Map<String, DocumentenApiVersion>,
    private val pluginService: PluginService,
    private val authorizationService: AuthorizationService,
    private val documentService: DocumentService,
    private val documentDefinitionService: DocumentDefinitionService,
    private val documentDefinitionProcessLinkService: DocumentDefinitionProcessLinkService,
    private val pluginProcessLinkService: PluginProcessLinkService,
    private val camundaRepositoryService: CamundaRepositoryService,
) {

    private var documentenApiVersions: Map<String, DocumentenApiVersion> = emptyMap()

    init {
        val resourceDocumentenApiVersions = loadResources()
            .flatMap { resource -> objectMapper.readValue<List<DocumentenApiVersion>>(resource.inputStream) }
            .associateBy { it.version }
        documentenApiVersions = resourceDocumentenApiVersions + beanDocumentenApiVersions
    }

    fun isValidVersion(versionTag: String) = documentenApiVersions.contains(versionTag)

    fun getAllVersions(): List<DocumentenApiVersion> {
        denyAuthorization()
        return documentenApiVersions.values.sortedDescending()
    }

    fun getVersion(
        @LoggableResource("documentDefinitionName") caseDefinitionName: String
    ): DocumentenApiVersion {
        return getPluginVersion(caseDefinitionName).third ?: MINIMUM_VERSION
    }

    fun getVersionByDocumentId(
        @LoggableResource(resourceType = JsonSchemaDocument::class) documentId: UUID
    ): DocumentenApiVersion {
        return getVersion(documentService.get(documentId.toString()).definitionId().name())
    }

    fun getPluginVersion(
        @LoggableResource("documentDefinitionName") caseDefinitionName: String
    ): Triple<PluginConfiguration?, DocumentenApiPlugin?, DocumentenApiVersion?> {
        return detectPluginVersions(caseDefinitionName).lastOrNull() ?: Triple(null, null, null)
    }

    fun detectPluginVersions(
        @LoggableResource("documentDefinitionName") caseDefinitionName: String
    ): List<Triple<PluginConfiguration, DocumentenApiPlugin, DocumentenApiVersion?>> {
        return detectPluginConfigurations(caseDefinitionName)
            .map {  pluginConfiguration ->
                val plugin = pluginService.createInstance(pluginConfiguration) as DocumentenApiPlugin
                val version = getVersionByTag(plugin.apiVersion)
                Triple(pluginConfiguration, plugin, version)
            }
            .sortedByDescending { it.third }
            .toList()
    }

    fun detectPluginConfigurations(
        @LoggableResource("documentDefinitionName") caseDefinitionName: String
    ): List<PluginConfiguration> {
        documentDefinitionService.requirePermission(caseDefinitionName, JsonSchemaDocumentDefinitionActionProvider.VIEW)
        val link = documentDefinitionProcessLinkService.getDocumentDefinitionProcessLink(
            caseDefinitionName,
            "DOCUMENT_UPLOAD"
        )
        if (link.isEmpty) {
            return emptyList()
        }
        val processDefinitionKey = link.get().id.processDefinitionKey
        val detectedConfigurations = AuthorizationContext.runWithoutAuthorization {
            camundaRepositoryService.findLinkedProcessDefinitions(
                CamundaProcessDefinitionSpecificationHelper.byKey(
                    processDefinitionKey
                ).and(CamundaProcessDefinitionSpecificationHelper.byLatestVersion())
            )
                .asSequence()
                .flatMap { pluginProcessLinkService.getProcessLinks(it.id) }
                .map { pluginService.getPluginConfiguration(it.pluginConfigurationId) }
                .filter { it.pluginDefinition.key == DocumentenApiPlugin.PLUGIN_KEY }
                .toList()
        }
        return detectedConfigurations
    }

    fun getVersionByTag(versionTag: String?) = documentenApiVersions[versionTag]

    private fun loadResources(): Array<Resource> {
        return ResourcePatternUtils.getResourcePatternResolver(resourceLoader).getResources(PATH)
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
        private const val PATH = "classpath*:config/documenten-api/*.zgw-documenten-api-version.json"

        val MINIMUM_VERSION = DocumentenApiVersion("1.0.0")
    }

}
