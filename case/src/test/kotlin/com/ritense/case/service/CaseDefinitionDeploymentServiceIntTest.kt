package com.ritense.case.service

import com.ritense.case.BaseIntegrationTest
import com.ritense.case_.repository.CaseDefinitionRepository
import com.ritense.valtimo.contract.case_.CaseDefinitionId
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.transaction.annotation.Transactional
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@Transactional
class CaseDefinitionDeploymentServiceIntTest @Autowired constructor(
    private val caseDefinitionDeploymentService: CaseDefinitionDeploymentService,
    private val caseDefinitionRepository: CaseDefinitionRepository
) : BaseIntegrationTest() {

    @Test
    fun `should have deployed on startup`() {
        val deployedCaseDefinition1 = caseDefinitionRepository.findByIdOrNull(CaseDefinitionId.of("some-case-type", "1.2.3"))
        val deployedCaseDefinition2 = caseDefinitionRepository.findByIdOrNull(CaseDefinitionId.of("some-other-case-type", "1.1.1"))

        assertNotNull(deployedCaseDefinition1)
        assertEquals(deployedCaseDefinition1.name, "Some case type")
        assertEquals(deployedCaseDefinition1.canHaveAssignee, true)
        assertEquals(deployedCaseDefinition1.autoAssignTasks, true)
        assertNotNull(deployedCaseDefinition2)
        assertEquals(deployedCaseDefinition2.name, "Some other case type")
        assertEquals(deployedCaseDefinition2.canHaveAssignee, true)
        assertEquals(deployedCaseDefinition2.autoAssignTasks, true)
    }
}