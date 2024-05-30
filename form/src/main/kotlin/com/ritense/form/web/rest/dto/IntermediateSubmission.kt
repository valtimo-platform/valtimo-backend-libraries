package com.ritense.form.web.rest.dto

import com.fasterxml.jackson.databind.node.ObjectNode
import java.time.LocalDateTime
import com.ritense.form.domain.IntermediateSubmission as IntermediateSubmissionDomain

data class IntermediateSubmission(
    val submission: ObjectNode,
    val taskInstanceId: String,
    val createdBy: String,
    val createdOn: LocalDateTime,
    val editedBy: String?,
    val editedOn: LocalDateTime?
)

fun IntermediateSubmissionDomain.toResponse() = IntermediateSubmission(
    submission = this.content,
    taskInstanceId = this.taskInstanceId,
    createdBy = this.createdBy,
    createdOn = this.createdOn,
    editedBy = this.editedBy,
    editedOn = this.editedOn
)