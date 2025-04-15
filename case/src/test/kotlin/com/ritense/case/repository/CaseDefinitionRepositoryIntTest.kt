/*
 * Copyright 2015-2024 Ritense BV, the Netherlands.
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

package com.ritense.case.repository

import com.ritense.case.BaseIntegrationTest
import com.ritense.case_.domain.definition.CaseDefinition
import com.ritense.case_.repository.CaseDefinitionRepository
import com.ritense.valtimo.contract.case_.CaseDefinitionId
import org.junit.jupiter.api.Test
import org.semver4j.Semver
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional
import kotlin.test.assertEquals

@Transactional
class CaseDefinitionRepositoryIntTest @Autowired constructor(
    private val caseDefinitionRepository: CaseDefinitionRepository
): BaseIntegrationTest() {

    @Test
    fun `should support sorting on SemVer`() {
        // normally when sorting alphabetically 1.20.0 is greater than 1.100.0, but in semver 1.100.0 is greater than 1.20.0
        val caseDefinition1 = CaseDefinition(
            CaseDefinitionId(
                "key",
                Semver("1.20.0")
            ),
            "name",
            true,
            true
        )

        val caseDefinition2 = CaseDefinition(
            CaseDefinitionId(
                "key",
                Semver("1.100.0")
            ),
            "name",
            true,
            true
        )

        caseDefinitionRepository.save(caseDefinition1)
        caseDefinitionRepository.save(caseDefinition2)

        val foundDefinition = caseDefinitionRepository.findFirstByIdKeyOrderByIdVersionTagDesc("key")

        assertEquals(caseDefinition2, foundDefinition)
    }
}