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

package com.ritense.case_.domain.definition

import com.ritense.valtimo.contract.case_.CaseDefinitionId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.test.assertFailsWith


class CaseDefinitionTest {
    @Test
    fun `should set autoAssignTasks to false when canHaveAssignee is set to false`() {
        val exception = assertFailsWith<IllegalArgumentException> {
            CaseDefinition(
                CaseDefinitionId("key", "1.0.0"),
                "name",
                canHaveAssignee = false,
                autoAssignTasks = true
            )
        }

        assertEquals(IllegalArgumentException::class.java, exception::class.java)
    }
}