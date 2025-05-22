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

import com.ritense.case_.domain.definition.CaseDefinition
import com.ritense.logging.LoggableResource
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import com.ritense.valtimo.contract.case_.CaseDefinitionChecker
import com.ritense.valtimo.contract.case_.CaseDefinitionId
import com.ritense.zaakdetails.documentobjectenapisync.DocumentObjectenApiSyncService.Companion.logger
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Transactional
@Service
@SkipComponentScan
class DocumentObjectenApiSyncManagementService(
    private val documentObjectenApiSyncRepository: DocumentObjectenApiSyncRepository,
    private val caseDefinitionChecker: CaseDefinitionChecker,
) {
    fun getSyncConfiguration(
        @LoggableResource(resourceType = CaseDefinition ::class) caseDefinitionId: CaseDefinitionId
    ): DocumentObjectenApiSync? {
        logger.debug { "Get sync configuration caseDefinitionId=$caseDefinitionId" }
        return documentObjectenApiSyncRepository.findByCaseDefinitionId(caseDefinitionId)
    }

    fun saveSyncConfiguration(sync: DocumentObjectenApiSync) {
        logger.info { "Save sync configuration caseDefinitionId=${sync.caseDefinitionId}" }
        caseDefinitionChecker.assertCanUpdateCaseDefinition(sync.caseDefinitionId)
        val modifiedSync = getSyncConfiguration(sync.caseDefinitionId)
            ?.copy(
                objectManagementConfigurationId = sync.objectManagementConfigurationId,
                enabled = sync.enabled
            )
            ?: sync

        documentObjectenApiSyncRepository.save(modifiedSync)
    }

    fun deleteSyncConfigurationByDocumentDefinition(
        @LoggableResource(resourceType = CaseDefinition ::class) caseDefinitionId: CaseDefinitionId
    ) {
        logger.info {
            "Delete sync configuration caseDefinitionId=$caseDefinitionId"
        }
        caseDefinitionChecker.assertCanUpdateCaseDefinition(caseDefinitionId)
        documentObjectenApiSyncRepository.deleteByCaseDefinitionId(caseDefinitionId)
    }
}