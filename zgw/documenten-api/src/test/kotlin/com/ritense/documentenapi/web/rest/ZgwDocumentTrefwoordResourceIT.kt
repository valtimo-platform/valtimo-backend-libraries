package com.ritense.documentenapi.web.rest

import com.ritense.documentenapi.BaseIntegrationTest
import com.ritense.documentenapi.service.ZgwDocumentTrefwoordService
import com.ritense.valtimo.contract.authentication.AuthoritiesConstants.ADMIN
import com.ritense.valtimo.contract.domain.ValtimoMediaType
import jakarta.transaction.Transactional
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

@Transactional
internal class ZgwDocumentTrefwoordResourceIT : BaseIntegrationTest() {

    @Autowired
    lateinit var webApplicationContext: WebApplicationContext

    @Autowired
    private lateinit var service: ZgwDocumentTrefwoordService

    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun beforeEach() {
        mockMvc = MockMvcBuilders
            .webAppContextSetup(this.webApplicationContext)
            .build()
    }

    @Test
    @WithMockUser(username = "admin@ritense.com", authorities = [ADMIN])
    fun `test getTrefwoorden`() {
        val caseDefinitionName = "TestDefinition"

        service.createTrefwoord(caseDefinitionName, "Trefwoord1")
        service.createTrefwoord(caseDefinitionName, "Trefwoord2")

        mockMvc.perform(get("/api/management/v1/case-definition/{caseDefinitionName}/zgw-document/trefwoord", caseDefinitionName)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk)
            .andExpect(content().contentType(ValtimoMediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.content").isArray)
            .andExpect(jsonPath("$.content[0].caseDefinitionName").value("TestDefinition"))
            .andExpect(jsonPath("$.content[0].value").value("Trefwoord1"))
            .andExpect(jsonPath("$.content[1].caseDefinitionName").value("TestDefinition"))
            .andExpect(jsonPath("$.content[1].value").value("Trefwoord2"))
    }

    @Test
    @WithMockUser(username = "admin@ritense.com", authorities = [ADMIN])
    fun `test createTrefwoord`() {
        val caseDefinitionName = "TestDefinition"
        val trefwoord = "TestTrefwoord"

        mockMvc.perform(post("/api/management/v1/case-definition/{caseDefinitionName}/zgw-document/trefwoord/{trefwoord}", caseDefinitionName, trefwoord)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent)
    }

    @Test
    @WithMockUser(username = "admin@ritense.com", authorities = [ADMIN])
    fun `test deleteTrefwoord`() {
        val caseDefinitionName = "TestDefinition"
        val trefwoord = "TestTrefwoord"

        mockMvc.perform(delete("/api/management/v1/case-definition/{caseDefinitionName}/zgw-document/trefwoord/{trefwoord}", caseDefinitionName, trefwoord)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent)
    }
}
