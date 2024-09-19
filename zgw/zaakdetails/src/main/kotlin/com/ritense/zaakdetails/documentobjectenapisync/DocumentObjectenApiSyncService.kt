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

package com.ritense.zaakdetails.documentobjectenapisync

import com.fasterxml.jackson.databind.node.ObjectNode
import com.ritense.document.domain.Document
import com.ritense.document.domain.event.DocumentCreatedEvent
import com.ritense.document.domain.event.DocumentModifiedEvent
import com.ritense.document.service.DocumentService
import com.ritense.objectenapi.ObjectenApiPlugin
import com.ritense.objectenapi.client.Comparator.EQUAL_TO
import com.ritense.objectenapi.client.ObjectRecord
import com.ritense.objectenapi.client.ObjectRequest
import com.ritense.objectenapi.client.ObjectSearchParameter
import com.ritense.objectenapi.management.ObjectManagementInfoProvider
import com.ritense.objectsapi.service.ObjectSyncService
import com.ritense.objecttypenapi.ObjecttypenApiPlugin
import com.ritense.plugin.service.PluginService
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import org.springframework.context.event.EventListener
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Transactional
@Service
@SkipComponentScan
class DocumentObjectenApiSyncService(
    private val documentObjectenApiSyncRepository: DocumentObjectenApiSyncRepository,
    private val objectObjectManagementInfoProvider: ObjectManagementInfoProvider,
    private val documentService: DocumentService,
    private val pluginService: PluginService,
    private val objectSyncService: ObjectSyncService,
) {
    fun getSyncConfiguration(
        documentDefinitionName: String,
        documentDefinitionVersion: Long
    ): DocumentObjectenApiSync? {
        return documentObjectenApiSyncRepository.findByDocumentDefinitionNameAndDocumentDefinitionVersion(
            documentDefinitionName,
            documentDefinitionVersion
        )
    }

    fun saveSyncConfiguration(sync: DocumentObjectenApiSync) {
        val modifiedSync = getSyncConfiguration(sync.documentDefinitionName, sync.documentDefinitionVersion)
            ?.copy(
                objectManagementConfigurationId = sync.objectManagementConfigurationId,
                enabled = sync.enabled
            )
            ?: sync

        // Remove old connector configuration
        objectSyncService.getObjectSyncConfig(sync.documentDefinitionName).content
            .forEach { objectSyncService.removeObjectSyncConfig(it.id.id) }

        documentObjectenApiSyncRepository.save(modifiedSync)
    }

    fun deleteSyncConfigurationByDocumentDefinition(documentDefinitionName: String, documentDefinitionVersion: Long) {
        documentObjectenApiSyncRepository.deleteByDocumentDefinitionNameAndDocumentDefinitionVersion(
            documentDefinitionName,
            documentDefinitionVersion
        )
    }

    @EventListener(DocumentCreatedEvent::class)
    fun handleDocumentCreatedEvent(event: DocumentCreatedEvent) {
        sync(documentService.get(event.documentId().id.toString()))
    }

    @EventListener(DocumentModifiedEvent::class)
    fun handleDocumentModifiedEvent(event: DocumentModifiedEvent) {
        sync(documentService.get(event.documentId().id.toString()))
    }

    private fun sync(document: Document) {
        val syncConfiguration = getSyncConfiguration(document.definitionId().name(), document.definitionId().version())
        if (syncConfiguration?.enabled == true) {
            val objectManagementConfiguration =
                objectObjectManagementInfoProvider.getObjectManagementInfo(syncConfiguration.objectManagementConfigurationId)
            val objectenApiPlugin =
                pluginService.createInstance<ObjectenApiPlugin>(objectManagementConfiguration.objectenApiPluginConfigurationId)
            val objecttypenApiPlugin =
                pluginService.createInstance<ObjecttypenApiPlugin>(objectManagementConfiguration.objecttypenApiPluginConfigurationId)
            val searchString = ObjectSearchParameter.toQueryParameter(
                ObjectSearchParameter("caseId", EQUAL_TO, document.id().toString())
            )

            val syncObject = objectenApiPlugin.getObjectsByObjectTypeIdWithSearchParams(
                objecttypenApiPlugin.url,
                objectManagementConfiguration.objecttypeId,
                searchString,
                PageRequest.of(0, 2)
            ).results.firstOrNull()

            val content = document.content().asJson() as ObjectNode
            content.put("caseId", document.id().id.toString())

            val objectRequest = ObjectRequest(
                type = objecttypenApiPlugin.getObjectTypeUrlById(objectManagementConfiguration.objecttypeId),
                record = ObjectRecord(
                    typeVersion = objectManagementConfiguration.objecttypeVersion,
                    data = content,
                    startAt = LocalDate.now()
                )
            )

            if (syncObject == null) {
                objectenApiPlugin.createObject(objectRequest)
            } else {
                objectenApiPlugin.objectUpdate(syncObject.url, objectRequest)
            }

        }
    }

}
