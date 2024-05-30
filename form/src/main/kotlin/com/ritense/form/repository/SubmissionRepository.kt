package com.ritense.form.repository;

import com.ritense.form.domain.IntermediateSubmission
import com.ritense.form.domain.SubmissionId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository(value = "formSubmissionRepository")
interface SubmissionRepository : JpaRepository<IntermediateSubmission, SubmissionId> {
    fun getByTaskInstanceId(taskInstanceId: String): IntermediateSubmission?
}