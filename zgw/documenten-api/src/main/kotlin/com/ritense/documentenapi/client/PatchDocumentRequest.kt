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

package com.ritense.documentenapi.client

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonInclude
import com.ritense.documentenapi.web.rest.dto.ModifyDocumentRequest
import java.time.LocalDate

@JsonInclude(JsonInclude.Include.NON_NULL)
class PatchDocumentRequest(
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    val creatiedatum: LocalDate,
    val titel: String,
    val auteur: String,
    val status: DocumentStatusType? = null,
    val taal: String,
    val bestandsnaam: String? = null,
    val beschrijving: String? = null,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    val ontvangstdatum: LocalDate? = null,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    val verzenddatum: LocalDate? = null,
    val indicatieGebruiksrecht: Boolean? = false,
    val vertrouwelijkheidaanduiding: String? = null,
    val informatieobjecttype: String? = null,
    val trefwoorden: List<String>? = null,
    var lock: String? = null
) {
    constructor(modifyDocumentRequest: ModifyDocumentRequest) : this(
        modifyDocumentRequest.creatiedatum,
        modifyDocumentRequest.titel,
        modifyDocumentRequest.auteur,
        modifyDocumentRequest.status,
        modifyDocumentRequest.taal,
        modifyDocumentRequest.bestandsnaam,
        modifyDocumentRequest.beschrijving,
        modifyDocumentRequest.ontvangstdatum,
        modifyDocumentRequest.verzenddatum,
        modifyDocumentRequest.indicatieGebruiksrecht,
        modifyDocumentRequest.vertrouwelijkheidaanduiding,
        modifyDocumentRequest.informatieobjecttype,
        modifyDocumentRequest.trefwoorden,
    )
}
