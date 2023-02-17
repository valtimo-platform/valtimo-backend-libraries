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

package com.ritense.zakenapi.domain

import com.fasterxml.jackson.annotation.JsonFormat
import java.net.URI
import java.time.LocalDateTime
import java.util.UUID

data class ZaakInformatieObject(
    val url: URI,
    val uuid: UUID,
    val informatieobject: URI,
    val zaak: URI,
    val aardRelatieWeergave: String,
    val titel: String?,
    val beschrijving: String?,
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    val registratiedatum: LocalDateTime,
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    val vernietigingsdatum: LocalDateTime?,
    val status: URI?
)
