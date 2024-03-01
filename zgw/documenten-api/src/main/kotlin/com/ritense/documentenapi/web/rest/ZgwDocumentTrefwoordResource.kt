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

package com.ritense.documentenapi.web.rest

import com.ritense.documentenapi.domain.ZgwDocumentTrefwoord
import com.ritense.documentenapi.service.ZgwDocumentTrefwoordService
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import com.ritense.valtimo.contract.domain.ValtimoMediaType
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@SkipComponentScan
@RequestMapping("/api", produces = [ValtimoMediaType.APPLICATION_JSON_UTF8_VALUE])
class ZgwDocumentTrefwoordResource(
    val zgwDocumentTrefwoordService: ZgwDocumentTrefwoordService
) {

    @GetMapping("/management/v1/case-definition/{caseDefinitionName}/zgw-document/trefwoord")
    fun getTrefwoorden(
        @PathVariable(name = "caseDefinitionName") caseDefinitionName: String,
        pageable: Pageable,
    ): ResponseEntity<Page<ZgwDocumentTrefwoord>> {
        val page = zgwDocumentTrefwoordService.getTrefwoorden(caseDefinitionName, pageable)
        return ResponseEntity.ok(page)
    }

    @PostMapping("/management/v1/case-definition/{caseDefinitionName}/zgw-document/trefwoord/{trefwoord}")
    fun createTrefwoord(
        @PathVariable(name = "caseDefinitionName") caseDefinitionName: String,
        @PathVariable(name = "trefwoord") trefwoord: String,
    ): ResponseEntity<Page<ZgwDocumentTrefwoord>> {
        zgwDocumentTrefwoordService.createTrefwoord(caseDefinitionName, trefwoord)
        return ResponseEntity.noContent().build()
    }

    @DeleteMapping("/management/v1/case-definition/{caseDefinitionName}/zgw-document/trefwoord/{trefwoord}")
    fun deleteTrefwoord(
        @PathVariable(name = "caseDefinitionName") caseDefinitionName: String,
        @PathVariable(name = "trefwoord") trefwoord: String,
    ): ResponseEntity<Page<ZgwDocumentTrefwoord>> {
        zgwDocumentTrefwoordService.deleteTrefwoord(caseDefinitionName, trefwoord)
        return ResponseEntity.noContent().build()
    }
}
