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

package com.ritense.case.service

import com.ritense.authorization.Action.Companion.deny
import com.ritense.authorization.AuthorizationContext.Companion.runWithoutAuthorization
import com.ritense.authorization.AuthorizationService
import com.ritense.authorization.request.AuthorizationResourceContext
import com.ritense.authorization.request.EntityAuthorizationRequest
import com.ritense.case.domain.CaseTab
import com.ritense.case.domain.CaseTabId
import com.ritense.case.repository.CaseTabRepository
import com.ritense.case.repository.CaseTabSpecificationHelper.Companion.TAB_ORDER
import com.ritense.case.repository.CaseTabSpecificationHelper.Companion.byCaseDefinitionName
import com.ritense.case.repository.CaseTabSpecificationHelper.Companion.byCaseDefinitionNameAndTabKey
import com.ritense.case.service.exception.TabAlreadyExistsException
import com.ritense.case.web.rest.dto.CaseTabDto
import com.ritense.case.web.rest.dto.CaseTabUpdateDto
import com.ritense.case.web.rest.dto.CaseTabUpdateOrderDto
import com.ritense.case_.service.event.CaseTabCreatedEvent
import com.ritense.document.domain.impl.JsonSchemaDocument
import com.ritense.document.domain.impl.JsonSchemaDocumentId
import com.ritense.document.service.DocumentDefinitionService
import com.ritense.document.service.DocumentService
import com.ritense.document.service.findByOrNull
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import com.ritense.valtimo.contract.authentication.UserManagementService
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import kotlin.jvm.optionals.getOrNull

@Transactional
@Service
@SkipComponentScan
class CaseTabService(
    private val caseTabRepository: CaseTabRepository,
    private val documentDefinitionService: DocumentDefinitionService,
    private val authorizationService: AuthorizationService,
    private val applicationEventPublisher: ApplicationEventPublisher,
    private val userManagementService: UserManagementService,
    private val documentService: DocumentService
) {
    fun getCaseTab(caseDefinitionName: String, key: String): CaseTab {
        val caseTab = caseTabRepository.getReferenceById(CaseTabId(caseDefinitionName, key))
        authorizationService.requirePermission(
            EntityAuthorizationRequest(
                CaseTab::class.java,
                CaseTabActionProvider.VIEW,
                caseTab
            )
        )
        return caseTab
    }

    @Transactional
    fun getCaseTabs(caseDefinitionName: String): List<CaseTab> {
        val spec = authorizationService.getAuthorizationSpecification(
            EntityAuthorizationRequest(
                CaseTab::class.java,
                CaseTabActionProvider.VIEW
            )
        )
        return caseTabRepository.findAll(spec.and(byCaseDefinitionName(caseDefinitionName)), Sort.by(TAB_ORDER))
    }

    @Transactional
    fun getCaseTabs(documentId: JsonSchemaDocumentId): List<CaseTab> {
        val document = runWithoutAuthorization { documentService.findByOrNull(documentId) }

        val spec = authorizationService.getAuthorizationSpecification(
            EntityAuthorizationRequest(
                CaseTab::class.java,
                CaseTabActionProvider.VIEW
            ).withContext(
                AuthorizationResourceContext(
                    JsonSchemaDocument::class.java,
                    document as JsonSchemaDocument
                )
            )
        )

        return caseTabRepository.findAll(
            spec.and(
                byCaseDefinitionName(
                    document.definitionId().name()
                )
            ), Sort.by(TAB_ORDER)
        )
    }

    fun createCaseTab(caseDefinitionName: String, caseTabDto: CaseTabDto): CaseTab {
        denyAuthorization()

        documentDefinitionService.findLatestByName(caseDefinitionName).getOrNull()
            ?: throw NoSuchElementException("Case definition with name $caseDefinitionName does not exist!")

        val currentTabs = getCaseTabs(caseDefinitionName)
        val tabWithKeyExists = currentTabs.any { tab ->
            tab.id.key == caseTabDto.key
        }

        if (tabWithKeyExists) {
            throw TabAlreadyExistsException(caseTabDto.key)
        }

        val caseTab = CaseTab(
            CaseTabId(caseDefinitionName, caseTabDto.key),
            caseTabDto.name,
            currentTabs.size, // Add it to the end
            caseTabDto.type,
            caseTabDto.contentKey,
            LocalDateTime.now(),
            userManagementService.currentUserId,
            caseTabDto.showTasks
        )

        val savedTab = caseTabRepository.save(caseTab)

        applicationEventPublisher.publishEvent(CaseTabCreatedEvent(savedTab))

        return savedTab
    }

    fun updateCaseTab(caseDefinitionName: String, tabKey: String, caseTab: CaseTabUpdateDto) {
        denyAuthorization()

        val existingTab = caseTabRepository.findOne(byCaseDefinitionNameAndTabKey(caseDefinitionName, tabKey)).get()

        caseTabRepository.save(
            existingTab.copy(
                name = caseTab.name,
                type = caseTab.type,
                contentKey = caseTab.contentKey,
                showTasks = caseTab.showTasks
            )
        )
    }

    fun updateCaseTabs(caseDefinitionName: String, caseTabDtos: List<CaseTabUpdateOrderDto>): List<CaseTab> {
        denyAuthorization()

        val existingTabs = caseTabRepository.findAll(byCaseDefinitionName(caseDefinitionName))
        if (existingTabs.size != caseTabDtos.size) {
            throw IllegalStateException("Failed to update tabs. Reason: the number of tabs in the update request doesn't match the number of existing tabs.")
        }

        val updatedTabs = caseTabDtos.mapIndexed { index, caseTabDto ->
            val existingTab = existingTabs.find { it.id.key == caseTabDto.key }
                ?: throw IllegalStateException("Failed to update tabs. Reason: tab with key '${caseTabDto.key}' doesn't exist.")
            existingTab.copy(
                name = caseTabDto.name,
                tabOrder = index,
                type = caseTabDto.type,
                contentKey = caseTabDto.contentKey,
                showTasks = caseTabDto.showTasks
            )
        }

        return caseTabRepository.saveAll(updatedTabs)
    }

    fun deleteCaseTab(caseDefinitionName: String, tabKey: String) {
        denyAuthorization()

        caseTabRepository.findOne(byCaseDefinitionNameAndTabKey(caseDefinitionName, tabKey))
            .ifPresent {
                caseTabRepository.delete(it)
                reorderTabs(caseDefinitionName)
            }
    }

    private fun reorderTabs(caseDefinitionName: String) {
        val caseTabs = caseTabRepository.findAll(byCaseDefinitionName(caseDefinitionName), Sort.by(TAB_ORDER))
            .mapIndexed { index, caseTab -> caseTab.copy(tabOrder = index) }
        caseTabRepository.saveAll(caseTabs)
    }

    private fun denyAuthorization() {
        authorizationService.requirePermission(
            EntityAuthorizationRequest(
                CaseTab::class.java,
                deny()
            )
        )
    }
}
