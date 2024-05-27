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

package com.ritense.case_.rest

import com.ritense.authorization.AuthorizationContext.Companion.runWithoutAuthorization
import com.ritense.case_.rest.dto.CaseWidgetTabDto
import com.ritense.case_.service.CaseWidgetTabService
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import com.ritense.valtimo.contract.domain.ValtimoMediaType.APPLICATION_JSON_UTF8_VALUE
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@SkipComponentScan
@RequestMapping("/api/management", produces = [APPLICATION_JSON_UTF8_VALUE])
class CaseWidgetTabManagementResource(
    private val caseWidgetTabService: CaseWidgetTabService
) {

    @GetMapping("/v1/case-definition/{caseDefinitionName}/widget-tab/{tabKey}")
    fun getCaseWidgetTab(
        @PathVariable caseDefinitionName: String,
        @PathVariable tabKey: String
    ): ResponseEntity<CaseWidgetTabDto> {
        val widgetTab =  runWithoutAuthorization {
            caseWidgetTabService.getWidgetTab(caseDefinitionName, tabKey)
        }
        return ResponseEntity.ofNullable(widgetTab)
    }

    @PostMapping("/v1/case-definition/{caseDefinitionName}/widget-tab/{tabKey}")
    fun updateCaseWidgetTab(
        @PathVariable caseDefinitionName: String,
        @PathVariable tabKey: String,
        @Valid @RequestBody caseWidgetTabDto: CaseWidgetTabDto
    ): ResponseEntity<CaseWidgetTabDto> {
        val widgetTab = runWithoutAuthorization {
            caseWidgetTabService.updateWidgetTab(caseWidgetTabDto)
        }
        return ResponseEntity.ofNullable(widgetTab)
    }
}
