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

import com.ritense.BaseTest
import com.ritense.authorization.AuthorizationService
import com.ritense.authorization.request.EntityAuthorizationRequest
import com.ritense.authorization.specification.AuthorizationSpecification
import com.ritense.case.domain.ColumnDefaultSort
import com.ritense.case.domain.DisplayType
import com.ritense.case.domain.EnumDisplayTypeParameter
import com.ritense.case.exception.InvalidListColumnException
import com.ritense.case.exception.UnknownCaseDefinitionException
import com.ritense.case.repository.CaseDefinitionListColumnRepository
import com.ritense.case.web.rest.dto.CaseListColumnDto
import com.ritense.case.web.rest.dto.CaseSettingsDto
import com.ritense.case.web.rest.mapper.CaseListColumnMapper
import com.ritense.case_.domain.definition.CaseDefinition
import com.ritense.case_.repository.CaseDefinitionRepository
import com.ritense.document.service.DocumentDefinitionService
import com.ritense.valtimo.contract.case_.CaseDefinitionId
import com.ritense.valueresolver.ValueResolverService
import com.ritense.valueresolver.exception.ValueResolverValidationException
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import java.util.Optional
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CaseDefinitionServiceTest : BaseTest() {
    lateinit var caseDefinitionRepository: CaseDefinitionRepository
    lateinit var caseDefinitionListColumnRepository: CaseDefinitionListColumnRepository
    lateinit var service: CaseDefinitionService
    lateinit var documentDefinitionService: DocumentDefinitionService
    lateinit var valueResolverService: ValueResolverService
    lateinit var authorizationService: AuthorizationService

    @BeforeEach
    fun setUp() {
        documentDefinitionService = mock()
        caseDefinitionListColumnRepository = mock()
        caseDefinitionRepository = mock()
        valueResolverService = mock()
        authorizationService = mock()
        service = CaseDefinitionService(
            caseDefinitionListColumnRepository,
            documentDefinitionService,
            caseDefinitionRepository,
            valueResolverService,
            authorizationService,
            mock(),
            mock(),
        )
    }

    @Test
    fun `should get case definition by id`() {
        val externalFormUrl = "https://www.example.com/external-form"
        val caseDefinition = caseDefinition(
            canHaveAssignee = true,
            hasExternalStartForm = true,
            externalStartFormUrl = externalFormUrl
        )

        whenever(caseDefinitionRepository.findById(caseDefinition.id)).thenReturn(Optional.of(caseDefinition))

        val foundCaseDefinitionSettings = service.getCaseDefinition(caseDefinition.id)

        assertEquals(caseDefinition.name, foundCaseDefinitionSettings.name)
        assertTrue(foundCaseDefinitionSettings.canHaveAssignee)
    }

    @Test
    fun `should throw exception when getting case settings by id and document definition does not exist `() {
        val caseDefinitionId = CaseDefinitionId.of("name", "1.0.0")

        assertThrows<UnknownCaseDefinitionException> {
            service.getCaseDefinition(caseDefinitionId)
        }
    }

    @Test
    fun `should update case settings`() {
        val currentCaseDefinition = caseDefinition(
            canHaveAssignee = true
        )
        val updatedCaseDefinition = caseDefinition(
            canHaveAssignee = false
        )
        val caseSettingsDto: CaseSettingsDto = mock()
        whenever(caseDefinitionRepository.findById(eq(currentCaseDefinition.id))).thenReturn(
            Optional.of(
                currentCaseDefinition
            )
        )
        whenever(caseDefinitionRepository.save(updatedCaseDefinition)).thenReturn(
            updatedCaseDefinition
        )
        whenever(caseSettingsDto.update(currentCaseDefinition)).thenReturn(updatedCaseDefinition)
        val returnedCaseDefinitionSettings = service.updateCaseSettings(currentCaseDefinition.id, caseSettingsDto)

        assertEquals("name", returnedCaseDefinitionSettings.name)
        assertFalse(returnedCaseDefinitionSettings.canHaveAssignee)
    }

    @Test
    fun `should throw exception when updating case settings and case definition does not exist `() {
        val caseDefinitionId = CaseDefinitionId.of("name", "1.0.0")
        val caseSettingsDto: CaseSettingsDto = mock()

        assertThrows<UnknownCaseDefinitionException> {
            service.updateCaseSettings(caseDefinitionId, caseSettingsDto)
        }
    }

    @Test
    fun `should fail to validate list column on create when a key already exists`() {
        val caseDefinitionName = "aName"
        val listColumnDto = getListColumnDtoToFirstName(
            DisplayType("enum", EnumDisplayTypeParameter(mapOf(Pair("Key1", "Value1"))))
        )
        whenever(documentDefinitionService.existsByName(caseDefinitionName))
            .thenReturn(true)
        whenever(
            caseDefinitionListColumnRepository
                .existsByIdCaseDefinitionKeyAndIdKey(
                    caseDefinitionName,
                    listColumnDto.key
                )
        )
            .thenReturn(true)
        val exception = assertThrows<InvalidListColumnException> {
            service.createListColumn(caseDefinitionName, listColumnDto)
        }
        verify(documentDefinitionService).existsByName(caseDefinitionName)
        verify(caseDefinitionListColumnRepository).existsByIdCaseDefinitionKeyAndIdKey(
            caseDefinitionName,
            listColumnDto.key
        )
        assertEquals("Unable to create list column. A column with the same key already exists", exception.message)
    }

    @Test
    fun `should fail to validate list column on create when document definition doesn't exist`() {
        val caseDefinitionName = "name"
        val listColumnDto: CaseListColumnDto = mock()
        assertThrows<UnknownCaseDefinitionException> {
            service.createListColumn(caseDefinitionName, listColumnDto)
        }
    }

    @Test
    fun `should fail to validate list column on create when a default sort column already exists`() {
        val caseDefinitionName = "aName"
        val listColumnDto = getListColumnDtoToFirstName(
            DisplayType("enum", EnumDisplayTypeParameter(mapOf(Pair("Key1", "Value1"))))
        )
        whenever(documentDefinitionService.existsByName(caseDefinitionName))
            .thenReturn(true)
        whenever(
            caseDefinitionListColumnRepository.findByIdCaseDefinitionKeyOrderByOrderAsc(
                caseDefinitionName
            )
        )
            .thenReturn(
                listOf(
                    CaseListColumnMapper.toEntity(caseDefinitionName, listColumnDto)
                )
            )
        val exception = assertThrows<InvalidListColumnException> {
            service.createListColumn(caseDefinitionName, listColumnDto)
        }
        verify(documentDefinitionService).existsByName(caseDefinitionName)
        verify(caseDefinitionListColumnRepository).findByIdCaseDefinitionKeyOrderByOrderAsc(
            caseDefinitionName
        )
        assertEquals("Unable to create list column. A column with defaultSort value already exists", exception.message)

    }

    @Test
    fun `should fail to validate list column on create when a json path is invalid`() {
        val caseDefinitionName = "aName"
        val listColumnDto = getListColumnDtoToFirstName(
            DisplayType("enum", EnumDisplayTypeParameter(mapOf(Pair("Key1", "Value1"))))
        )
        whenever(documentDefinitionService.existsByName(caseDefinitionName))
            .thenReturn(true)
        whenever(
            caseDefinitionListColumnRepository.findByIdCaseDefinitionKeyOrderByOrderAsc(
                caseDefinitionName
            )
        )
            .thenReturn(
                emptyList()
            )
        doAnswer {
            throw ValueResolverValidationException(
                "JsonPath '"
                    + listColumnDto.path +
                    "' doesn't point to any property inside document definition '" + caseDefinitionName + "'"
            )
        }
            .whenever(valueResolverService).validateValues(caseDefinitionName, listOf(listColumnDto.path))
        val exception = assertThrows<InvalidListColumnException> {
            service.createListColumn(caseDefinitionName, listColumnDto)
        }
        verify(documentDefinitionService).existsByName(caseDefinitionName)
        verify(caseDefinitionListColumnRepository).findByIdCaseDefinitionKeyOrderByOrderAsc(
            caseDefinitionName
        )
        verify(valueResolverService).validateValues(caseDefinitionName, listOf(listColumnDto.path))
        assertEquals(
            "JsonPath '"
                + listColumnDto.path +
                "' doesn't point to any property inside document definition '" + caseDefinitionName + "'",
            exception.message
        )

    }

    @Test
    fun `should fail to validate list column dto on create`() {
        val caseDefinitionName = "aName"
        val listColumnDto = getListColumnDtoToFirstName(
            DisplayType("enum", EnumDisplayTypeParameter(emptyMap()))
        )
        whenever(documentDefinitionService.existsByName(caseDefinitionName))
            .thenReturn(true)
        whenever(
            caseDefinitionListColumnRepository.findByIdCaseDefinitionKeyOrderByOrderAsc(
                caseDefinitionName
            )
        )
            .thenReturn(
                emptyList()
            )
        doNothing().whenever(valueResolverService).validateValues(caseDefinitionName, listOf(listColumnDto.path))
        val exception = assertThrows<InvalidListColumnException> {
            service.createListColumn(caseDefinitionName, listColumnDto)
        }
        verify(documentDefinitionService).existsByName(caseDefinitionName)
        verify(caseDefinitionListColumnRepository).findByIdCaseDefinitionKeyOrderByOrderAsc(
            caseDefinitionName
        )
        verify(valueResolverService).validateValues(caseDefinitionName, listOf(listColumnDto.path))
        assertEquals("Display type parameters are invalid for type enum.", exception.message)

    }

    @Test
    fun `should fail to validate column on update when document definition doesn't exist`() {
        val caseDefinitionName = "name"
        val listColumnDto: CaseListColumnDto = mock()
        assertThrows<UnknownCaseDefinitionException> {
            service.createListColumn(caseDefinitionName, listColumnDto)
        }
    }

    @Test
    fun `should fail to validate column on update when list has more than 1 defaultSort column`() {
        val caseDefinitionName = "aName"
        val listColumnDtoFirstName = getListColumnDtoToFirstName(
            DisplayType("enum", EnumDisplayTypeParameter(mapOf(Pair("Key1", "Value1"))))
        )
        val listColumnDtoLastName = getListColumnDtoLastName(
            DisplayType("enum", EnumDisplayTypeParameter(mapOf(Pair("Key1", "Value1"))))
        )
        listColumnDtoLastName.defaultSort = ColumnDefaultSort.ASC
        whenever(documentDefinitionService.existsByName(caseDefinitionName))
            .thenReturn(true)
        whenever(
            caseDefinitionListColumnRepository.findByIdCaseDefinitionKeyOrderByOrderAsc(
                caseDefinitionName
            )
        )
            .thenReturn(
                listOf(
                    CaseListColumnMapper.toEntity(caseDefinitionName, listColumnDtoFirstName),
                    CaseListColumnMapper.toEntity(caseDefinitionName, listColumnDtoLastName)
                )
            )
        val exception = assertThrows<InvalidListColumnException> {
            service.updateListColumns(
                caseDefinitionName,
                listOf(listColumnDtoFirstName, listColumnDtoLastName)
            )
        }
        verify(documentDefinitionService).existsByName(caseDefinitionName)
        verify(caseDefinitionListColumnRepository).findByIdCaseDefinitionKeyOrderByOrderAsc(
            caseDefinitionName
        )
        assertEquals("Invalid set of columns. There is more than 1 column with default sort value", exception.message)
    }

    @Test
    fun `should fail to validate column on update when at least one element has invalid jsonPath`() {
        val caseDefinitionName = "aName"
        val listColumnDtoFirstName = getListColumnDtoToFirstName(
            DisplayType("enum", EnumDisplayTypeParameter(mapOf(Pair("Key1", "Value1"))))
        )
        val listColumnDtoLastName = getListColumnDtoLastName(
            DisplayType("enum", EnumDisplayTypeParameter(mapOf(Pair("Key1", "Value1"))))
        )
        whenever(documentDefinitionService.existsByName(caseDefinitionName))
            .thenReturn(true)
        whenever(
            caseDefinitionListColumnRepository.findByIdCaseDefinitionKeyOrderByOrderAsc(
                caseDefinitionName
            )
        )
            .thenReturn(
                listOf(
                    CaseListColumnMapper.toEntity(caseDefinitionName, listColumnDtoFirstName),
                    CaseListColumnMapper.toEntity(caseDefinitionName, listColumnDtoLastName)
                )
            )
        doAnswer {
            throw ValueResolverValidationException(
                "JsonPath '"
                    + listColumnDtoFirstName.path +
                    "' doesn't point to any property inside document definition '" + caseDefinitionName + "'"
            )
        }
            .whenever(valueResolverService).validateValues(caseDefinitionName, listOf(listColumnDtoFirstName.path))
        val exception = assertThrows<InvalidListColumnException> {
            service.updateListColumns(caseDefinitionName, listOf(listColumnDtoFirstName))
        }
        verify(documentDefinitionService).existsByName(caseDefinitionName)
        verify(caseDefinitionListColumnRepository).findByIdCaseDefinitionKeyOrderByOrderAsc(
            caseDefinitionName
        )
        verify(valueResolverService).validateValues(caseDefinitionName, listOf(listColumnDtoFirstName.path))
        assertEquals(
            "JsonPath '"
                + listColumnDtoFirstName.path +
                "' doesn't point to any property inside document definition '" + caseDefinitionName + "'",
            exception.message
        )
    }

    @Test
    fun `should not delete an active draft when more drafts exist`() {
        val caseDefinitionId = CaseDefinitionId("key", "1.0.0")
        val caseDefinition = caseDefinition(id = caseDefinitionId, active = true, final = false)
        whenever(caseDefinitionRepository.findAll(any(), any<Pageable>()))
            .thenReturn(PageImpl(listOf<CaseDefinition>(caseDefinition, mock())))
        whenever(caseDefinitionRepository.findById(caseDefinitionId))
            .thenReturn(Optional.of(caseDefinition))
        val spec = mock<AuthorizationSpecification<CaseDefinition>>()
        whenever(authorizationService.getAuthorizationSpecification<CaseDefinition>(any(), eq(null)))
            .thenReturn(spec)
        whenever(spec.and(any())).thenReturn(spec)

        assertEquals("Failed to delete case-definition. Case-definition with id: '$caseDefinitionId' is the global active version.", assertThrows<Exception> {
            service.deleteCaseDefinition(caseDefinitionId)
        }.message)
    }

    @Test
    fun `should delete an active draft when it is the last one`() {
        val caseDefinitionId = CaseDefinitionId("key", "1.0.0")
        val caseDefinition = caseDefinition(id = caseDefinitionId, active = true, final = false)
        whenever(caseDefinitionRepository.findAll(any(), any<Pageable>()))
            .thenReturn(PageImpl(listOf(caseDefinition)))
        whenever(caseDefinitionRepository.findById(caseDefinitionId))
            .thenReturn(Optional.of(caseDefinition))
        val spec = mock<AuthorizationSpecification<CaseDefinition>>()
        whenever(authorizationService.getAuthorizationSpecification<CaseDefinition>(any(), eq(null)))
            .thenReturn(spec)
        whenever(spec.and(any())).thenReturn(spec)

        service.deleteCaseDefinition(caseDefinitionId)
    }

    private fun getListColumnDtoToFirstName(displayType: DisplayType): CaseListColumnDto {
        return CaseListColumnDto(
            title = "First name",
            key = "first-name",
            path = "doc:firstName",
            displayType = displayType,
            sortable = true,
            defaultSort = ColumnDefaultSort.ASC,
            order = 1
        )
    }

    private fun getListColumnDtoLastName(displayType: DisplayType): CaseListColumnDto {
        return CaseListColumnDto(
            title = "Last name",
            key = "last-name",
            path = "doc:lastName",
            displayType = displayType,
            sortable = true,
            defaultSort = null,
            order = 2
        )
    }
}
