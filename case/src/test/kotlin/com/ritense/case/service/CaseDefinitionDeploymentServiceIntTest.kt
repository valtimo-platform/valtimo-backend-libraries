package com.ritense.case.service

import com.ritense.case.BaseIntegrationTest
import com.ritense.document.service.DocumentDefinitionService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class CaseDefinitionDeploymentServiceIntTest: BaseIntegrationTest() {
    @Autowired
    lateinit var documentDefinitionService: DocumentDefinitionService

    @Test
    fun `should create settings when settings are defined`() {
        documentDefinitionService.deploy("" +
            "{\n" +
            "    \"\$id\": \"all-properties-present.schema\",\n" +
            "    \"\$schema\": \"http://json-schema.org/draft-07/schema#\"\n" +
            "}\n")
    }

    @Test
    fun `should create settings with default values when settings are not defined`() {
        documentDefinitionService.deploy("" +
            "{\n" +
            "    \"\$id\": \"empty-properties.schema\",\n" +
            "    \"\$schema\": \"http://json-schema.org/draft-07/schema#\"\n" +
            "}\n")
    }
}