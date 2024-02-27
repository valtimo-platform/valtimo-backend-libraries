package com.ritense.documentenapi.web.rest

import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.documentenapi.domain.ZgwDocumentTrefwoord
import com.ritense.documentenapi.service.ZgwDocumentTrefwoordService
import com.ritense.valtimo.contract.authentication.AuthoritiesConstants
import com.ritense.valtimo.contract.authentication.AuthoritiesConstants.ADMIN
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@ExtendWith(MockitoExtension::class, SpringExtension::class)
@WebMvcTest(ZgwDocumentTrefwoordResource::class)
@AutoConfigureMockMvc
class ZgwDocumentTrefwoordResourceTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
    private lateinit var service: ZgwDocumentTrefwoordService

    @Test
    @WithMockUser(username = "admin@ritense.com", authorities = [ADMIN])
    fun `test getTrefwoorden`() {
        val caseDefinitionName = "TestDefinition"
        val pageable = Pageable.unpaged()

        val trefwoorden = listOf(
            ZgwDocumentTrefwoord(caseDefinitionName, "Trefwoord1"),
            ZgwDocumentTrefwoord(caseDefinitionName, "Trefwoord2")
        )

        val expectedPage = PageImpl(emptyList<ZgwDocumentTrefwoord>())

        whenever(service.getTrefwoorden(caseDefinitionName, pageable)).thenReturn(expectedPage)

        mockMvc.perform(get("/api/management/v1/case-definition/{caseDefinitionName}/zgw-document/trefwoord", caseDefinitionName)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.content").isArray)
            .andExpect(jsonPath("$.content[0].caseDefinitionName").value(trefwoorden[0].caseDefinitionName))
            .andExpect(jsonPath("$.content[0].value").value(trefwoorden[0].value))
            .andExpect(jsonPath("$.content[1].caseDefinitionName").value(trefwoorden[1].caseDefinitionName))
            .andExpect(jsonPath("$.content[1].value").value(trefwoorden[1].value))

        verify(service).getTrefwoorden(caseDefinitionName, pageable)
    }

    @Test
    @WithMockUser(username = "admin@ritense.com", authorities = [ADMIN])
    fun `test createTrefwoord`() {
        val caseDefinitionName = "TestDefinition"
        val trefwoord = "TestTrefwoord"

        mockMvc.perform(post("/api/management/v1/case-definition/{caseDefinitionName}/zgw-document/trefwoord/{trefwoord}", caseDefinitionName, trefwoord)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent)

        verify(service).createTrefwoord(caseDefinitionName, trefwoord)
    }

    @Test
    @WithMockUser(username = "admin@ritense.com", authorities = [ADMIN])
    fun `test deleteTrefwoord`() {
        val caseDefinitionName = "TestDefinition"
        val trefwoord = "TestTrefwoord"

        mockMvc.perform(delete("/api/management/v1/case-definition/{caseDefinitionName}/zgw-document/trefwoord/{trefwoord}", caseDefinitionName, trefwoord)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent)

        verify(service).deleteTrefwoord(caseDefinitionName, trefwoord)
    }
}
