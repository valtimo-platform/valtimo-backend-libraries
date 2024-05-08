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

import com.ritense.documentenapi.domain.DocumentenApiVersion

data class DocumentenApiVersionManagementDto(
    val selectedVersion: String? = null,
    val detectedVersions: List<String>,
    val supportsFilterableColumns: Boolean,
    val supportsSortableColumns: Boolean,
    val supportsTrefwoorden: Boolean,
) {
    companion object {
        fun of(detectedVersions: List<DocumentenApiVersion>): DocumentenApiVersionManagementDto {
            val version = detectedVersions.lastOrNull()
            return DocumentenApiVersionManagementDto(
                selectedVersion = version?.version,
                detectedVersions = detectedVersions.map { it.version },
                supportsFilterableColumns = version?.supportsFilterableColumns() ?: false,
                supportsSortableColumns = version?.supportsSortableColumns() ?: false,
                supportsTrefwoorden = version?.supportsTrefwoorden ?: false,
            )
        }
    }
}