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

package com.ritense.form.event

import com.fasterxml.jackson.databind.node.ObjectNode
import com.ritense.outbox.domain.BaseEvent
import com.ritense.valtimo.contract.domain.DomainEvent
import com.ritense.valtimo.contract.json.MapperSingleton
import java.time.LocalDateTime
import java.util.UUID

data class IntermediateSubmissionCreatedEvent(
    val intermediateSubmissionId: UUID,
    val taskInstanceId: String,
    val content: ObjectNode,
    val createdOn: LocalDateTime,
    val createdBy: String,
    val editedBy: String? = null,
    val editedOn: LocalDateTime? = null
) : DomainEvent, BaseEvent(
    type = "com.ritense.form.submission.created",
    resultType = "com.ritense.document.domain.IntermediateSubmissionCreated",
    resultId = intermediateSubmissionId.toString(),
    result = MapperSingleton.get().createObjectNode().apply {
        put("intermediateSubmissionId", intermediateSubmissionId.toString())
        put("taskInstanceId", taskInstanceId)
        put("content", content.toString())
        put("createdOn", createdOn.toString())
        put("createdBy", createdBy)
        put("editedBy", editedBy)
        put("editedOn", editedOn.toString())
    }
)