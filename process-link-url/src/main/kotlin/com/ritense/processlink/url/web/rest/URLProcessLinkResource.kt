package com.ritense.processlink.url.web.rest

import com.ritense.processlink.url.service.URLProcessLinkSubmissionService
import com.ritense.processlink.url.web.rest.dto.URLSubmissionResult
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import com.ritense.valtimo.contract.domain.ValtimoMediaType
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
    val urlProcessLinkSubmissionService: URLProcessLinkSubmissionService
) {

    @PostMapping("/v1/process-link/url/{processLinkId}")
    fun handleSubmission(
        @PathVariable processLinkId: UUID,
        @RequestParam(required = false) documentDefinitionName: String?,
        @RequestParam(required = false) documentId: String?,
        @RequestParam(required = false) taskInstanceId: String?,
    ): URLSubmissionResult {
        return urlProcessLinkSubmissionService.submit(
            processLinkId,
            documentDefinitionName,
            documentId,
            taskInstanceId
        )
    }

}