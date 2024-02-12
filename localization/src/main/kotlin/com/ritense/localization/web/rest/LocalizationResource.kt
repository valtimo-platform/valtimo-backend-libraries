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
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import com.ritense.valtimo.contract.domain.ValtimoMediaType.APPLICATION_JSON_UTF8_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@SkipComponentScan
@RequestMapping("/api", produces = [APPLICATION_JSON_UTF8_VALUE])
class LocalizationResource(
    private val localizationService: LocalizationService
) {

    @GetMapping("/v1/localization")
    fun getLocalizations(): ResponseEntity<List<LocalizationResponseDto>> {
        val localizationResponseDtos = localizationService.getLocalizations()
            .map { LocalizationResponseDto.of(it) }
        return ResponseEntity.ok(localizationResponseDtos)
    }

    @GetMapping("/v1/localization/{languageKey}")
    fun getLocalization(@PathVariable languageKey: String): ResponseEntity<ObjectNode> {
        val data = localizationService.getLocalization(languageKey)
        return ResponseEntity.ok(data)
    }
}
