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

package com.ritense.form.domain

import com.fasterxml.jackson.databind.node.ObjectNode
import com.ritense.form.event.IntermediateSubmissionChangedEvent
import com.ritense.form.event.IntermediateSubmissionCreatedEvent
import com.ritense.outbox.domain.BaseEvent
import com.ritense.valtimo.contract.domain.AggregateRoot
import com.ritense.valtimo.contract.utils.SecurityUtils
import com.ritense.valtimo.contract.validation.Validatable
import io.hypersistence.utils.hibernate.type.json.JsonType
import jakarta.persistence.Column
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.Table
import org.hibernate.annotations.Type
import java.io.Serializable
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "intermediate_submission")
class IntermediateSubmission(
    @EmbeddedId
    val intermediateSubmissionId: IntermediateSubmissionId,

    @Column(name = "task_instance_id", updatable = false, unique = true)
    val taskInstanceId: String,

    @Type(value = JsonType::class)
    @Column(name = "content")
    val content: ObjectNode,

    @Column(name = "created_on", updatable = false)
    val createdOn: LocalDateTime,

    @Column(name = "created_by", updatable = false)
    val createdBy: String,

    @Column(name = "edited_by")
    val editedBy: String? = null,

    @Column(name = "edited_on")
    val editedOn: LocalDateTime? = null

) : AggregateRoot<BaseEvent>(), Validatable, Serializable {

    init {
        validate()
    }

    companion object {

        fun new(
            intermediateSubmissionId: IntermediateSubmissionId = IntermediateSubmissionId.newId(UUID.randomUUID()),
            content: ObjectNode,
            taskInstanceId: String,
            createdBy: String = SecurityUtils.getCurrentUserLogin(),
            createdOn: LocalDateTime = LocalDateTime.now()
        ): IntermediateSubmission {
            val intermediateSubmission = IntermediateSubmission(
                intermediateSubmissionId = intermediateSubmissionId,
                taskInstanceId = taskInstanceId,
                content = content,
                createdBy = createdBy,
                createdOn = createdOn
            )
            intermediateSubmission.registerEvent(
                IntermediateSubmissionCreatedEvent(
                    intermediateSubmissionId = intermediateSubmissionId.id,
                    taskInstanceId = intermediateSubmission.taskInstanceId,
                    content = intermediateSubmission.content,
                    createdOn = intermediateSubmission.createdOn,
                    createdBy = intermediateSubmission.createdBy,
                    editedBy = intermediateSubmission.editedBy,
                    editedOn = intermediateSubmission.editedOn
                )
            )
            return intermediateSubmission
        }
    }

    fun changeSubmissionContent(
        content: ObjectNode,
        editedBy: String = SecurityUtils.getCurrentUserLogin(),
        editedOn: LocalDateTime = LocalDateTime.now()
    ): IntermediateSubmission {
        val intermediateSubmission = IntermediateSubmission(
            intermediateSubmissionId = intermediateSubmissionId,
            taskInstanceId = taskInstanceId,
            content = content,
            createdOn = createdOn,
            createdBy = createdBy,
            editedBy = editedBy,
            editedOn = editedOn
        )
        intermediateSubmission.registerEvent(
            IntermediateSubmissionChangedEvent(
                intermediateSubmissionId = intermediateSubmissionId.id,
                taskInstanceId = this.taskInstanceId,
                content = content,
                createdOn = createdOn,
                createdBy = createdBy,
                editedBy = editedBy,
                editedOn = editedOn
            )
        )
        return intermediateSubmission
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as IntermediateSubmission

        if (intermediateSubmissionId != other.intermediateSubmissionId) return false
        if (content != other.content) return false
        if (createdOn != other.createdOn) return false
        if (createdBy != other.createdBy) return false
        if (taskInstanceId != other.taskInstanceId) return false
        if (editedBy != other.editedBy) return false
        if (editedOn != other.editedOn) return false

        return true
    }

    override fun hashCode(): Int {
        var result = intermediateSubmissionId.hashCode()
        result = 31 * result + content.hashCode()
        result = 31 * result + createdOn.hashCode()
        result = 31 * result + createdBy.hashCode()
        result = 31 * result + (taskInstanceId?.hashCode() ?: 0)
        result = 31 * result + (editedBy?.hashCode() ?: 0)
        result = 31 * result + (editedOn?.hashCode() ?: 0)
        return result
    }

}