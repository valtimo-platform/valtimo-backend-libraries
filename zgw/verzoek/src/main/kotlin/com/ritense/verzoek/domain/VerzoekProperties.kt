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

package com.ritense.verzoek.domain

import com.fasterxml.jackson.annotation.JsonProperty
import com.ritense.valtimo.contract.validation.Url
import java.net.URI
import java.util.UUID

data class VerzoekProperties(
    val type: String,
    val caseDefinitionName: String,
    val processDefinitionKey: String,
    val objectManagementId: UUID,
    @field:Url val initiatorRoltypeUrl: URI,
    val initiatorRolDescription: String,
    val copyStrategy: CopyStrategy,
    val mapping: List<Mapping>?,
)

enum class CopyStrategy {
    @JsonProperty("full")
    FULL,

    @JsonProperty("specified")
    SPECIFIED
}

data class Mapping(
    val source: String,
    val target: String,
)
