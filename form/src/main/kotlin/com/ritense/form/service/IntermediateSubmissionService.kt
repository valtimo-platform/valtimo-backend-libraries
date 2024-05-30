package com.ritense.form.service

import com.fasterxml.jackson.databind.node.ObjectNode
import com.ritense.form.domain.IntermediateSubmission
import com.ritense.form.repository.SubmissionRepository
import mu.KotlinLogging
import org.springframework.transaction.annotation.Transactional

@Transactional
class IntermediateSubmissionService(
    private val submissionRepository: SubmissionRepository,
) {

    fun get(taskInstanceId: String) = submissionRepository.getByTaskInstanceId(taskInstanceId)

    fun store(
        submission: ObjectNode,
        taskInstanceId: String,
    ): IntermediateSubmission {
        val existingIntermediateSubmission = submissionRepository.getByTaskInstanceId(taskInstanceId)
        if (existingIntermediateSubmission != null) {
            return submissionRepository.save(
                existingIntermediateSubmission.changeSubmissionContent(
                    content = submission
                )
            ).also {
                logger.info { "Updated existing intermediate submission for taskInstanceId($taskInstanceId)" }
            }
        } else {
            return submissionRepository.save(
                IntermediateSubmission.new(
                    content = submission,
                    taskInstanceId = taskInstanceId
                )
            ).also {
                logger.info { "Inserted intermediate submission for taskInstanceId($taskInstanceId)" }
            }
        }
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }

}