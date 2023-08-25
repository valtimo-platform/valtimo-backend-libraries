/*
 * Copyright 2015-2023 Ritense BV, the Netherlands.
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
import com.ritense.case.domain.CaseDefinitionSettings
import com.ritense.case.domain.ColumnDefaultSort
import com.ritense.case.domain.DisplayType
import com.ritense.case.domain.EnumDisplayTypeParameter
import com.ritense.case.exception.InvalidListColumnException
import com.ritense.case.exception.UnknownCaseDefinitionException
import com.ritense.case.repository.CaseDefinitionListColumnRepository
import com.ritense.case.repository.CaseDefinitionSettingsRepository
import com.ritense.case.web.rest.dto.CaseListColumnDto
import com.ritense.case.web.rest.dto.CaseSettingsDto
import com.ritense.case.web.rest.mapper.CaseListColumnMapper
import com.ritense.document.domain.impl.JsonSchemaDocumentDefinitionId
import com.ritense.document.exception.UnknownDocumentDefinitionException
import com.ritense.document.service.DocumentDefinitionService
import com.ritense.valueresolver.ValueResolverService
import com.ritense.valueresolver.exception.ValueResolverValidationException
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Optional

class CaseDefinitionServiceTest {
    lateinit var caseDefinitionSettingsRepository: CaseDefinitionSettingsRepository

    lateinit var caseDefinitionListColumnRepository: CaseDefinitionListColumnRepository

    lateinit var service: CaseDefinitionService

    lateinit var documentDefinitionService: DocumentDefinitionService

    lateinit var valueResolverService: ValueResolverService

    @BeforeEach
    fun setUp() {
        caseDefinitionSettingsRepository = mock()
        documentDefinitionService = mock()
        caseDefinitionListColumnRepository = mock()
        valueResolverService = mock()
        service = CaseDefinitionService(
            caseDefinitionSettingsRepository,
            caseDefinitionListColumnRepository,
            documentDefinitionService,
            valueResolverService,
            mock()
        )
    }

    @Test
    fun `should get case settings by id`() {
        val caseDefinitionName = "name"
        val caseDefinitionSettings = CaseDefinitionSettings(caseDefinitionName, true)

        whenever(documentDefinitionService.findLatestByName(caseDefinitionName)).thenReturn(Optional.of(mock()))
        whenever(caseDefinitionSettingsRepository.getReferenceById(caseDefinitionName)).thenReturn(caseDefinitionSettings)

        val foundCaseDefinitionSettings = service.getCaseSettings(caseDefinitionName)

        verify(caseDefinitionSettingsRepository).getReferenceById(caseDefinitionName)
        assertEquals(caseDefinitionName, foundCaseDefinitionSettings.name)
        assertTrue(foundCaseDefinitionSettings.canHaveAssignee)
    }

    @Test
    fun `should throw exception when getting case settings by id and document definition does not exist `() {
        val caseDefinitionName = "name"

        assertThrows<UnknownCaseDefinitionException> {
            service.getCaseSettings(caseDefinitionName)
        }
    }

    @Test
    fun `should update case settings`() {
        val caseDefinitionName = "name"
        val currentCaseDefinitionSettings = CaseDefinitionSettings(caseDefinitionName, true)
        val updatedCaseDefinitionSettings = CaseDefinitionSettings(caseDefinitionName, false)
        val caseSettingsDto: CaseSettingsDto = mock()
        whenever(documentDefinitionService.findLatestByName(caseDefinitionName)).thenReturn(Optional.of(mock()))
        whenever(caseDefinitionSettingsRepository.getReferenceById(caseDefinitionName)).thenReturn(currentCaseDefinitionSettings)
        whenever(caseDefinitionSettingsRepository.save(updatedCaseDefinitionSettings)).thenReturn(
            updatedCaseDefinitionSettings
        )
        whenever(caseSettingsDto.update(currentCaseDefinitionSettings)).thenReturn(updatedCaseDefinitionSettings)
        val returnedCaseDefinitionSettings = service.updateCaseSettings(caseDefinitionName, caseSettingsDto)
        verify(caseDefinitionSettingsRepository).getReferenceById(caseDefinitionName)
        assertEquals(caseDefinitionName, returnedCaseDefinitionSettings.name)
        assertFalse(returnedCaseDefinitionSettings.canHaveAssignee)
    }

    @Test
    fun `should throw exception when updating case settings and document definition does not exist `() {
        val caseDefinitionName = "name"
        val caseSettingsDto: CaseSettingsDto = mock()

        assertThrows<UnknownCaseDefinitionException> {
            service.updateCaseSettings(caseDefinitionName, caseSettingsDto)
        }
    }

    @Test
    fun `should fail to validate list column on create when a key already exists`() {
        val caseDefinitionName = "aName"
        val listColumnDto = getListColumnDtoToFirstName(
            DisplayType("enum", EnumDisplayTypeParameter(mapOf(Pair("Key1", "Value1"))))
        )
        whenever(documentDefinitionService.findIdByName(caseDefinitionName))
            .thenReturn(JsonSchemaDocumentDefinitionId.newId("aName"))
        whenever(
            caseDefinitionListColumnRepository
                .existsByIdCaseDefinitionNameAndIdKey(
                    caseDefinitionName,
                    listColumnDto.key
                )
        )
            .thenReturn(true)
        val exception = assertThrows<InvalidListColumnException> {
            service.createListColumn(caseDefinitionName, listColumnDto)
        }
        verify(documentDefinitionService).findIdByName(caseDefinitionName)
        verify(caseDefinitionListColumnRepository).existsByIdCaseDefinitionNameAndIdKey(
            caseDefinitionName,
            listColumnDto.key
        )
        assertEquals("Unable to create list column. A column with the same key already exists", exception.message)
    }

    @Test
    fun `should fail to validate list column on create when document definition doesn't exist`() {
        val caseDefinitionName = "name"
        val listColumnDto: CaseListColumnDto = mock()
        whenever(documentDefinitionService.findIdByName(any())).thenThrow(
            UnknownDocumentDefinitionException(
                caseDefinitionName
            )
        )
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
        whenever(documentDefinitionService.findIdByName(caseDefinitionName))
            .thenReturn(JsonSchemaDocumentDefinitionId.newId("aName"))
        whenever(
            caseDefinitionListColumnRepository.findByIdCaseDefinitionNameOrderByOrderAsc(
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
        verify(documentDefinitionService).findIdByName(caseDefinitionName)
        verify(caseDefinitionListColumnRepository).findByIdCaseDefinitionNameOrderByOrderAsc(
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
        whenever(documentDefinitionService.findIdByName(caseDefinitionName))
            .thenReturn(JsonSchemaDocumentDefinitionId.newId("aName"))
        whenever(
            caseDefinitionListColumnRepository.findByIdCaseDefinitionNameOrderByOrderAsc(
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
        verify(documentDefinitionService).findIdByName(caseDefinitionName)
        verify(caseDefinitionListColumnRepository).findByIdCaseDefinitionNameOrderByOrderAsc(
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
        whenever(documentDefinitionService.findIdByName(caseDefinitionName))
            .thenReturn(JsonSchemaDocumentDefinitionId.newId("aName"))
        whenever(
            caseDefinitionListColumnRepository.findByIdCaseDefinitionNameOrderByOrderAsc(
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
        verify(documentDefinitionService).findIdByName(caseDefinitionName)
        verify(caseDefinitionListColumnRepository).findByIdCaseDefinitionNameOrderByOrderAsc(
            caseDefinitionName
        )
        verify(valueResolverService).validateValues(caseDefinitionName, listOf(listColumnDto.path))
        assertEquals("Display type parameters are invalid for type enum.", exception.message)

    }

    @Test
    fun `should fail to validate column on update when document definition doesn't exist`() {
        val caseDefinitionName = "name"
        val listColumnDto: CaseListColumnDto = mock()
        whenever(documentDefinitionService.findIdByName(any())).thenThrow(
            UnknownDocumentDefinitionException(
                caseDefinitionName
            )
        )
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
        whenever(documentDefinitionService.findIdByName(caseDefinitionName))
            .thenReturn(JsonSchemaDocumentDefinitionId.newId("aName"))
        whenever(
            caseDefinitionListColumnRepository.findByIdCaseDefinitionNameOrderByOrderAsc(
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
        verify(documentDefinitionService).findIdByName(caseDefinitionName)
        verify(caseDefinitionListColumnRepository).findByIdCaseDefinitionNameOrderByOrderAsc(
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
        whenever(documentDefinitionService.findIdByName(caseDefinitionName))
            .thenReturn(JsonSchemaDocumentDefinitionId.newId("aName"))
        whenever(
            caseDefinitionListColumnRepository.findByIdCaseDefinitionNameOrderByOrderAsc(
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
        verify(documentDefinitionService).findIdByName(caseDefinitionName)
        verify(caseDefinitionListColumnRepository).findByIdCaseDefinitionNameOrderByOrderAsc(
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
