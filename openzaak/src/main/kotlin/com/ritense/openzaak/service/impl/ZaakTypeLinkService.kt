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

package com.ritense.openzaak.service.impl

import com.ritense.authorization.AuthorizationContext
import com.ritense.openzaak.domain.mapping.impl.ServiceTaskHandlers
import com.ritense.openzaak.domain.mapping.impl.ZaakTypeLink
import com.ritense.openzaak.domain.mapping.impl.ZaakTypeLinkId
import com.ritense.openzaak.domain.request.CreateZaakTypeLinkRequest
import com.ritense.openzaak.repository.ZaakTypeLinkRepository
import com.ritense.openzaak.service.ZaakTypeLinkService
import com.ritense.openzaak.service.impl.result.CreateServiceTaskHandlerResultSucceeded
import com.ritense.openzaak.service.impl.result.CreateZaakTypeLinkResultFailed
import com.ritense.openzaak.service.impl.result.CreateZaaktypeLinkResultSucceeded
import com.ritense.openzaak.service.impl.result.ModifyServiceTaskHandlerResultSucceeded
import com.ritense.openzaak.service.impl.result.RemoveServiceTaskHandlerResultSucceeded
import com.ritense.openzaak.service.result.CreateServiceTaskHandlerResult
import com.ritense.openzaak.service.result.CreateZaakTypeLinkResult
import com.ritense.openzaak.service.result.ModifyServiceTaskHandlerResult
import com.ritense.openzaak.service.result.RemoveServiceTaskHandlerResult
import com.ritense.openzaak.web.rest.request.ServiceTaskHandlerRequest
import com.ritense.processdocument.domain.impl.CamundaProcessDefinitionKey
import com.ritense.processdocument.service.ProcessDocumentAssociationService
import com.ritense.valtimo.contract.result.OperationError
import org.springframework.transaction.annotation.Transactional
import java.util.UUID
import javax.validation.ConstraintViolationException

@Transactional
class ZaakTypeLinkService(
    private val zaakTypeLinkRepository: ZaakTypeLinkRepository,
    private val processDocumentAssociationService: ProcessDocumentAssociationService
) : ZaakTypeLinkService {

    override fun get(documentDefinitionName: String): ZaakTypeLink? {
        return zaakTypeLinkRepository.findByDocumentDefinitionName(documentDefinitionName)
    }

    fun getByProcess(processDefinitionKey: String): List<ZaakTypeLink?> {
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

    override fun findBy(documentDefinitionName: String): ZaakTypeLink {
        return when (val zaakTypeLink = zaakTypeLinkRepository.findByDocumentDefinitionName(documentDefinitionName)) {
            null -> throw IllegalStateException("Zaak type link not found")
            else -> zaakTypeLink
        }
    }

    override fun createZaakTypeLink(request: CreateZaakTypeLinkRequest): CreateZaakTypeLinkResult {
        return try {
            var zaakTypeLink = zaakTypeLinkRepository.findByDocumentDefinitionName(request.documentDefinitionName)
            if (zaakTypeLink == null) {
                zaakTypeLink = ZaakTypeLink(
                    ZaakTypeLinkId.newId(UUID.randomUUID()),
                    request.documentDefinitionName,
                    request.zaakTypeUrl,
                    ServiceTaskHandlers(),
                    request.createWithDossier ?: false
                )
            } else {
                zaakTypeLink.processUpdateRequest(request)
            }
            zaakTypeLinkRepository.save(zaakTypeLink)
            return CreateZaaktypeLinkResultSucceeded(zaakTypeLink)
        } catch (ex: ConstraintViolationException) {
            val errors: List<OperationError> = ex.constraintViolations.map { OperationError.FromString(it.message) }
            CreateZaakTypeLinkResultFailed(errors)
        } catch (ex: RuntimeException) {
            CreateZaakTypeLinkResultFailed(listOf(OperationError.FromException(ex)))
        }
    }

    override fun deleteZaakTypeLinkBy(documentDefinitionName: String) {
        zaakTypeLinkRepository.deleteByDocumentDefinitionName(documentDefinitionName)
    }

    private fun findBy(id: ZaakTypeLinkId): ZaakTypeLink {
        return zaakTypeLinkRepository.getById(id)
    }

    override fun assignServiceTaskHandler(
        zaakTypeLinkId: ZaakTypeLinkId,
        request: ServiceTaskHandlerRequest
    ): CreateServiceTaskHandlerResult {
        val zaakTypeLink = findBy(zaakTypeLinkId)
        zaakTypeLink.assignZaakServiceHandler(request)
        zaakTypeLinkRepository.save(zaakTypeLink)
        return CreateServiceTaskHandlerResultSucceeded(zaakTypeLink)
    }

    override fun modifyServiceTaskHandler(
        zaakTypeLinkId: ZaakTypeLinkId,
        request: ServiceTaskHandlerRequest
    ): ModifyServiceTaskHandlerResult {
        val zaakTypeLink = findBy(zaakTypeLinkId)
        zaakTypeLink.assignZaakServiceHandler(request)
        zaakTypeLinkRepository.save(zaakTypeLink)
        return ModifyServiceTaskHandlerResultSucceeded(zaakTypeLink)
    }

    override fun removeServiceTaskHandler(
        zaakTypeLinkId: ZaakTypeLinkId,
        processDefinitionKey: String,
        serviceTaskId: String
    ): RemoveServiceTaskHandlerResult {
        val zaakTypeLink = findBy(zaakTypeLinkId)
        zaakTypeLink.removeZaakServiceHandler(processDefinitionKey, serviceTaskId)
        zaakTypeLinkRepository.save(zaakTypeLink)
        return RemoveServiceTaskHandlerResultSucceeded(zaakTypeLink)
    }

    override fun modify(zaakTypeLink: ZaakTypeLink) {
        zaakTypeLinkRepository.save(zaakTypeLink)
    }
}