/*
 * Copyright 2015-2020 Ritense BV, the Netherlands.
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

package com.ritense.document.export.domain

import com.fasterxml.jackson.core.JsonPointer
import com.ritense.document.export.domain.type.ExportType
import com.ritense.valtimo.contract.validation.Validatable
import java.util.UUID

data class Node(
    val id: UUID, // unique id
    val name: String, // node's primary label.
    val pointer: JsonPointer,// data point within definition
    val colour: String, // color for Excel column
    val type: ExportType, // orb / star / key
    var order: Int, // position column in export
    var filters : List<Filter> // filter used on pointer as where clause(s)
) : Validatable {

    init {
        validate()
    }

}