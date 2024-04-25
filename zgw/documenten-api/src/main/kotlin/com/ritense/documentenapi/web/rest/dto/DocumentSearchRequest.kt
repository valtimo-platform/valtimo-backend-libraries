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

package com.ritense.documentenapi.web.rest.dto

import com.fasterxml.jackson.annotation.JsonIgnore
import java.net.URI
import java.time.LocalDateTime

data class DocumentSearchRequest(
    val informationObjectType: String?,
    val title: String?,
    val confidentialityLevel: String?,
    val creationDateFrom: LocalDateTime?,
    val creationDateTo: LocalDateTime?,
    val author: String?,
    val tags: List<String>?,
    @JsonIgnore val zaakUrl: URI?
)