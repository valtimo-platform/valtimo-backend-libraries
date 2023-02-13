/*
 * Copyright 2015-2022 Ritense BV, the Netherlands.
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

package com.ritense.objectenapi.client

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.JsonNode
import java.time.LocalDate

@JsonInclude(JsonInclude.Include.NON_NULL)
class ObjectRecord(
    val index: Int? = null,
    val typeVersion: Int,
    val data: JsonNode? = null,
    val geometry: ObjectGeometry? = null,
    @JsonFormat(pattern = "yyyy-MM-dd")
    val startAt: LocalDate,
    @JsonFormat(pattern = "yyyy-MM-dd")
    val endAt: LocalDate? = null,
    @JsonFormat(pattern = "yyyy-MM-dd")
    val registrationAt: LocalDate? = null,
    val correctionFor: String? = null,
    val correctedBy: String? = null
)

class ObjectGeometry(
    val type: String,
    val coordinates: Array<Int>
)