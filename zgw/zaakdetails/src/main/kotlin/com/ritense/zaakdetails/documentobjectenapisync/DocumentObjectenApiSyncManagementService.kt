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

import com.ritense.document.domain.event.DocumentDefinitionDeployedEvent
import com.ritense.document.domain.impl.JsonSchemaDocumentDefinition
import com.ritense.logging.LoggableResource
import com.ritense.objectsapi.service.ObjectSyncService
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import com.ritense.zaakdetails.documentobjectenapisync.DocumentObjectenApiSyncService.Companion.logger
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Transactional
@Service
@SkipComponentScan
class DocumentObjectenApiSyncManagementService(
    private val documentObjectenApiSyncRepository: DocumentObjectenApiSyncRepository,
    private val objectSyncService: ObjectSyncService,
) {

    @EventListener(DocumentDefinitionDeployedEvent::class)
    fun deployNewSyncConfiguration(event: DocumentDefinitionDeployedEvent) {
        val newDocumentDefinitionId = event.documentDefinition().id()
        val latest = documentObjectenApiSyncRepository
            .findFirstByDocumentDefinitionNameOrderByDocumentDefinitionVersionDesc(newDocumentDefinitionId.name())
        if (latest != null && latest.documentDefinitionVersion + 1 == newDocumentDefinitionId.version()) {
            saveSyncConfiguration(
                DocumentObjectenApiSync(
                    documentDefinitionName = latest.documentDefinitionName,
                    documentDefinitionVersion = newDocumentDefinitionId.version(),
                    objectManagementConfigurationId = latest.objectManagementConfigurationId,
                    enabled = latest.enabled,
                )
            )
        }
    }

    fun getSyncConfiguration(
        @LoggableResource(resourceType = JsonSchemaDocumentDefinition::class) documentDefinitionName: String,
        documentDefinitionVersion: Long
    ): DocumentObjectenApiSync? {
        logger.debug { "Get sync configuration documentDefinitionName=$documentDefinitionName" }
        return documentObjectenApiSyncRepository.findByDocumentDefinitionNameAndDocumentDefinitionVersion(
            documentDefinitionName,
            documentDefinitionVersion
        )
    }

    fun saveSyncConfiguration(sync: DocumentObjectenApiSync) {
        logger.info { "Save sync configuration documentDefinitionName=${sync.documentDefinitionName}" }
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

    fun deleteSyncConfigurationByDocumentDefinition(
        @LoggableResource(resourceType = JsonSchemaDocumentDefinition::class) documentDefinitionName: String,
        documentDefinitionVersion: Long
    ) {
        logger.info {
            """Delete sync configuration documentDefinitionName=$documentDefinitionName
                documentDefinitionVersion=$documentDefinitionVersion"""
        }
        documentObjectenApiSyncRepository.deleteByDocumentDefinitionNameAndDocumentDefinitionVersion(
            documentDefinitionName,
            documentDefinitionVersion
        )
    }
}