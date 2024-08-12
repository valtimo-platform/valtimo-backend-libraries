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

package com.ritense.case_.widget.collection

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.annotation.JsonValue
import com.ritense.case_.widget.displayproperties.FieldDisplayProperties
import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull

data class CollectionWidgetProperties (
    @field:NotBlank val collection: String,
    @field:Min(1) val defaultPageSize: Int,
    @field:NotNull val title: TitleField,
    @field:NotEmpty val fields: List<@Valid Field>,
) {

    @JsonInclude(Include.NON_NULL)
    data class TitleField (
        @field:NotBlank val value: String,
        @field:Valid val displayProperties: FieldDisplayProperties? = null
    )

    @JsonInclude(Include.NON_NULL)
    data class Field (
        @field:NotBlank val key: String,
        val title: String,
        @field:NotBlank val value: String,
        val width: FieldWidth = FieldWidth.FULL,
        @field:Valid val displayProperties: FieldDisplayProperties? = null
    )

    enum class FieldWidth {
        FULL,
        HALF;

        val value: String
            @JsonValue get() = name.lowercase()
    }
}