package com.ritense.form.service

import com.fasterxml.jackson.databind.node.ObjectNode
import com.ritense.authorization.AuthorizationService
import com.ritense.authorization.request.EntityAuthorizationRequest
import com.ritense.form.domain.IntermediateSubmission
import com.ritense.form.repository.IntermediateSubmissionRepository
import com.ritense.form.util.EventDispatcherHelper.Companion.dispatchEvents
import com.ritense.logging.LoggableResource
import com.ritense.valtimo.operaton.authorization.OperatonTaskActionProvider.Companion.COMPLETE
import com.ritense.valtimo.operaton.authorization.OperatonTaskActionProvider.Companion.VIEW
import com.ritense.valtimo.operaton.domain.OperatonTask
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import com.ritense.valtimo.contract.authentication.UserManagementService
import com.ritense.valtimo.service.OperatonTaskService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Transactional
@Service
@SkipComponentScan
class IntermediateSubmissionService(
    private val intermediateSubmissionRepository: IntermediateSubmissionRepository,
    private val userManagementService: UserManagementService,
    private val authorizationService: AuthorizationService,
    private val operatonTaskService: OperatonTaskService
) {

    fun get(
        @LoggableResource(resourceType = OperatonTask::class) taskInstanceId: String
    ): IntermediateSubmission? {
        val task = operatonTaskService.findTaskById(taskInstanceId)
        authorizationService.requirePermission(
            EntityAuthorizationRequest(OperatonTask::class.java, VIEW, task)
        )
        return intermediateSubmissionRepository.getByTaskInstanceId(taskInstanceId)
    }

    fun store(
        submission: ObjectNode,
        @LoggableResource(resourceType = OperatonTask::class) taskInstanceId: String
    ): IntermediateSubmission {
        val task = operatonTaskService.findTaskById(taskInstanceId)
        authorizationService.requirePermission(
            EntityAuthorizationRequest(OperatonTask::class.java, COMPLETE, task)
        )
        val currentUser: String = userManagementService.currentUser.username
        val existingIntermediateSubmission = intermediateSubmissionRepository.getByTaskInstanceId(taskInstanceId)
        if (existingIntermediateSubmission != null) {
            return intermediateSubmissionRepository.save(
                existingIntermediateSubmission.changeSubmissionContent(
                    editedBy = currentUser,
                    content = submission
                )
            ).also {
                dispatchEvents(it)
                logger.info { "Updated existing intermediate submission for taskInstanceId($taskInstanceId)" }
            }
        } else {
            return intermediateSubmissionRepository.save(
                IntermediateSubmission.new(
                    createdBy = currentUser,
                    content = submission,
                    taskInstanceId = taskInstanceId
                )
            ).also {
                dispatchEvents(it)
                logger.info { "Inserted intermediate submission for taskInstanceId($taskInstanceId)" }
            }
        }
    }

    fun clear(
        @LoggableResource(resourceType = OperatonTask::class) taskInstanceId: String
    ) {
        val task = operatonTaskService.findTaskById(taskInstanceId)
        authorizationService.requirePermission(
            EntityAuthorizationRequest(OperatonTask::class.java, COMPLETE, task)
        )
        intermediateSubmissionRepository.getByTaskInstanceId(taskInstanceId)?.let { intermediateSubmission ->
            intermediateSubmissionRepository.deleteById(intermediateSubmission.intermediateSubmissionId)
        }
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }

}