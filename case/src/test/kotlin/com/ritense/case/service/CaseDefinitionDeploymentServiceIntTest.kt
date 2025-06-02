/*
 * Copyright 2015-2025 Ritense BV, the Netherlands.
 *
 * Licensed under EUPL, Version 1.2 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ritense.case.service

import com.ritense.BaseIntegrationTest
import com.ritense.case_.repository.CaseDefinitionRepository
import com.ritense.valtimo.contract.case_.CaseDefinitionId
import org.junit.jupiter.api.Test
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
        val deployedCaseDefinition0 = caseDefinitionRepository.findByIdOrNull(CaseDefinitionId.of("some-case-type", "0.0.1"))
        val deployedCaseDefinition1 = caseDefinitionRepository.findByIdOrNull(CaseDefinitionId.of("some-case-type", "1.2.3"))
        val deployedCaseDefinition2 = caseDefinitionRepository.findByIdOrNull(CaseDefinitionId.of("some-other-case-type", "1.1.1"))

        assertNotNull(deployedCaseDefinition0)
        assertEquals(deployedCaseDefinition0.name, "Some case type")
        assertEquals(deployedCaseDefinition0.canHaveAssignee, true)
        assertEquals(deployedCaseDefinition0.autoAssignTasks, true)
        assertEquals(deployedCaseDefinition0.active, false)
        assertNotNull(deployedCaseDefinition1)
        assertEquals(deployedCaseDefinition1.name, "Some case type")
        assertEquals(deployedCaseDefinition1.canHaveAssignee, true)
        assertEquals(deployedCaseDefinition1.autoAssignTasks, true)
        assertEquals(deployedCaseDefinition1.active, true)
        assertNotNull(deployedCaseDefinition2)
        assertEquals(deployedCaseDefinition2.name, "Some other case type")
        assertEquals(deployedCaseDefinition2.canHaveAssignee, true)
        assertEquals(deployedCaseDefinition2.autoAssignTasks, true)
        assertEquals(deployedCaseDefinition2.active, true)
    }
}