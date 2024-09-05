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

package com.ritense.processlink.url.web.rest

import com.ritense.processlink.url.service.URLProcessLinkService
import com.ritense.processlink.url.web.rest.dto.URLVariables
import com.ritense.processlink.url.web.rest.dto.URLSubmissionResult
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import com.ritense.valtimo.contract.domain.ValtimoMediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@SkipComponentScan
@RequestMapping("/api", produces = [ValtimoMediaType.APPLICATION_JSON_UTF8_VALUE])
class URLProcessLinkResource(
    val urlProcessLinkService: URLProcessLinkService
) {

    @PostMapping("/v1/process-link/url/{processLinkId}")
    fun handleSubmission(
        @PathVariable processLinkId: UUID,
        @RequestParam(required = false) documentDefinitionName: String?,
        @RequestParam(required = false) documentId: String?,
        @RequestParam(required = false) taskInstanceId: String?,
    ): URLSubmissionResult {
        return urlProcessLinkService.submit(
            processLinkId,
            documentDefinitionName,
            documentId,
            taskInstanceId
        )
    }

    @GetMapping("/v1/process-link/url/variables")
    fun getDefaultUrl(
    ): URLVariables {
        return URLVariables(urlProcessLinkService.getVariables().url)
    }

}