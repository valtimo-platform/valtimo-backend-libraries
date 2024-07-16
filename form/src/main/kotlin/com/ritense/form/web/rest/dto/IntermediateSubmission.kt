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

package com.ritense.form.web.rest.dto

import com.fasterxml.jackson.databind.node.ObjectNode
import com.ritense.form.domain.FormSpringContextHelper
import com.ritense.valtimo.contract.authentication.UserManagementService
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

fun IntermediateSubmissionDomain.toResponse(): IntermediateSubmission {
    val userManagementService = FormSpringContextHelper.getBean(UserManagementService::class.java)
    return IntermediateSubmission(
        submission = this.content,
        taskInstanceId = this.taskInstanceId,
        createdBy = this.createdBy.let { userManagementService.findByUserIdentifier(it).fullName },
        createdOn = this.createdOn,
        editedBy = this.editedBy?.let { userManagementService.findByUserIdentifier(it).fullName },
        editedOn = this.editedOn
    )
}