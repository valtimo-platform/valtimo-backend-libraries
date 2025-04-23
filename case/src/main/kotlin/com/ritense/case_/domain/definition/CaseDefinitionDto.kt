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
import org.semver4j.Semver
import java.time.LocalDateTime

data class CaseDefinitionDto(
    val key: String,
    val versionTag: String,
    val name: String,
    val description: String?,
    val createdBy: String?,
    val createdDate: LocalDateTime?,
    val basedOnVersionTag: String?,
    val final: Boolean,
    val canHaveAssignee: Boolean = false,
    val autoAssignTasks: Boolean = false,
) {
    fun getCaseDefinitionId(): CaseDefinitionId = CaseDefinitionId(key, versionTag)

    fun toEntity(): CaseDefinition {
        return CaseDefinition(
            id = getCaseDefinitionId(),
            name = name,
            description = description,
            createdBy = createdBy,
            createdDate = createdDate,
            basedOnVersionTag = basedOnVersionTag?.let { Semver(it) },
            final = final,
            canHaveAssignee = canHaveAssignee,
            autoAssignTasks = autoAssignTasks
        )
    }
}