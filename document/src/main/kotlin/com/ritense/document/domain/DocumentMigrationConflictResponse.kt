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

package com.ritense.document.domain

data class DocumentMigrationConflictResponse(
    val documentDefinitionNameSource: String,
    val documentDefinitionVersionSource: Long,
    val documentDefinitionNameTarget: String,
    val documentDefinitionVersionTarget: Long,
    val conflicts: List<DocumentMigrationConflict> = emptyList(),
    val errors: List<String> = emptyList(),
    val documentCount: Int? = null
) {
    companion object {
        fun of(
            migrationRequest: DocumentMigrationRequest,
            conflicts: List<DocumentMigrationConflict> = emptyList(),
            errors: List<String> = emptyList(),
            documentCount: Int? = null
        ): DocumentMigrationConflictResponse {

            val combinedConflicts = migrationRequest.getConflicts().toMutableList()
            combinedConflicts.replaceAll { existing -> conflicts.firstOrNull { it == existing } ?: existing }
            combinedConflicts += conflicts.filter { !combinedConflicts.contains(it) }

            return DocumentMigrationConflictResponse(
                documentDefinitionNameSource = migrationRequest.documentDefinitionNameSource,
                documentDefinitionVersionSource = migrationRequest.documentDefinitionVersionSource,
                documentDefinitionNameTarget = migrationRequest.documentDefinitionNameTarget,
                documentDefinitionVersionTarget = migrationRequest.documentDefinitionVersionTarget,
                conflicts = combinedConflicts,
                errors = errors,
                documentCount = documentCount,
            )
        }
    }
}