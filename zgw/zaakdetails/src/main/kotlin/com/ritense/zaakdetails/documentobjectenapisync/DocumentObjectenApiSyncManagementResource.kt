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

import com.ritense.logging.LoggableResource
import com.ritense.objectenapi.management.ObjectManagementInfoProvider
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import com.ritense.valtimo.contract.domain.ValtimoMediaType.APPLICATION_JSON_UTF8_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@SkipComponentScan
@RequestMapping("/api/management", produces = [APPLICATION_JSON_UTF8_VALUE])
class DocumentObjectenApiSyncManagementResource(
    private val documentObjectenApiSyncManagementService: DocumentObjectenApiSyncManagementService,
    private val objectManagementInfoProvider: ObjectManagementInfoProvider,
) {
    @GetMapping("/v1/document-definition/{name}/version/{version}/objecten-api-sync")
    fun getSyncConfiguration(
        @LoggableResource("documentDefinitionName") @PathVariable(name = "name") documentDefinitionName: String,
        @PathVariable(name = "version") documentDefinitionVersion: Long,
    ): ResponseEntity<DocumentObjectenApiSyncResponse?> {
        val syncConfiguration =
            documentObjectenApiSyncManagementService.getSyncConfiguration(documentDefinitionName, documentDefinitionVersion)
                ?: return ResponseEntity.ok(null)
        val objectManagementConfiguration =
            objectManagementInfoProvider.getObjectManagementInfo(syncConfiguration.objectManagementConfigurationId)
        return ResponseEntity.ok(
            DocumentObjectenApiSyncResponse.of(syncConfiguration, objectManagementConfiguration)
        )
    }

    @PutMapping("/v1/document-definition/{name}/version/{version}/objecten-api-sync")
    fun createOrUpdateSyncConfiguration(
        @LoggableResource("documentDefinitionName") @PathVariable(name = "name") documentDefinitionName: String,
        @PathVariable(name = "version") documentDefinitionVersion: Long,
        @RequestBody syncRequest: DocumentObjectenApiSyncRequest
    ): ResponseEntity<Unit> {
        val syncConfiguration = syncRequest.toEntity(documentDefinitionName, documentDefinitionVersion)
        documentObjectenApiSyncManagementService.saveSyncConfiguration(syncConfiguration)
        return ResponseEntity.ok().build()
    }

    @DeleteMapping("/v1/document-definition/{name}/version/{version}/objecten-api-sync")
    fun deleteSyncConfiguration(
        @LoggableResource("documentDefinitionName") @PathVariable(name = "name") documentDefinitionName: String,
        @PathVariable(name = "version") documentDefinitionVersion: Long,
    ): ResponseEntity<Unit> {
        documentObjectenApiSyncManagementService.deleteSyncConfigurationByDocumentDefinition(
            documentDefinitionName,
            documentDefinitionVersion
        )
        return ResponseEntity.ok().build()
    }
}
