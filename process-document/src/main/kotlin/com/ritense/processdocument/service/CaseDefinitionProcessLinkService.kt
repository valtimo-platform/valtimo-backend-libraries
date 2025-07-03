/*
 * Copyright 2015-2025 Ritense BV, the Netherlands.
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
package com.ritense.processdocument.service

import com.ritense.authorization.AuthorizationContext.Companion.runWithoutAuthorization
import com.ritense.processdocument.domain.CaseDefinitionProcess
import com.ritense.processdocument.domain.CaseDefinitionProcessLink
import com.ritense.processdocument.domain.CaseDefinitionProcessLinkId.Companion.newId
import com.ritense.processdocument.domain.impl.request.DocumentDefinitionProcessLinkResponse
import com.ritense.processdocument.domain.impl.request.DocumentDefinitionProcessRequest
import com.ritense.processdocument.repository.CaseDefinitionProcessLinkRepository
import com.ritense.valtimo.operaton.domain.OperatonProcessDefinition
import com.ritense.valtimo.operaton.service.OperatonRepositoryService
import com.ritense.valtimo.contract.case_.CaseDefinitionChecker
import com.ritense.valtimo.contract.case_.CaseDefinitionId
import org.springframework.transaction.annotation.Transactional

@Transactional
open class CaseDefinitionProcessLinkService(
    private val caseDefinitionProcessLinkRepository: CaseDefinitionProcessLinkRepository,
    private val repositoryService: OperatonRepositoryService,
    private val caseDefinitionChecker: CaseDefinitionChecker,
) {
    fun getDocumentDefinitionProcess(caseDefinitionId: CaseDefinitionId, type: String): CaseDefinitionProcess? {
        val link = caseDefinitionProcessLinkRepository.findByIdCaseDefinitionIdAndType(caseDefinitionId, type)

        return link?.let {
            val processDefinition = runWithoutAuthorization {
                repositoryService.findLatestProcessDefinition(link.id.processDefinitionKey)
            }

            CaseDefinitionProcess(processDefinition!!.key, processDefinition.name!!)
        }
    }

    fun getDocumentDefinitionProcessLinks(caseDefinitionId: CaseDefinitionId): List<CaseDefinitionProcessLink> {
        return caseDefinitionProcessLinkRepository.findAllByIdCaseDefinitionId(caseDefinitionId)
    }

    fun getDocumentDefinitionProcessLink(
        caseDefinitionId: CaseDefinitionId,
        type: String
    ): CaseDefinitionProcessLink? {
        return caseDefinitionProcessLinkRepository.findByIdCaseDefinitionIdAndType(caseDefinitionId, type)
    }

    fun saveDocumentDefinitionProcessLink(
        caseDefinitionId: CaseDefinitionId,
        processDefinitionKey: String,
        linkType: String
    ): CaseDefinitionProcessLink {
        return caseDefinitionProcessLinkRepository.save(
            CaseDefinitionProcessLink(
                newId(
                    caseDefinitionId,
                    processDefinitionKey
                ),
                linkType
            )
        )
    }

    fun saveDocumentDefinitionProcess(
        caseDefinitionId: CaseDefinitionId,
        request: DocumentDefinitionProcessRequest
    ): DocumentDefinitionProcessLinkResponse {
        caseDefinitionChecker.assertCanUpdateCaseDefinition(caseDefinitionId)

        val processDefinition: OperatonProcessDefinition? = runWithoutAuthorization {
            repositoryService.findLatestProcessDefinition(request.getProcessDefinitionKey())
        }

        requireNotNull(processDefinition) { "Unknown process definition with key: " + request.getProcessDefinitionKey() }

        var currentLink = caseDefinitionProcessLinkRepository.findByIdCaseDefinitionIdAndType(
            caseDefinitionId,
            request.getLinkType()
        )
        if (currentLink != null) {
            // If there is already a link set for this document definition then delete the current link
            // before storing the new one
            caseDefinitionProcessLinkRepository.deleteByIdCaseDefinitionIdAndType(
                caseDefinitionId,
                request.getLinkType()
            )
        }

        val link = CaseDefinitionProcessLink(
            newId(
                caseDefinitionId,
                request.getProcessDefinitionKey()
            ),
            request.getLinkType()
        )

        caseDefinitionProcessLinkRepository.save(link)

        return DocumentDefinitionProcessLinkResponse(
            processDefinition.key,
            processDefinition.name
        )
    }

    fun deleteDocumentDefinitionProcess(caseDefinitionId: CaseDefinitionId, type: String) {
        caseDefinitionChecker.assertCanUpdateCaseDefinition(caseDefinitionId)
        caseDefinitionProcessLinkRepository.deleteByIdCaseDefinitionIdAndType(caseDefinitionId, type)
    }

    fun deleteDocumentDefinitionProcesses(caseDefinitionId: CaseDefinitionId) {
        caseDefinitionChecker.assertCanUpdateCaseDefinition(caseDefinitionId)
        caseDefinitionProcessLinkRepository.deleteAllByIdCaseDefinitionId(caseDefinitionId)
    }
}
