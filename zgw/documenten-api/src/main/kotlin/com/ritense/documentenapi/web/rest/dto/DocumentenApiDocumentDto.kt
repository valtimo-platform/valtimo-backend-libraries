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

import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

data class DocumentenApiDocumentDto(
    // valtimo fields
    val fileId: UUID,
    val pluginConfigurationId: UUID,
    // document api fields
    val bestandsnaam: String?,
    val bestandsomvang: Long?,
    val creatiedatum: LocalDateTime,
    val auteur: String? = null,
    val titel: String? = null,
    val status: String? = null,
    val taal: String? = null,
    val identificatie: String? = null,
    val beschrijving: String? = null,
    val informatieobjecttype: String? = null,
    val informatieobjecttypeOmschrijving: String? = null,
    val trefwoorden: List<String>? = null,
    val formaat: String? = null,
    val verzenddatum: LocalDate? = null,
    val ontvangstdatum: LocalDate? = null,
    val vertrouwelijkheidaanduiding: String? = null,
    val versie: Int? = null,
    val indicatieGebruiksrecht: Boolean? = null,
)