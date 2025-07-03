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

package com.ritense.case.domain

import com.ritense.valtimo.contract.case_.CaseDefinitionId
import com.ritense.valtimo.contract.domain.AbstractId
import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.persistence.Embedded

@Embeddable
data class CaseTabId(
    @Embedded
    val caseDefinitionId: CaseDefinitionId,
    @Column(name = "tab_key")
    val key: String
) : AbstractId<CaseTabId>() {
    init {
        require(key.isNotBlank()) { "key was blank!" }
        require(key.matches(Regex("^[a-zA-Z0-9\\-]+$"))) { "key contains characters that are not allowed (only alphanumeric characters and dashes)" }
    }

    override fun toString(): String {
        return "$caseDefinitionId:$key)"
    }

    companion object {
        @JvmStatic
        fun of(idAsString: String): CaseTabId? {
            val strings = idAsString.split(':')
            return if (strings.size != 3) {
                null
            } else {
                CaseTabId(CaseDefinitionId.of(strings[0], strings[1]), strings[2])
            }
        }
    }
}