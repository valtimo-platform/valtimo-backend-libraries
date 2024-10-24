package com.ritense.case.service

import com.ritense.authorization.AuthorizationContext.Companion.runWithoutAuthorization
import com.ritense.authorization.AuthorizationService
import com.ritense.case.BaseIntegrationTest
import com.ritense.case.domain.CaseTabType
import com.ritense.case.web.rest.dto.CaseTabDto
import jakarta.transaction.Transactional
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.test.context.support.WithMockUser
import java.util.UUID

@Transactional
class CaseDefinitionServiceIntTest @Autowired constructor(
    private val caseDefinitionService: CaseDefinitionService,
    private val caseTabService: CaseTabService,
    private val authorizationService: AuthorizationService
) : BaseIntegrationTest() {

    @Test
    fun `should deploy case-definition`() {
        val id = UUID.randomUUID()
        val name = "case-definition-name"
        val version = "1.0.0"
        val result = caseDefinitionService.deployCaseDefinition(
            id = id,
            name = name,
            version = version
        )
        assertThat(result.id).isEqualTo(id)
        assertThat(result.name).isEqualTo(name)
        assertThat(result.version.toString()).isEqualTo(version)
    }

    @Test
    @WithMockUser(username = "john.doe@ritense.com", authorities = ["ROLE_CUSTOM_TEST"])
    fun `should link case-definition to CaseTab`() {
        val id = UUID.randomUUID()
        val name = "definition-test"
        val version = "1.0.0"

        // Given
        runWithoutAuthorization {
            val caseDefinition = caseDefinitionService.deployCaseDefinition(
                id = id,
                name = name,
                version = version
            )
            // When
            val result = caseTabService.createCaseTab(
                caseDefinitionName = name, // Reuse the name here, if this is not f
                caseTabDto = CaseTabDto(
                    key = "case-tab-key",
                    type = CaseTabType.CUSTOM,
                    contentKey = "case-tab-content-key"
                ),
                caseDefinitionId = caseDefinition.id,
            )
            // Then
            assertThat(result.caseDefinitionId).isEqualTo(id)
        }

        // Retrieve the case tab
        val caseTab = caseTabService.getCaseTab(
            caseDefinitionName = "definition-test",
            key = "case-tab-key"
        )
        assertThat(caseTab.caseDefinitionId).isEqualTo(id)

        // Retrieve the case tab via list call
        val caseTabs = caseTabService.getCaseTabs(
            caseDefinitionName = "definition-test"
        )
        assertThat(caseTabs).hasSize(1)

    }
}