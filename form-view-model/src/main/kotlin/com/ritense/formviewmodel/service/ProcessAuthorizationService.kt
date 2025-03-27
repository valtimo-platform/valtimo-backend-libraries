package com.ritense.formviewmodel.service

import com.ritense.authorization.AuthorizationContext.Companion.runWithoutAuthorization
import com.ritense.authorization.AuthorizationService
import com.ritense.authorization.request.AuthorizationResourceContext
import com.ritense.authorization.request.RelatedEntityAuthorizationRequest
import com.ritense.document.domain.impl.JsonSchemaDocument
import com.ritense.valtimo.camunda.authorization.CamundaExecutionActionProvider
import com.ritense.valtimo.camunda.domain.CamundaExecution
import com.ritense.valtimo.camunda.domain.CamundaProcessDefinition
import com.ritense.valtimo.camunda.service.CamundaRepositoryService
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import org.springframework.stereotype.Service

@Service
@SkipComponentScan
class ProcessAuthorizationService(
    private val camundaRepositoryService: CamundaRepositoryService,
    private val authorizationService: AuthorizationService
) {

    fun checkStartProcessAuthorization(
        processDefinitionKey: String,
        document: JsonSchemaDocument? = null,
    ) {
        val processDefinition = runWithoutAuthorization {
            camundaRepositoryService.findLatestProcessDefinition(
                processDefinitionKey
            )
        }
        require(processDefinition != null)

        authorizationService.requirePermission(
            RelatedEntityAuthorizationRequest(
                CamundaExecution::class.java,
                CamundaExecutionActionProvider.CREATE,
                CamundaProcessDefinition::class.java,
                processDefinition.id
            ).apply {
                if (document != null) {
                    withContext(
                        AuthorizationResourceContext(
                            JsonSchemaDocument::class.java,
                            document
                        )
                    )
                }
            }
        )
    }

}