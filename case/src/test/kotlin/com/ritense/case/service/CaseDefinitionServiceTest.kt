package com.ritense.case.service

import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import com.ritense.case.domain.CaseDefinitionSettings
import com.ritense.case.repository.CaseDefinitionSettingsRepository
import com.ritense.case.web.rest.dto.CaseSettingsDto
import com.ritense.document.exception.UnknownDocumentDefinitionException
import com.ritense.document.service.DocumentDefinitionService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CaseDefinitionServiceTest {
    lateinit var repository: CaseDefinitionSettingsRepository

    lateinit var service: CaseDefinitionService

    lateinit var documentDefinitionService: DocumentDefinitionService

    @BeforeEach
    fun setUp() {
        repository = mock()
        documentDefinitionService = mock()
        service = CaseDefinitionService(repository, documentDefinitionService)
    }

    @Test
    fun `should get case settings by id`() {
        val caseDefinitionName = "name"
        val caseDefinitionSettings = CaseDefinitionSettings(caseDefinitionName, true)

        whenever(repository.getById(caseDefinitionName)).thenReturn(caseDefinitionSettings)

        val foundCaseDefinitionSettings = service.getCaseSettings(caseDefinitionName)

        verify(repository).getById(caseDefinitionName)
        assertEquals(caseDefinitionName, foundCaseDefinitionSettings.name)
        assertTrue(foundCaseDefinitionSettings.canHaveAssignee)
    }

    @Test
    fun `should throw exception when getting case settings by id and document definition does not exist `() {
        val caseDefinitionName = "name"

        whenever(documentDefinitionService.findIdByName(any())).thenThrow(UnknownDocumentDefinitionException(caseDefinitionName))

        assertThrows<UnknownDocumentDefinitionException> {
            val foundCaseDefinitionSettings = service.getCaseSettings(caseDefinitionName)
        }
    }

    @Test
    fun `should update case settings`() {
        val caseDefinitionName = "name"
        val currentCaseDefinitionSettings = CaseDefinitionSettings(caseDefinitionName, true)
        val updatedCaseDefinitionSettings = CaseDefinitionSettings(caseDefinitionName, false)

        val caseSettingsDto: CaseSettingsDto = mock()

        whenever(repository.getById(caseDefinitionName)).thenReturn(currentCaseDefinitionSettings)
        whenever(repository.save(updatedCaseDefinitionSettings)).thenReturn(updatedCaseDefinitionSettings)
        whenever(caseSettingsDto.update(currentCaseDefinitionSettings)).thenReturn(updatedCaseDefinitionSettings)

        val returnedCaseDefinitionSettings = service.updateCaseSettings(caseDefinitionName, caseSettingsDto)

        verify(repository).getById(caseDefinitionName)
        assertEquals(caseDefinitionName, returnedCaseDefinitionSettings.name)
        assertFalse(returnedCaseDefinitionSettings.canHaveAssignee)
    }

    @Test
    fun `should throw exception when updating case settings and document definition does not exist `() {
        val caseDefinitionName = "name"
        val caseSettingsDto: CaseSettingsDto = mock()

        whenever(documentDefinitionService.findIdByName(any())).thenThrow(UnknownDocumentDefinitionException(caseDefinitionName))

        assertThrows<UnknownDocumentDefinitionException> {
            val foundCaseDefinitionSettings = service.updateCaseSettings(caseDefinitionName, caseSettingsDto)
        }
    }
}