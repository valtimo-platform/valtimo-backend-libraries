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

import com.ritense.case_.domain.definition.CaseDefinition
import com.ritense.valtimo.contract.case_.CaseDefinitionId

data class CaseDefinitionDto(
    val key: String,
    val versionTag: String,
    val name: String,
    val canHaveAssignee: Boolean = false,
    val autoAssignTasks: Boolean = false,
) {
    fun toEntity(): CaseDefinition {
        val id = CaseDefinitionId(key, versionTag)
        return CaseDefinition(
            id = id,
            name = name,
            canHaveAssignee = canHaveAssignee,
            autoAssignTasks = autoAssignTasks
        )
    }
}