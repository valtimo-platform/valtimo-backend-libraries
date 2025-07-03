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

package com.ritense.valtimo.contract.case_

import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.ritense.valtimo.contract.domain.AbstractId
import com.ritense.valtimo.contract.repository.SemverConverter
import com.ritense.valtimo.contract.serializer.SemverSerializer
import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Embeddable
import org.semver4j.Semver

@Embeddable
data class CaseDefinitionId(
    @Column(name = "case_definition_key", nullable = false, updatable = false)
    val key: String,
    @Convert(converter = SemverConverter::class)
    @Column(name = "case_definition_version_tag", nullable = false, updatable = true)
    @JsonSerialize(using = SemverSerializer::class)
    val versionTag: Semver
) : AbstractId<CaseDefinitionId>() {

    constructor(
        key: String,
        versionTag: String
    ) : this(
        key,
        Semver.parse(versionTag)
            ?: throw IllegalArgumentException("Given version '$versionTag' is not a valid Semver version")
    )

    init {
        require(key.isNotBlank()) { "[caseDefinitionId.key] was blank!" }
        require(key.matches(Regex("^[a-zA-Z0-9\\-]+$"))) {
            "[caseDefinitionId.key] contains characters that are not allowed (only alphanumeric characters and dashes)"
        }
    }


    override fun toString(): String {
        return "${key}:${versionTag}"
    }

    companion object {
        @JvmStatic
        fun of(key: String, versionTag: String): CaseDefinitionId {
            return CaseDefinitionId(key, versionTag)
        }

        @JvmStatic
        fun of(caseDefinitionId: String): CaseDefinitionId {
            val parts = caseDefinitionId.split(":")

            return of(parts[0], parts[1])
        }

        @JvmStatic
        fun fromProcessVersionTag(versionTag: String?): CaseDefinitionId? {
            if (versionTag == null || !versionTag.startsWith("CD:")) return null
            val parts = versionTag.removePrefix("CD:").split(":")
            if (parts.size != 2) return null

            val (key, tag) = parts

            return try {
                of(key, tag)
            } catch (ex: IllegalArgumentException) {
                null
            }
        }
    }
}