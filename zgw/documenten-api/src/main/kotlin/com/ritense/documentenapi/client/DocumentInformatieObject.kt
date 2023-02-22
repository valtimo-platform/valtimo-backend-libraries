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

package com.ritense.documentenapi.client

import com.fasterxml.jackson.annotation.JsonFormat
import java.net.URI
import java.time.LocalDate
import java.time.LocalDateTime

class DocumentInformatieObject (
    val url: URI,
    val identificatie: String? = null,
    val bronorganisatie: String,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    val creatiedatum: LocalDate,
    val titel: String,
    val vertrouwelijkheidaanduiding: ConfidentialityLevel? = null,
    val auteur: String,
    val status: DocumentStatusType? = null,
    val formaat: String? = null,
    val taal: String,
    val versie: Int,
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    val beginRegistratie: LocalDateTime,
    val bestandsnaam: String? = null,
    val bestandsomvang: Long? = null,
    val link: URI? = null,
    val beschrijving: String? = null,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    val ontvangstdatum: LocalDate? = null,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    val verzenddatum: LocalDate? = null,
    val indicatieGebruiksrecht: Boolean? = null,
    val verschijningsvorm: String? = null,
)
