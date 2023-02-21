/*
 * Copyright 2015-2023 Ritense BV, the Netherlands.
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

package com.ritense.objecttypenapi.client

import com.fasterxml.jackson.annotation.JsonProperty
import java.net.URI
import java.time.LocalDate
import java.util.UUID

class Objecttype(
    val url: URI,
    val uuid: UUID,
    val name: String,
    val namePlural: String,
    val description: String?,
    val dataClassification: DataClassification?,
    val maintainerOrganization: String?,
    val maintainerDepartment: String?,
    val contactPerson: String?,
    val contactEmail: String?,
    val source: String?,
    val updateFrequency: UpdateFrequency?,
    val providerOrganization: String?,
    val documentationUrl: URI?,
    val labels: Map<String, Any>?,
    val createdAt: LocalDate?,
    val modifiedAt: LocalDate?,
    val versions: List<URI>?
)

enum class DataClassification {
    @JsonProperty("open")
    OPEN,
    @JsonProperty("intern")
    INTERN,
    @JsonProperty("confidential")
    CONFIDENTIAL,
    @JsonProperty("strictly_confidential")
    STRICTLY_CONFIDENTIAL
}

enum class UpdateFrequency {
    @JsonProperty("real_time")
    REAL_TIME,
    @JsonProperty("hourly")
    HOURLY,
    @JsonProperty("daily")
    DAILY,
    @JsonProperty("weekly")
    WEEKLY,
    @JsonProperty("monthly")
    MONTHLY,
    @JsonProperty("yearly")
    YEARLY,
    @JsonProperty("unknown")
    UNKNOWN
}