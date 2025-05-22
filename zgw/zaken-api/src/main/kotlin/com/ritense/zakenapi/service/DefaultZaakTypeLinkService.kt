/*
 * Copyright 2020 Dimpact.
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

package com.ritense.zakenapi.service

import com.ritense.authorization.AuthorizationContext
import com.ritense.logging.LoggableResource
import com.ritense.logging.withLoggingContext
import com.ritense.plugin.domain.PluginConfiguration
import com.ritense.processdocument.domain.impl.CamundaProcessDefinitionKey
import com.ritense.processdocument.service.ProcessDocumentAssociationService
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import com.ritense.zakenapi.domain.ZaakTypeLink
import com.ritense.zakenapi.domain.ZaakTypeLinkId
import com.ritense.zakenapi.repository.ZaakTypeLinkRepository
import com.ritense.zakenapi.web.rest.request.CreateZaakTypeLinkRequest
import com.ritense.zgw.Rsin
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Transactional
@Service
@SkipComponentScan
class DefaultZaakTypeLinkService(
    private val zaakTypeLinkRepository: ZaakTypeLinkRepository,
    private val processDocumentAssociationService: ProcessDocumentAssociationService
) : ZaakTypeLinkService {

    override fun get(
        @LoggableResource("documentDefinitionName") documentDefinitionName: String
    ): ZaakTypeLink? {
        return zaakTypeLinkRepository.findByDocumentDefinitionName(documentDefinitionName)
    }

    override fun getByPluginConfigurationId(
        @LoggableResource(resourceType = PluginConfiguration::class) id: UUID
    ): List<ZaakTypeLink> {
        return zaakTypeLinkRepository.findByZakenApiPluginConfigurationId(id)
    }

    override fun getByProcess(
        @LoggableResource("processDefinitionKey") processDefinitionKey: String
    ): List<ZaakTypeLink> {
        val processDocumentDefinitions = AuthorizationContext.runWithoutAuthorization {
            processDocumentAssociationService.findAllProcessDocumentDefinitions(
                CamundaProcessDefinitionKey(processDefinitionKey)
            )
        }
        if (processDocumentDefinitions.isNotEmpty()) {
            val documentDefinitionsNames = processDocumentDefinitions
                .map { it.processDocumentDefinitionId().documentDefinitionId().name() }.toList()
            return zaakTypeLinkRepository.findByDocumentDefinitionNameIn(documentDefinitionsNames)
        }
        return emptyList()
    }

    override fun createZaakTypeLink(request: CreateZaakTypeLinkRequest): ZaakTypeLink {
        return withLoggingContext("documentDefinitionName", request.documentDefinitionName) {
            var zaakTypeLink = zaakTypeLinkRepository.findByDocumentDefinitionName(request.documentDefinitionName)
            if (zaakTypeLink == null) {
                zaakTypeLink = ZaakTypeLink(
                    ZaakTypeLinkId.newId(UUID.randomUUID()),
                    request.documentDefinitionName,
                    request.zaakTypeUrl,
                    request.createWithDossier ?: false,
                    request.zakenApiPluginConfigurationId,
                    request.rsin?.let { Rsin(it) }
                )
            } else {
                zaakTypeLink.processUpdateRequest(request)
            }
            val newZaakTypeLink = zaakTypeLinkRepository.save(zaakTypeLink)
            newZaakTypeLink
        }
    }

    override fun deleteZaakTypeLinkBy(
        @LoggableResource("documentDefinitionName") documentDefinitionName: String
    ) {
        zaakTypeLinkRepository.deleteByDocumentDefinitionName(documentDefinitionName)
    }

    override fun modify(zaakTypeLink: ZaakTypeLink) {
        zaakTypeLinkRepository.save(zaakTypeLink)
    }
}