package com.ritense.case.service

import com.ritense.case.BaseIntegrationTest
import jakarta.transaction.Transactional
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.util.UUID

@Transactional
class CaseDefinitionServiceIntTest @Autowired constructor(
    private val caseDefinitionService: CaseDefinitionService
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

}