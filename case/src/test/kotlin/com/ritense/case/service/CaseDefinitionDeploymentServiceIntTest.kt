package com.ritense.case.service

import com.ritense.case.BaseIntegrationTest
import com.ritense.case.repository.CaseDefinitionSettingsRepository
import com.ritense.document.service.DocumentDefinitionService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CaseDefinitionDeploymentServiceIntTest: BaseIntegrationTest() {
    @Autowired
    lateinit var documentDefinitionService: DocumentDefinitionService

    @Autowired
    lateinit var caseDefinitionSettingsRepository: CaseDefinitionSettingsRepository

    @Test
    fun `should create settings when settings are defined`() {
        documentDefinitionService.deploy("" +
            "{\n" +
            "    \"\$id\": \"all-properties-present.schema\",\n" +
            "    \"\$schema\": \"http://json-schema.org/draft-07/schema#\"\n" +
            "}\n")

        val settings = caseDefinitionSettingsRepository.getById("all-properties-present")

        assertEquals("all-properties-present", settings.name)
        assertTrue(settings.canHaveAssignee)

    }

    @Test
    fun `should create settings with default values when settings are not defined`() {
        documentDefinitionService.deploy("" +
            "{\n" +
            "    \"\$id\": \"empty-properties.schema\",\n" +
            "    \"\$schema\": \"http://json-schema.org/draft-07/schema#\"\n" +
            "}\n")

        val settings = caseDefinitionSettingsRepository.getById("empty-properties")

        assertEquals("empty-properties", settings.name)
        assertTrue(settings.canHaveAssignee)
    }
}