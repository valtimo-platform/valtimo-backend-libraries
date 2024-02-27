package com.ritense.documentenapi.service

import com.ritense.documentenapi.domain.ZgwDocumentTrefwoord
import com.ritense.documentenapi.repository.ZgwDocumentTrefwoordRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable

@ExtendWith(MockitoExtension::class)
class ZgwDocumentTrefwoordServiceTest {

    @Mock
    private lateinit var repository: ZgwDocumentTrefwoordRepository

    @InjectMocks
    private lateinit var service: ZgwDocumentTrefwoordService

    @Test
    fun `test getTrefwoorden`() {
        val caseDefinitionName = "TestDefinition"
        val pageable = Pageable.unpaged()
        val expectedPage = PageImpl(emptyList<ZgwDocumentTrefwoord>())

        whenever(repository.findAllByCaseDefinitionName(caseDefinitionName, pageable)).thenReturn(expectedPage)

        val result = service.getTrefwoorden(caseDefinitionName, pageable)

        assertEquals(expectedPage, result)
    }

    @Test
    fun `test createTrefwoord`() {
        val caseDefinitionName = "TestDefinition"
        val trefwoord = "TestTrefwoord"

        whenever(repository.findAllByCaseDefinitionNameAndValue(caseDefinitionName, trefwoord)).thenReturn(null)

        service.createTrefwoord(caseDefinitionName, trefwoord)

        verify(repository).save(ZgwDocumentTrefwoord(caseDefinitionName, trefwoord))
    }

    @Test
    fun `test createTrefwoord throws exception when trefwoord already exists`() {
        val caseDefinitionName = "TestDefinition"
        val trefwoordValue = "TestTrefwoord"
        val trefwoord = ZgwDocumentTrefwoord(caseDefinitionName, trefwoordValue)

        whenever(repository.findAllByCaseDefinitionNameAndValue(caseDefinitionName, trefwoordValue)).thenReturn(trefwoord)

        assertThrows<IllegalArgumentException> {
            service.createTrefwoord(caseDefinitionName, trefwoordValue)
        }
    }

    @Test
    fun `test deleteTrefwoord`() {
        val caseDefinitionName = "TestDefinition"
        val trefwoord = "TestTrefwoord"

        service.deleteTrefwoord(caseDefinitionName, trefwoord)

        verify(repository).deleteByCaseDefinitionNameAndValue(caseDefinitionName, trefwoord)
    }
}