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

package com.ritense.iko.web.rest

import com.ritense.iko.repository.ViewRepository
import com.ritense.iko.service.ViewService
import com.ritense.iko.web.rest.request.CreateViewRequest
import com.ritense.iko.web.rest.request.UpdateViewRequest
import com.ritense.iko.web.rest.response.View
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import com.ritense.valtimo.contract.domain.ValtimoMediaType.APPLICATION_JSON_UTF8_VALUE
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping

// TODO Security
@Controller
@SkipComponentScan
@RequestMapping("/api/management", produces = [APPLICATION_JSON_UTF8_VALUE])
class IkoViewManagementResource(
    private val viewService: ViewService,
    private val viewRepository: ViewRepository
) {

    @GetMapping("/v1/view")
    fun getViews(): ResponseEntity<List<View>> {
        // TODO PBAC
        val views = viewRepository.findAll().map { View.fromEntity(it) }
        return ResponseEntity.ok(views)
    }

    @PostMapping("/v1/view")
    fun createView(
        @Valid @RequestBody request: CreateViewRequest
    ): ResponseEntity<View> {
        // TODO PBAC
        //val view = runWithoutAuthorization {
        val view = viewService.createView(request)
        //}
        return ResponseEntity.ok(View.fromEntity(view))
    }

    @PostMapping("/v1/view")
    fun updateView(
        @Valid @RequestBody request: UpdateViewRequest
    ): ResponseEntity<View> {
        // TODO PBAC
        //val view = runWithoutAuthorization {
        val view = viewService.updateView(request)
        //}
        return ResponseEntity.ok(View.fromEntity(view))
    }
}
