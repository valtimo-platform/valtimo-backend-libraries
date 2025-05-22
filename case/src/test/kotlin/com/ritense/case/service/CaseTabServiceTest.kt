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

import com.ritense.authorization.AuthorizationService
import com.ritense.authorization.request.EntityAuthorizationRequest
import com.ritense.authorization.specification.AuthorizationSpecification
import com.ritense.case.domain.CaseTab
import com.ritense.case.domain.CaseTabId
import com.ritense.case.domain.CaseTabType
import com.ritense.case.repository.CaseTabRepository
import com.ritense.case.web.rest.dto.CaseTabDto
import com.ritense.case_.service.event.CaseTabCreatedEvent
import com.ritense.document.service.DocumentDefinitionService
import com.ritense.document.service.DocumentService
import com.ritense.valtimo.contract.authentication.UserManagementService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.domain.Sort
import java.util.Optional


@ExtendWith(MockitoExtension::class)
class CaseTabServiceTest(
    @Mock private val caseTabRepository: CaseTabRepository,
    @Mock private val documentDefinitionService: DocumentDefinitionService,
    @Mock private val authorizationService: AuthorizationService,
    @Mock private val applicationEventPublisher: ApplicationEventPublisher,
    @Mock private val userManagementService: UserManagementService,
    @Mock private val documentService: DocumentService
) {
    private lateinit var caseTabService: CaseTabService

    @BeforeEach
    fun before() {
        caseTabService = CaseTabService(
            caseTabRepository,
            documentDefinitionService,
            authorizationService,
            applicationEventPublisher,
            userManagementService,
            documentService
        )
    }

    @Test
    fun `should publish create event`() {
        val caseDefinitionName = "myCaseDefinitionName"
        val caseTab = CaseTab(CaseTabId(caseDefinitionName, "myKey"), "myName", 0, CaseTabType.WIDGETS, "myContentKey")

        whenever(documentDefinitionService.findLatestByName(caseDefinitionName)).thenReturn(Optional.of(mock()))
        val specMock = mock<AuthorizationSpecification<CaseTab>>()
        whenever(specMock.and(any())).thenReturn(specMock)
        whenever(authorizationService.getAuthorizationSpecification(any<EntityAuthorizationRequest<CaseTab>>(), anyOrNull())).thenReturn(specMock)
        whenever(caseTabRepository.findAll(any<AuthorizationSpecification<CaseTab>>(), any<Sort>())).thenReturn(emptyList())
        whenever(caseTabRepository.save(any<CaseTab>())).thenReturn(caseTab)


        caseTabService.createCaseTab(caseDefinitionName, CaseTabDto.of(caseTab))

        verify(applicationEventPublisher).publishEvent(eq(CaseTabCreatedEvent(caseTab)))
    }

}