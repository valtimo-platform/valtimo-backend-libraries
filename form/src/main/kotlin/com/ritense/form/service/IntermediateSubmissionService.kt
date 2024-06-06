package com.ritense.form.service

import com.fasterxml.jackson.databind.node.ObjectNode
import com.ritense.form.domain.IntermediateSubmission
import com.ritense.form.repository.IntermediateSubmissionRepository
import mu.KotlinLogging
import org.springframework.transaction.annotation.Transactional

@Transactional
class IntermediateSubmissionService(
    private val intermediateSubmissionRepository: IntermediateSubmissionRepository
) {

    fun get(taskInstanceId: String) = intermediateSubmissionRepository.getByTaskInstanceId(taskInstanceId)

    fun store(
        submission: ObjectNode,
        taskInstanceId: String,
    ): IntermediateSubmission {
        val existingIntermediateSubmission = intermediateSubmissionRepository.getByTaskInstanceId(taskInstanceId)
        if (existingIntermediateSubmission != null) {
            return intermediateSubmissionRepository.save(
                existingIntermediateSubmission.changeSubmissionContent(
                    content = submission
                )
            ).also {
                logger.info { "Updated existing intermediate submission for taskInstanceId($taskInstanceId)" }
            }
        } else {
            return intermediateSubmissionRepository.save(
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