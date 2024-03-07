package com.ritense.form.service

import com.fasterxml.jackson.databind.JsonNode
import com.ritense.form.web.rest.dto.FormSubmissionResult
import java.util.UUID

interface FormSubmissionService {

    fun handleSubmission(
        processLinkId: UUID,
        formData: JsonNode,
        documentId: String?,
        taskInstanceId: String?,
        tenantId: String
    ): FormSubmissionResult

}