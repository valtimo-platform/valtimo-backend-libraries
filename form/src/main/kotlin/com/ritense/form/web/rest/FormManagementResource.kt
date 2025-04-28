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

package com.ritense.form.web.rest

import com.ritense.form.domain.FormDefinition
import com.ritense.form.domain.request.CreateFormDefinitionRequest
import com.ritense.form.domain.request.ModifyFormDefinitionRequest
import com.ritense.form.service.FormDefinitionService
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import com.ritense.valtimo.contract.case_.CaseDefinitionId
import com.ritense.valtimo.contract.domain.ValtimoMediaType.APPLICATION_JSON_UTF8_VALUE
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort.Direction.ASC
import org.springframework.data.web.PageableDefault
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.Optional
import java.util.UUID


@RestController
@SkipComponentScan
@RequestMapping("/api", produces = [APPLICATION_JSON_UTF8_VALUE])
class FormManagementResource(
    private val formDefinitionService: FormDefinitionService,
) {
    @GetMapping("/management/v1/case-definition/{caseDefinitionKey}/version/{versionTag}/form")
    fun getFormDefinitions(
        @PathVariable("caseDefinitionKey") caseDefinitionKey: String,
        @PathVariable("versionTag") versionTag: String,
        @RequestParam("searchTerm", required = false) searchTerm: String? = "",
        pageable: Pageable,
    ): ResponseEntity<Page<out FormDefinition>> {
        return ResponseEntity.ok(
            formDefinitionService.queryFormDefinitions(
                CaseDefinitionId.of(caseDefinitionKey, versionTag),
                searchTerm,
                pageable,
            )
        )
    }

    @GetMapping("/management/v1/case-definition/{caseDefinitionKey}/version/{versionTag}/form/{formDefinitionId}")
    fun getFormDefinition(
        @PathVariable("caseDefinitionKey") caseDefinitionKey: String,
        @PathVariable("versionTag") versionTag: String,
        @PathVariable("formDefinitionId") formDefinitionId: String,
    ): ResponseEntity<
        Optional<out FormDefinition>> {
        return ResponseEntity.ok(
            formDefinitionService.getFormDefinitionById(
                CaseDefinitionId(caseDefinitionKey, versionTag),
                UUID.fromString(formDefinitionId),
            )
        )
    }

    @PostMapping("/management/v1/case-definition/{caseDefinitionKey}/version/{versionTag}/form")
    fun createFormDefinition(
        @PathVariable("caseDefinitionKey") caseDefinitionKey: String,
        @PathVariable("versionTag") versionTag: String,
        @RequestBody formDefinition: CreateFormDefinitionRequest,
    ): ResponseEntity<FormDefinition> {
        return ResponseEntity.ok(
            formDefinitionService.createFormDefinition(
                CaseDefinitionId.of(caseDefinitionKey, versionTag),
                formDefinition,
            )
        )
    }

    @PutMapping("/management/v1/case-definition/{caseDefinitionKey}/version/{versionTag}/form")
    fun updateFormDefinition(
        @PathVariable("caseDefinitionKey") caseDefinitionKey: String,
        @PathVariable("versionTag") versionTag: String,
        @RequestBody formDefinition: ModifyFormDefinitionRequest,
    ): ResponseEntity<FormDefinition> {
        return ResponseEntity.ok(
            formDefinitionService.modifyFormDefinition(
                CaseDefinitionId.of(caseDefinitionKey, versionTag),
                formDefinition,
            )
        )
    }

    @DeleteMapping("/management/v1/case-definition/{caseDefinitionKey}/version/{versionTag}/form/{formDefinitionId}")
    fun deleteFormDefinition(
        @PathVariable("caseDefinitionKey") caseDefinitionKey: String,
        @PathVariable("versionTag") versionTag: String,
        @PathVariable("formDefinitionId") formDefinitionId: String,
    ): ResponseEntity<Any> {
        formDefinitionService.deleteFormDefinition(
            CaseDefinitionId.of(caseDefinitionKey, versionTag),
            UUID.fromString(formDefinitionId)
        )
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/management/v1/case-definition/{caseDefinitionKey}/version/{versionTag}/form/{name}/exists")
    fun formDefinitionExists(
        @PathVariable("caseDefinitionKey") caseDefinitionKey: String,
        @PathVariable("versionTag") versionTag: String,
        @PathVariable("name") formName: String,
    ): ResponseEntity<Boolean> {
        return ResponseEntity.ok(
            !formDefinitionService.getFormDefinitionByName(
                formName,
                CaseDefinitionId(caseDefinitionKey, versionTag),
            ).isEmpty
        )
    }

    @GetMapping("/v1/form-management")
    fun getFormDefinitions(
        @RequestParam(required = false) searchTerm: String?,
        @PageableDefault(sort = ["name"], direction = ASC) pageable: Pageable?
    ): ResponseEntity<Page<out FormDefinition?>> {
        return if (searchTerm.isNullOrEmpty()) {
            ResponseEntity.ok(formDefinitionService.getAll(pageable))
        } else {
            ResponseEntity.ok(formDefinitionService.queryFormDefinitions(searchTerm, pageable))
        }
    }

    @GetMapping("/v1/form-management/exists/{name}")
    fun formDefinitionExists(
        @PathVariable("name") formName: String,
    ): ResponseEntity<Boolean> {
        return ResponseEntity.ok(
            !formDefinitionService.getFormDefinitionByName(
                formName
            ).isEmpty
        )
    }

    @PostMapping("/v1/form-management")
    fun createFormDefinition(
        @RequestBody formDefinition: CreateFormDefinitionRequest,
    ): ResponseEntity<FormDefinition> {
        return ResponseEntity.ok(
            formDefinitionService.createFormDefinition(
                formDefinition,
            )
        )
    }

    @GetMapping("/v1/form-management/{formDefinitionId}")
    fun getFormDefinition(
        @PathVariable("formDefinitionId") formDefinitionId: String,
    ): ResponseEntity<
        Optional<out FormDefinition>> {
        return ResponseEntity.ok(
            formDefinitionService.getFormDefinitionById(
                UUID.fromString(formDefinitionId),
            )
        )
    }

    @PutMapping("/v1/form-management")
    fun updateFormDefinition(
        @RequestBody formDefinition: ModifyFormDefinitionRequest,
    ): ResponseEntity<FormDefinition> {
        return ResponseEntity.ok(
            formDefinitionService.modifyFormDefinition(
                formDefinition,
            )
        )
    }

    @DeleteMapping("/v1/form-management/{formDefinitionId}")
    fun deleteFormDefinition(
        @PathVariable("formDefinitionId") formDefinitionId: String,
    ): ResponseEntity<Any> {
        formDefinitionService.deleteFormDefinition(
            UUID.fromString(formDefinitionId)
        )
        return ResponseEntity.noContent().build();
    }
}
