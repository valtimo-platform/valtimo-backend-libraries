package com.ritense.form.repository;

import com.ritense.form.domain.IntermediateSubmission
import com.ritense.form.domain.IntermediateSubmissionId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface IntermediateSubmissionRepository : JpaRepository<IntermediateSubmission, IntermediateSubmissionId> {
    fun getByTaskInstanceId(taskInstanceId: String): IntermediateSubmission?
}