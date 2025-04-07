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

package com.ritense.case

import com.ritense.case_.domain.definition.CaseDefinition
import com.ritense.valtimo.contract.annotation.AllOpen
import com.ritense.valtimo.contract.case_.CaseDefinitionId
import org.semver4j.Semver
import java.time.LocalDateTime

@AllOpen
class BaseTest {

    fun caseDefinition(
        caseDefinitionId: CaseDefinitionId = CaseDefinitionId("key", "1.0.0"),
        name: String = "name",
        canHaveAssignee: Boolean = false,
        autoAssignTasks: Boolean = false,
    ): CaseDefinition {
        return CaseDefinition(
            id = caseDefinitionId,
            name = name,
            description = "description",
            createdBy = "system",
            createdDate = LocalDateTime.now(),
            baseOnVersionTag = Semver.parse("1.0.0-SNAPSHOT"),
            isFinal = true,
            canHaveAssignee = canHaveAssignee,
            autoAssignTasks = autoAssignTasks
        )
    }
}