package com.ritense.form.service

import com.fasterxml.jackson.databind.node.ObjectNode
import com.ritense.authorization.AuthorizationService
import com.ritense.authorization.request.EntityAuthorizationRequest
import com.ritense.form.domain.IntermediateSubmission
import com.ritense.form.repository.IntermediateSubmissionRepository
import com.ritense.form.util.EventDispatcherHelper.Companion.dispatchEvents
import com.ritense.logging.LoggableResource
import com.ritense.valtimo.camunda.authorization.CamundaTaskActionProvider.Companion.COMPLETE
import com.ritense.valtimo.camunda.authorization.CamundaTaskActionProvider.Companion.VIEW
import com.ritense.valtimo.camunda.domain.CamundaTask
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import com.ritense.valtimo.contract.authentication.UserManagementService
import com.ritense.valtimo.service.CamundaTaskService
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Transactional
@Service
@SkipComponentScan
class IntermediateSubmissionService(
    private val intermediateSubmissionRepository: IntermediateSubmissionRepository,
    private val userManagementService: UserManagementService,
    private val authorizationService: AuthorizationService,
    private val camundaTaskService: CamundaTaskService
) {

    fun get(
        @LoggableResource(resourceType = CamundaTask::class) taskInstanceId: String
    ): IntermediateSubmission? {
        val task = camundaTaskService.findTaskById(taskInstanceId)
        authorizationService.requirePermission(
            EntityAuthorizationRequest(CamundaTask::class.java, VIEW, task)
        )
        return intermediateSubmissionRepository.getByTaskInstanceId(taskInstanceId)
    }

    fun store(
        submission: ObjectNode,
        @LoggableResource(resourceType = CamundaTask::class) taskInstanceId: String
    ): IntermediateSubmission {
        val task = camundaTaskService.findTaskById(taskInstanceId)
        authorizationService.requirePermission(
            EntityAuthorizationRequest(CamundaTask::class.java, COMPLETE, task)
        )
        val currentUser: String = userManagementService.currentUser.userIdentifier
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
        @LoggableResource(resourceType = CamundaTask::class) taskInstanceId: String
    ) {
        val task = camundaTaskService.findTaskById(taskInstanceId)
        authorizationService.requirePermission(
            EntityAuthorizationRequest(CamundaTask::class.java, COMPLETE, task)
        )
        intermediateSubmissionRepository.getByTaskInstanceId(taskInstanceId)?.let { intermediateSubmission ->
            intermediateSubmissionRepository.deleteById(intermediateSubmission.intermediateSubmissionId)
        }
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }

}