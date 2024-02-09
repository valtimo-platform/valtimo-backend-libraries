package com.ritense.case.service

import com.ritense.authorization.AuthorizationContext.Companion.runWithoutAuthorization
import com.ritense.case.BaseIntegrationTest
import com.ritense.case.repository.CaseDefinitionSettingsRepository
import com.ritense.document.service.DocumentDefinitionService
import jakarta.transaction.Transactional
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@Transactional
class CaseDefinitionDeploymentServiceIntTest @Autowired constructor(
    private val documentDefinitionService: DocumentDefinitionService,
    private val caseDefinitionSettingsRepository: CaseDefinitionSettingsRepository,
    private val caseDefinitionDeploymentService: CaseDefinitionDeploymentService
) : BaseIntegrationTest() {

    @Test
    fun `should create settings when settings are defined`() {
        runWithoutAuthorization {
            documentDefinitionService.deploy(
                "" +
                    "{\n" +
                    "    \"\$id\": \"all-properties-present.schema\",\n" +
                    "    \"\$schema\": \"http://json-schema.org/draft-07/schema#\"\n" +
                    "}\n"
            )
        }

        val settings = caseDefinitionSettingsRepository.getReferenceById("all-properties-present")

        assertEquals("all-properties-present", settings.name)
        assertTrue(settings.canHaveAssignee)
        assertTrue(settings.autoAssignTasks)
    }

    @Test
    fun `should deploy settings caseDefinitionName and json content`() {
        val caseDefinitionName = "by-case-definition-name-and-json"

        caseDefinitionDeploymentService.deploy(caseDefinitionName, """
            {
                "canHaveAssignee": true,
                "autoAssignTasks": false
            }
        """.trimIndent())

        val settings = caseDefinitionSettingsRepository.getReferenceById(caseDefinitionName)

        assertEquals(caseDefinitionName, settings.name)
        assertTrue(settings.canHaveAssignee)
        assertFalse(settings.autoAssignTasks)
    }

    @Test
    fun `should deploy settings when settings are defined`() {
        runWithoutAuthorization {
            documentDefinitionService.deploy(
                "" +
                    "{\n" +
                    "    \"\$id\": \"all-properties-present.schema\",\n" +
                    "    \"\$schema\": \"http://json-schema.org/draft-07/schema#\"\n" +
                    "}\n"
            )
        }

        val settings = caseDefinitionSettingsRepository.getReferenceById("all-properties-present")

        assertEquals("all-properties-present", settings.name)
        assertTrue(settings.canHaveAssignee)
        assertTrue(settings.autoAssignTasks)
    }

    @Test
    fun `should throw exception when settings are invalid`() {
        val result = runWithoutAuthorization {
            documentDefinitionService.deploy(
                "" +
                    "{\n" +
                    "    \"\$id\": \"invalid-properties.schema\",\n" +
                    "    \"\$schema\": \"http://json-schema.org/draft-07/schema#\"\n" +
                    "}\n"
            )
        }

        assertEquals(1, result.errors().size)
    }

    @Test
    fun `should create settings with default values when settings are not defined`() {
        runWithoutAuthorization {
            documentDefinitionService.deploy(
                "" +
                    "{\n" +
                    "    \"\$id\": \"empty-properties.schema\",\n" +
                    "    \"\$schema\": \"http://json-schema.org/draft-07/schema#\"\n" +
                    "}\n"
            )
        }

        val settings = caseDefinitionSettingsRepository.getReferenceById("empty-properties")

        assertEquals("empty-properties", settings.name)
        assertFalse(settings.canHaveAssignee)
    }

    @Test
    fun `should create settings with default values when settings file is not present`() {
        runWithoutAuthorization {
            documentDefinitionService.deploy(
                "" +
                    "{\n" +
                    "    \"\$id\": \"no-settings-present.schema\",\n" +
                    "    \"\$schema\": \"http://json-schema.org/draft-07/schema#\"\n" +
                    "}\n"
            )
        }

        val settings = caseDefinitionSettingsRepository.getReferenceById("no-settings-present")

        assertEquals("no-settings-present", settings.name)
        assertFalse(settings.canHaveAssignee)

    }
}