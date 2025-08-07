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

package com.ritense.document.web.rest

import com.ritense.authorization.annotation.RunWithoutAuthorization
import com.ritense.document.domain.DocumentMigrationConflictResponse
import com.ritense.document.domain.DocumentMigrationRequest
import com.ritense.document.service.DocumentMigrationService
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import com.ritense.valtimo.contract.domain.ValtimoMediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@SkipComponentScan
@RequestMapping("/api/management", produces = [ValtimoMediaType.APPLICATION_JSON_UTF8_VALUE])
class DocumentMigrationManagementResource(
    private val documentMigrationService: DocumentMigrationService,
) {

    @RunWithoutAuthorization
    @PostMapping("/v1/document-definition/migration/conflicts")
    fun getConflicts(
        @RequestBody documentMigrationRequest: DocumentMigrationRequest,
    ): ResponseEntity<DocumentMigrationConflictResponse> {
        val conflicts = documentMigrationService.getConflicts(documentMigrationRequest)
        return ResponseEntity.ok(conflicts)
    }

    @RunWithoutAuthorization
    @PostMapping("/v1/document-definition/migrate")
    fun migrateDocuments(
        @RequestBody documentMigrationRequest: DocumentMigrationRequest,
    ): ResponseEntity<Unit> {
        documentMigrationService.migrateDocuments(documentMigrationRequest)
        return ResponseEntity.ok().build()
    }

}