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

package com.ritense.documentenapi.domain

import com.fasterxml.jackson.annotation.JsonCreator

enum class DocumentenApiColumnKey(val property: String) {
    IDENTIFICATIE("identificatie"),
    BRONORGANISATIE("bronorganisatie"),
    CREATIEDATUM("creatiedatum"),
    TITEL("titel"),
    VERTROUWELIJKHEIDAANDUIDING("vertrouwelijkheidaanduiding"),
    AUTEUR("auteur"),
    STATUS("status"),
    FORMAAT("formaat"),
    TAAL("taal"),
    VERSIE("versie"),
    BESTANDSNAAM("bestandsnaam"),
    BESTANDSOMVANG("bestandsomvang"),
    BESCHRIJVING("beschrijving"),
    INFORMATIEOBJECTTYPE_OMSCHRIJVING("informatieobjecttypeOmschrijving"),
    LOCKED("locked"),
    TREFWOORDEN("trefwoorden");

    companion object {
        @JvmStatic
        @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
        fun fromEnumName(name: String) = entries.firstOrNull { it.name == name }

        @JvmStatic
        fun fromProperty(property: String) = entries.firstOrNull { it.property == property }
    }
}

