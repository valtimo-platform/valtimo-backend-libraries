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

package com.ritense.document.deployment

import com.ritense.document.BaseIntegrationTest
import com.ritense.document.domain.CaseTagColor
import com.ritense.document.repository.CaseTagRepository
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional
import kotlin.test.assertEquals


@Transactional
class CaseTagDeployerIntTest @Autowired constructor(
    private val caseTagRepository: CaseTagRepository
) : BaseIntegrationTest() {

    @Test
    fun `should have imported two case tags for house case`() {
        val caseTags =
            caseTagRepository.findByIdCaseDefinitionNameOrderByOrder("house")

        assertEquals(2, caseTags.size)
        assertEquals("important", caseTags[0].id.key)
        assertEquals("Important", caseTags[0].title)
        assertEquals("needs-attention", caseTags[1].id.key)
        assertEquals("Needs attention!", caseTags[1].title)
    }

    @Test
    fun `should have updated case tag status for house case`() {
        val caseTags =
            caseTagRepository.findByIdCaseDefinitionNameOrderByOrder("house")

        assertEquals(CaseTagColor.RED, caseTags.first { it.id.key == "needs-attention" }.color)
    }

}