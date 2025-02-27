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
import com.ritense.document.domain.InternalCaseStatusColor
import com.ritense.document.repository.InternalCaseStatusRepository
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@Transactional
class InternalCaseDeployerIntTest @Autowired constructor(
    private val internalCaseStatusRepository: InternalCaseStatusRepository
) : BaseIntegrationTest() {

    @Test
    fun `should have imported two internal case statuses for person case`() {
        val internalCaseStatuses =
            internalCaseStatusRepository.findByIdCaseDefinitionNameOrderByOrder("person")

        assertEquals(2, internalCaseStatuses.size)
        assertEquals("closed", internalCaseStatuses[0].id.key)
        assertEquals("Closed", internalCaseStatuses[0].title)
        assertFalse(internalCaseStatuses[0].visibleInCaseListByDefault)
        assertEquals("started", internalCaseStatuses[1].id.key)
        assertEquals("Started", internalCaseStatuses[1].title)
        assertTrue(internalCaseStatuses[1].visibleInCaseListByDefault)
    }

    @Test
    fun `should have updated internal case status for house case`() {
        val internalCaseStatuses =
            internalCaseStatusRepository.findByIdCaseDefinitionNameOrderByOrder("house")

        assertEquals(InternalCaseStatusColor.BLUE, internalCaseStatuses.filter{ it.title == "Closed" }.first().color)
    }

}