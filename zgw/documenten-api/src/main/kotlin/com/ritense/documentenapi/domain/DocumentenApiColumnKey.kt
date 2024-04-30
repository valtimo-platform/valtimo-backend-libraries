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

enum class DocumentenApiColumnKey(
    val sortable: Boolean = false,
    val filterable: Boolean = false
) {
    IDENTIFICATIE,
    BRONORGANISATIE,
    CREATIEDATUM(sortable = true, filterable = true),
    TITEL(sortable = true, filterable = true),
    VERTROUWELIJKHEIDAANDUIDING(sortable = true, filterable = true),
    AUTEUR(sortable = true, filterable = true),
    STATUS(sortable = true),
    FORMAAT(sortable = true),
    TAAL,
    VERSIE,
    BESTANDSNAAM,
    BESTANDSOMVANG(sortable = true),
    BESCHRIJVING,
    INFORMATIEOBJECTTYPE(filterable = true),
    LOCKED;

    companion object {
        @JvmStatic
        @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
        fun from(name: String) = entries.firstOrNull { it.name.equals(name, ignoreCase = true) }
    }
}

