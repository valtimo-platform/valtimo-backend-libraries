package com.ritense.case.service

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.ritense.case.domain.CaseDefinitionSettings
import com.ritense.case.repository.CaseDefinitionSettingsRepository
import com.ritense.case.web.rest.dto.CaseSettingsDto
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CaseDefinitionServiceTest {
    lateinit var repository: CaseDefinitionSettingsRepository

    lateinit var service: CaseDefinitionService

    @BeforeEach
    fun setUp() {
        repository = mock()
        service = CaseDefinitionService(repository)
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
}