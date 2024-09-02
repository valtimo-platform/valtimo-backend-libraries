package com.ritense.formviewmodel.service

import com.ritense.authorization.AuthorizationContext.Companion.runWithoutAuthorization
import com.ritense.authorization.AuthorizationService
import com.ritense.authorization.request.EntityAuthorizationRequest
import com.ritense.valtimo.camunda.authorization.CamundaExecutionActionProvider
import com.ritense.valtimo.camunda.domain.CamundaExecution
import com.ritense.valtimo.camunda.domain.CamundaProcessDefinition
import com.ritense.valtimo.camunda.service.CamundaRepositoryService
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import org.camunda.bpm.engine.impl.persistence.entity.SuspensionState
import org.springframework.stereotype.Service
import java.util.UUID

@Service
@SkipComponentScan
class ProcessAuthorizationService(
    private val camundaRepositoryService: CamundaRepositoryService,
    private val authorizationService: AuthorizationService
) {

    fun checkAuthorization(processDefinitionKey: String) {
        val processDefinition = runWithoutAuthorization {
            camundaRepositoryService.findLatestProcessDefinition(
                processDefinitionKey
            )
        }
        require(processDefinition != null)
        authorizationService.requirePermission(
            EntityAuthorizationRequest(
                CamundaExecution::class.java,
                CamundaExecutionActionProvider.CREATE,
                createDummyCamundaExecution(
                    processDefinition,
                    "UNDEFINED_BUSINESS_KEY"
                )
            )
        )
    }

    private fun createDummyCamundaExecution(
        processDefinition: CamundaProcessDefinition,
        businessKey: String
    ): CamundaExecution {
        val execution = CamundaExecution(
            id = UUID.randomUUID().toString(),
            revision = 1,
            rootProcessInstance = null,
            processInstance = null,
            businessKey = businessKey,
            parent = null,
            processDefinition = processDefinition,
            superExecution = null,
            superCaseExecutionId = null,
            caseInstanceId = null,
            activityId = null,
            activityInstanceId = null,
            active = true,
            concurrent = false,
            scope = false,
            eventScope = false,
            suspensionState = SuspensionState.ACTIVE.stateCode,
            cachedEntityState = 0,
            sequenceCounter = 0,
            tenantId = null,
            variableInstances = emptySet()
        );
        execution.processInstance = execution
        return execution
    }

}