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

import com.fasterxml.jackson.annotation.JsonAnyGetter
import com.fasterxml.jackson.annotation.JsonAnySetter

open class DocumentenApiVersion(
    open val version: String,
    open val filterableColumns: List<String> = emptyList(),
    open val sortableColumns: List<String> = emptyList(),
    open val supportsTrefwoorden: Boolean = false,

    @JsonAnySetter
    @get:JsonAnyGetter
    open val unmappedFields: Map<String, Any?> = mapOf(),
) : Comparable<DocumentenApiVersion> {

    open fun supportsFilterableColumns(): Boolean = filterableColumns.isNotEmpty()
    open fun supportsSortableColumns(): Boolean = sortableColumns.isNotEmpty()

    open fun isColumnFilterable(columnKey: DocumentenApiColumnKey) =
        filterableColumns.contains(columnKey.property)

    open fun isColumnSortable(columnKey: DocumentenApiColumnKey) = sortableColumns.contains(columnKey.property)

    override fun compareTo(other: DocumentenApiVersion) = version.compareTo(other.version)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DocumentenApiVersion
        return version == other.version
    }

    override fun hashCode() = version.hashCode()

    override fun toString(): String = version
}
