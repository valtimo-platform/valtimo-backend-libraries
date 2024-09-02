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

package com.ritense.case_.service

import com.ritense.authorization.Action
import com.ritense.authorization.Action.Companion.deny
import com.ritense.authorization.AuthorizationContext.Companion.runWithoutAuthorization
import com.ritense.authorization.AuthorizationService
import com.ritense.authorization.request.AuthorizationResourceContext
import com.ritense.authorization.request.EntityAuthorizationRequest
import com.ritense.case.domain.CaseTab
import com.ritense.case.domain.CaseTabId
import com.ritense.case.domain.CaseTabType
import com.ritense.case.repository.CaseTabRepository
import com.ritense.case.service.CaseTabActionProvider.Companion.VIEW
import com.ritense.case_.domain.tab.CaseWidgetTab
import com.ritense.case_.domain.tab.CaseWidgetTabWidget
import com.ritense.case_.repository.CaseWidgetTabRepository
import com.ritense.case_.rest.dto.CaseWidgetTabDto
import com.ritense.case_.rest.dto.CaseWidgetTabWidgetDto
import com.ritense.case_.service.event.CaseTabCreatedEvent
import com.ritense.case_.widget.CaseWidgetDataProvider
import com.ritense.case_.widget.CaseWidgetMapper
import com.ritense.document.domain.impl.JsonSchemaDocument
import com.ritense.document.domain.impl.JsonSchemaDocumentId
import com.ritense.document.service.DocumentService
import com.ritense.document.service.findByOrNull
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import jakarta.validation.Valid
import org.springframework.context.event.EventListener
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.validation.annotation.Validated
import java.util.UUID

@Validated
@Transactional(readOnly = false)
@Service
@SkipComponentScan
class CaseWidgetTabService(
    private val documentService: DocumentService,
    private val caseWidgetTabRepository: CaseWidgetTabRepository,
    private val caseTabRepository: CaseTabRepository,
    private val authorizationService: AuthorizationService,
    private val caseWidgetMappers: List<CaseWidgetMapper<CaseWidgetTabWidget, CaseWidgetTabWidgetDto>>,
    private val caseWidgetDataProviders: List<CaseWidgetDataProvider<CaseWidgetTabWidget>>
) {

    @EventListener(CaseTabCreatedEvent::class)
    fun handleCaseTabCreatedEvent(event: CaseTabCreatedEvent) {
        if (event.tab.type == CaseTabType.WIDGETS) {
            caseWidgetTabRepository.save(CaseWidgetTab(event.tab.id))
        }
    }

    fun getWidgetTab(caseDefinitionName: String, key: String): CaseWidgetTabDto? {
        checkCaseTabAccess(caseDefinitionName, key, VIEW)

        return caseWidgetTabRepository.findByIdOrNull(CaseTabId(caseDefinitionName, key))
            ?.let { CaseWidgetTabDto.of(it, caseWidgetMappers, this::viewPermissionCheck) }

    }

    fun getWidgetTab(documentId: JsonSchemaDocumentId, key: String): CaseWidgetTabDto? {
        val document = runWithoutAuthorization { documentService.findByOrNull(documentId) }

        return document?.let { existingDocument ->
            caseTabRepository.findByIdOrNull(CaseTabId(document.definitionId().name(), key))?.let { caseTab ->
                checkCaseTabAccess(existingDocument as JsonSchemaDocument, caseTab, VIEW)
                caseWidgetTabRepository.findByIdOrNull(CaseTabId(existingDocument.definitionId().name(), key))
                    ?.let { widgetTab ->
                        CaseWidgetTabDto
                        .ofWithContext(
                            widgetTab,
                            caseWidgetMappers,
                            this::viewPermissionCheckForContext,
                            document as JsonSchemaDocument
                        )
                    }
            }
        }

    }

    @Transactional
    fun updateWidgetTab(@Valid tabDto: CaseWidgetTabDto): CaseWidgetTabDto {
        denyAuthorization()

        val caseWidgetTab = (caseWidgetTabRepository.findByIdOrNull(CaseTabId(tabDto.caseDefinitionName, tabDto.key))
            ?: throw RuntimeException(
                "Failed to update dashboard. Dashboard with key '${tabDto.key}' doesn't exist " +
                    "for case definition with name '${tabDto.caseDefinitionName}'."
            )
            ).copy(
                widgets = tabDto.widgets.mapIndexed { index, widgetDto ->
                    caseWidgetMappers.first { mapper ->
                        mapper.supportedDtoType().isAssignableFrom(widgetDto::class.java)
                    }.toEntity(widgetDto, index)
                }
            )

        return CaseWidgetTabDto.of(
            caseWidgetTabRepository.save(caseWidgetTab),
            caseWidgetMappers,
            this::viewPermissionCheck
        )
    }

    @Transactional
    fun getCaseWidgetData(documentId: UUID, tabKey: String, widgetKey: String, pageable: Pageable): Any? {
        val document = runWithoutAuthorization {
            documentService.findByOrNull(JsonSchemaDocumentId.existingId(documentId))
        } ?: return null

        val caseDefinitionName = document.definitionId().name()
        checkCaseTabAccess(caseDefinitionName, tabKey, VIEW)

        val widgetTab = caseWidgetTabRepository.findByIdOrNull(CaseTabId(caseDefinitionName, tabKey)) ?: return null
        val widget = widgetTab.widgets.firstOrNull { it.id.key == widgetKey } ?: return null

        authorizationService.requirePermission(
            EntityAuthorizationRequest(
                CaseWidgetTabWidget::class.java,
                CaseWidgetTabWidgetActionProvider.VIEW,
                widget
            ).withContext(
                AuthorizationResourceContext(
                    JsonSchemaDocument::class.java,
                    document as JsonSchemaDocument
                )
            )
        )

        return runWithoutAuthorization {
            caseWidgetDataProviders
                .first { provider -> provider.supportedWidgetType().isAssignableFrom(widget::class.java) }
                .getData(document.id().id, widgetTab, widget, pageable)
        }
    }

    private fun checkCaseTabAccess(caseDefinitionName: String, key: String, action: Action<CaseTab>) {
        caseTabRepository.findByIdOrNull(CaseTabId(caseDefinitionName, key))?.let { caseTab ->
            authorizationService.requirePermission(
                EntityAuthorizationRequest(
                    CaseTab::class.java,
                    action,
                    caseTab
                )
            )
        }
    }

    private fun checkCaseTabAccess(
        document: JsonSchemaDocument,
        caseTab: CaseTab,
        action: Action<CaseTab>
    ) {
        authorizationService.requirePermission(
            EntityAuthorizationRequest(
                CaseTab::class.java,
                action,
                caseTab
            ).withContext(
                AuthorizationResourceContext(
                    JsonSchemaDocument::class.java,
                    document
                )
            )
        )
    }

    private fun denyAuthorization() {
        authorizationService.requirePermission(
            EntityAuthorizationRequest(
                CaseTab::class.java,
                deny()
            )
        )
    }

    private fun viewPermissionCheck(widget: CaseWidgetTabWidget): Boolean {
        return authorizationService.hasPermission(
            EntityAuthorizationRequest(
                CaseWidgetTabWidget::class.java,
                CaseWidgetTabWidgetActionProvider.VIEW,
                widget
            )
        )
    }

    private fun viewPermissionCheckForContext(widget: CaseWidgetTabWidget, document: JsonSchemaDocument): Boolean {
        return authorizationService.hasPermission(
            EntityAuthorizationRequest(
                CaseWidgetTabWidget::class.java,
                CaseWidgetTabWidgetActionProvider.VIEW,
                widget
            ).withContext(
                AuthorizationResourceContext(
                    JsonSchemaDocument::class.java,
                    document
                )
            )
        )
    }
}