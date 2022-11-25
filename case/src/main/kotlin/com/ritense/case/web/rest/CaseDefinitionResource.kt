/*
 * Copyright 2015-2022 Ritense BV, the Netherlands.
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

package com.ritense.case.web.rest

import com.ritense.case.domain.CaseDefinitionSettings
import com.ritense.case.service.CaseDefinitionService
import com.ritense.case.web.rest.dto.CaseSettingsDto
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping(value = ["/api"])
class CaseDefinitionResource(
    private val service: CaseDefinitionService
) {

    @GetMapping(value = ["/v1/case/{caseDefinitionName}/settings"])
    fun getCaseSettings(
        @PathVariable caseDefinitionName: String
    ): ResponseEntity<CaseDefinitionSettings> {
        return ResponseEntity.ok(
            service.getCaseSettings(caseDefinitionName)
        )
    }

    @PatchMapping(value = ["/v1/case/{caseDefinitionName}/settings"])
    fun updateCaseSettings(
        @RequestBody caseSettingsDto: CaseSettingsDto,
        @PathVariable caseDefinitionName: String
    ): ResponseEntity<CaseDefinitionSettings> {
        return ResponseEntity.ok(
            service.updateCaseSettings(caseDefinitionName, caseSettingsDto)
        )
    }
}