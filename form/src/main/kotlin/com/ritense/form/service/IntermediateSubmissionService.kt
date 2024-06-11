package com.ritense.form.service

import com.fasterxml.jackson.databind.node.ObjectNode
import com.ritense.form.domain.IntermediateSubmission
import com.ritense.form.repository.IntermediateSubmissionRepository
import com.ritense.form.util.EventDispatcherHelper.Companion.dispatchEvents
import mu.KotlinLogging
import org.springframework.transaction.annotation.Transactional

open class IntermediateSubmissionService(
    private val intermediateSubmissionRepository: IntermediateSubmissionRepository
) {

    open fun get(taskInstanceId: String) = intermediateSubmissionRepository.getByTaskInstanceId(taskInstanceId)

    @Transactional
    open fun store(
        submission: ObjectNode,
        taskInstanceId: String
    ): IntermediateSubmission {
        val existingIntermediateSubmission = intermediateSubmissionRepository.getByTaskInstanceId(taskInstanceId)
        if (existingIntermediateSubmission != null) {
            return intermediateSubmissionRepository.save(
                existingIntermediateSubmission.changeSubmissionContent(
                    content = submission
                )
            ).also {
                dispatchEvents(it)
                logger.info { "Updated existing intermediate submission for taskInstanceId($taskInstanceId)" }
            }
        } else {
            return intermediateSubmissionRepository.save(
                IntermediateSubmission.new(
                    content = submission,
                    taskInstanceId = taskInstanceId
                )
            ).also {
                dispatchEvents(it)
                logger.info { "Inserted intermediate submission for taskInstanceId($taskInstanceId)" }
            }
        }
    }

    @Transactional
    open fun clear(taskInstanceId: String) {
        intermediateSubmissionRepository.getByTaskInstanceId(taskInstanceId)?.let { intermediateSubmission ->
            intermediateSubmissionRepository.deleteById(intermediateSubmission.intermediateSubmissionId)
        }
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }

}