/*
 * Copyright 2015-2023 Ritense BV, the Netherlands.
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

import com.ritense.case.service.CaseInstanceService
import com.ritense.case.web.rest.dto.CaseListRowDto
import com.ritense.document.domain.search.SearchWithConfigRequest
import com.ritense.tenancy.TenantResolver
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping(value = ["/api"], produces = [MediaType.APPLICATION_JSON_VALUE])
class CaseInstanceResource(
    private val service: CaseInstanceService,
    private val tenantResolver: TenantResolver
) {

    @PostMapping(value = ["/v1/case/{caseDefinitionName}/search"])
    fun search(
        @PathVariable(name = "caseDefinitionName") caseDefinitionName: String,
        @RequestBody searchRequest: SearchWithConfigRequest,
        pageable: Pageable
    ): ResponseEntity<Page<CaseListRowDto>> {
        searchRequest.tenantId = tenantResolver.getTenantId()
        val result = service.search(caseDefinitionName, searchRequest, pageable)
        return ResponseEntity.ok(result)
    }
}
