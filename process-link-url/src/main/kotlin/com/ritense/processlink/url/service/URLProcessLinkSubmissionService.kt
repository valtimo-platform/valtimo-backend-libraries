package com.ritense.processlink.url.service

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.authorization.AuthorizationContext
import com.ritense.authorization.AuthorizationService
import com.ritense.document.domain.Document
import com.ritense.document.domain.impl.request.ModifyDocumentRequest
import com.ritense.document.domain.impl.request.NewDocumentRequest
import com.ritense.document.service.DocumentService
import com.ritense.document.service.impl.JsonSchemaDocumentService
import com.ritense.processdocument.domain.ProcessDocumentDefinition
import com.ritense.processdocument.domain.impl.CamundaProcessDefinitionKey
import com.ritense.processdocument.domain.impl.request.ModifyDocumentAndCompleteTaskRequest
import com.ritense.processdocument.domain.impl.request.ModifyDocumentAndStartProcessRequest
import com.ritense.processdocument.domain.impl.request.NewDocumentAndStartProcessRequest
import com.ritense.processdocument.domain.request.Request
import com.ritense.processdocument.service.ProcessDocumentAssociationService
import com.ritense.processdocument.service.ProcessDocumentService
import com.ritense.processlink.domain.ActivityTypeWithEventName
import com.ritense.processlink.domain.ProcessLink
import com.ritense.processlink.service.ProcessLinkService
import com.ritense.processlink.url.domain.URLProcessLink
import com.ritense.processlink.url.web.rest.dto.URLSubmissionResult
import com.ritense.valtimo.camunda.domain.CamundaProcessDefinition
import com.ritense.valtimo.camunda.service.CamundaRepositoryService
import com.ritense.valtimo.contract.event.ExternalDataSubmittedEvent
import com.ritense.valtimo.service.CamundaTaskService
import org.springframework.context.ApplicationEventPublisher
import java.util.UUID

class URLProcessLinkSubmissionService(
    private val processLinkService: ProcessLinkService,
    private val documentService: JsonSchemaDocumentService,
    private val processDocumentAssociationService: ProcessDocumentAssociationService,
    private val processDocumentService: ProcessDocumentService,
    private val camundaTaskService: CamundaTaskService,
    private val repositoryService: CamundaRepositoryService,
    private val applicationEventPublisher: ApplicationEventPublisher,
    private val objectMapper: ObjectMapper,
) {

    fun submit(
        processLinkId: UUID,
        documentDefinitionName: String?,
        documentId: String?,
        taskInstanceId: String?,
    ): URLSubmissionResult {
        val processLink = processLinkService.getProcessLink(processLinkId, URLProcessLink::class.java)
        val document = documentId
            ?.let { AuthorizationContext.runWithoutAuthorization { documentService.get(documentId) } }
        val processDefinition = getProcessDefinition(processLink)
        val documentDefinitionNameToUse = document?.definitionId()?.name()
            ?: documentDefinitionName
            ?: getProcessDocumentDefinition(processDefinition, document).processDocumentDefinitionId()
                .documentDefinitionId().name()

        val request = getRequest(
            processLink,
            document,
            taskInstanceId,
            documentDefinitionNameToUse,
            processDefinition.key
        )

        return dispatchRequest(
            request,
            documentDefinitionNameToUse,
        )
    }

    private fun getProcessDefinition(
        processLink: ProcessLink
    ): CamundaProcessDefinition {
        return AuthorizationContext.runWithoutAuthorization {
            repositoryService.findProcessDefinitionById(processLink.processDefinitionId)!!
        }
    }

    private fun getProcessDocumentDefinition(
        processDefinition: CamundaProcessDefinition,
        document: Document?
    ): ProcessDocumentDefinition {
        val processDefinitionKey = CamundaProcessDefinitionKey(processDefinition.key)
        return AuthorizationContext.runWithoutAuthorization {
            if (document == null) {
                processDocumentAssociationService.getProcessDocumentDefinition(processDefinitionKey)
            } else {
                processDocumentAssociationService.getProcessDocumentDefinition(
                    processDefinitionKey,
                    document.definitionId().version()
                )
            }
        }
    }

    private fun getRequest(
        processLink: URLProcessLink,
        document: Document?,
        taskInstanceId: String?,
        documentDefinitionName: String,
        processDefinitionKey: String,
    ): Request {
        return if (processLink.activityType == ActivityTypeWithEventName.START_EVENT_START) {
            if (document == null) {
                newDocumentAndStartProcessRequest(
                    documentDefinitionName,
                    processDefinitionKey
                )
            } else {
                modifyDocumentAndStartProcessRequest(
                    document,
                    processDefinitionKey
                )
            }
        } else if (processLink.activityType == ActivityTypeWithEventName.USER_TASK_CREATE) {
            modifyDocumentAndCompleteTaskRequest(
                document!!,
                taskInstanceId!!
            )
        } else {
            throw UnsupportedOperationException("Cannot handle submission for activity-type '" + processLink.activityType + "'")
        }
    }

    private fun newDocumentAndStartProcessRequest(
        documentDefinitionName: String,
        processDefinitionKey: String,
    ): NewDocumentAndStartProcessRequest {
        return NewDocumentAndStartProcessRequest(
            processDefinitionKey,
            NewDocumentRequest(
                documentDefinitionName,
                objectMapper.createObjectNode()
            )
        )
    }

    private fun modifyDocumentAndStartProcessRequest(
        document: Document,
        processDefinitionKey: String,
    ): ModifyDocumentAndStartProcessRequest {
        return ModifyDocumentAndStartProcessRequest(
            processDefinitionKey,
            ModifyDocumentRequest(
                document.id().toString(),
                objectMapper.createObjectNode()
            )
        )
    }

    private fun modifyDocumentAndCompleteTaskRequest(
        document: Document,
        taskInstanceId: String,
    ): ModifyDocumentAndCompleteTaskRequest {
        return ModifyDocumentAndCompleteTaskRequest(
            ModifyDocumentRequest(
                document.id().toString(),
                objectMapper.createObjectNode()
            ),
            taskInstanceId
        )
    }

    private fun dispatchRequest(
        request: Request,
        documentDefinitionName: String
    ) : URLSubmissionResult {
//        return try {
        val result = processDocumentService.dispatch(request)
        if (result.errors().isNotEmpty()) {
            return URLSubmissionResult(result.errors().map { it.asString() }, "")
        } else {
            val submittedDocument = result.resultingDocument().orElseThrow()
//                formFields.forEach { it.postProcess(submittedDocument) }
//                publishExternalDataSubmittedEvent(externalFormData, documentDefinitionName, submittedDocument)
//                valueResolverService.handleValues(submittedDocument.id.id, remainingValueResolverValues)
            return URLSubmissionResult(emptyList(), submittedDocument.id().toString())
//            }
//        } catch (ex: RuntimeException) {
//            val referenceId = UUID.randomUUID()
//            logger.error("Unexpected error occurred - $referenceId", ex)
//            FormSubmissionResultFailed(
//                OperationError.FromString("Unexpected error occurred, please contact support - referenceId: $referenceId")
//            )
//        }
        }
    }

}