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

package com.ritense.localization.web.rest

import com.fasterxml.jackson.databind.node.ObjectNode
import com.ritense.localization.service.LocalizationService
import com.ritense.localization.web.rest.dto.LocalizationResponseDto
import com.ritense.localization.web.rest.dto.LocalizationUpdateRequestDto
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import com.ritense.valtimo.contract.domain.ValtimoMediaType.APPLICATION_JSON_UTF8_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@SkipComponentScan
@RequestMapping("/api/management", produces = [APPLICATION_JSON_UTF8_VALUE])
class AdminLocalizationResource(
    private val localizationService: LocalizationService,
) {
    @PutMapping("/v1/localization/{languageKey}")
    fun editLocalization(
        @PathVariable(name = "languageKey") languageKey: String,
        @RequestBody content: ObjectNode
    ): ResponseEntity<ObjectNode> {
        val updatedLocalization = localizationService.updateLocalization(languageKey, content)
        return ResponseEntity.ok(updatedLocalization)
    }

    @PutMapping("/v1/localization")
    fun editLocalizations(
        @RequestBody localizations: List<LocalizationUpdateRequestDto>
    ): ResponseEntity<List<LocalizationResponseDto>> {
        val updatedLocalizations = localizationService.updateLocalizations(localizations)
        return ResponseEntity.ok(updatedLocalizations.map { LocalizationResponseDto.of(it) })
    }
}
