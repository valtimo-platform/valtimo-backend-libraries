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

package com.ritense.openzaak

import com.ritense.documentenapi.domain.DocumentenApiInfo
import com.ritense.documentenapi.domain.DocumentenApiColumnKey
import com.ritense.documentenapi.domain.DocumentenApiColumnKey.AUTEUR
import com.ritense.documentenapi.domain.DocumentenApiColumnKey.BESTANDSOMVANG
import com.ritense.documentenapi.domain.DocumentenApiColumnKey.CREATIEDATUM
import com.ritense.documentenapi.domain.DocumentenApiColumnKey.FORMAAT
import com.ritense.documentenapi.domain.DocumentenApiColumnKey.INFORMATIEOBJECTTYPE
import com.ritense.documentenapi.domain.DocumentenApiColumnKey.STATUS
import com.ritense.documentenapi.domain.DocumentenApiColumnKey.TITEL
import com.ritense.documentenapi.domain.DocumentenApiColumnKey.VERTROUWELIJKHEIDAANDUIDING

class DocumentenApiMaykin1130 : DocumentenApiInfo() {
    override fun isColumnFilterable(columnKey: DocumentenApiColumnKey): Boolean {
        return when (columnKey) {
            AUTEUR, CREATIEDATUM, INFORMATIEOBJECTTYPE, TITEL, VERTROUWELIJKHEIDAANDUIDING -> true
            else -> false
        }
    }

    override fun isColumnSortable(columnKey: DocumentenApiColumnKey): Boolean {
        return when (columnKey) {
            AUTEUR, BESTANDSOMVANG, CREATIEDATUM, FORMAAT, STATUS, TITEL, VERTROUWELIJKHEIDAANDUIDING -> true
            else -> false
        }
    }
}

