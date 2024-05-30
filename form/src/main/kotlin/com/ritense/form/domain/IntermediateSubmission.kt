package com.ritense.form.domain

import com.fasterxml.jackson.databind.node.ObjectNode
import com.ritense.valtimo.contract.utils.SecurityUtils
import com.ritense.valtimo.contract.validation.Validatable
import jakarta.persistence.Column
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.Table
import java.io.Serializable
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "form_submission")
class IntermediateSubmission(
    @EmbeddedId
    val submissionId: SubmissionId,

    @Column(name = "content", columnDefinition = "json")
    val content: ObjectNode,

    @Column(name = "task_instance_id", updatable = false)
    val taskInstanceId: String,

    @Column(name = "created_on", updatable = false)
    val createdOn: LocalDateTime,

    @Column(name = "created_by", updatable = false)
    val createdBy: String,

    @Column(name = "edited_by")
    val editedBy: String? = null,

    @Column(name = "edited_on")
    val editedOn: LocalDateTime? = null

) : Validatable, Serializable {

    init {
        validate()
    }

    companion object {

        fun new(
            submissionId: SubmissionId = SubmissionId.newId(UUID.randomUUID()),
            content: ObjectNode,
            taskInstanceId: String,
            createdBy: String = SecurityUtils.getCurrentUserLogin(),
            createdOn: LocalDateTime = LocalDateTime.now()
        ): IntermediateSubmission {
            return IntermediateSubmission(
                submissionId = submissionId,
                content = content,
                taskInstanceId = taskInstanceId,
                createdBy = createdBy,
                createdOn = createdOn
            )
        }
    }

    fun changeSubmissionContent(
        content: ObjectNode,
        editedBy: String = SecurityUtils.getCurrentUserLogin(),
        editedOn: LocalDateTime = LocalDateTime.now()
    ): IntermediateSubmission {
        return IntermediateSubmission(
            submissionId = submissionId,
            content = content,
            createdOn = createdOn,
            createdBy = createdBy,
            taskInstanceId = taskInstanceId,
            editedBy = editedBy,
            editedOn = editedOn
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as IntermediateSubmission

        if (submissionId != other.submissionId) return false
        if (content != other.content) return false
        if (createdOn != other.createdOn) return false
        if (createdBy != other.createdBy) return false
        if (taskInstanceId != other.taskInstanceId) return false
        if (editedBy != other.editedBy) return false
        if (editedOn != other.editedOn) return false

        return true
    }

    override fun hashCode(): Int {
        var result = submissionId.hashCode()
        result = 31 * result + content.hashCode()
        result = 31 * result + createdOn.hashCode()
        result = 31 * result + createdBy.hashCode()
        result = 31 * result + (taskInstanceId?.hashCode() ?: 0)
        result = 31 * result + (editedBy?.hashCode() ?: 0)
        result = 31 * result + (editedOn?.hashCode() ?: 0)
        return result
    }

}