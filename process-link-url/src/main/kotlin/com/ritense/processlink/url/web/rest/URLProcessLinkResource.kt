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