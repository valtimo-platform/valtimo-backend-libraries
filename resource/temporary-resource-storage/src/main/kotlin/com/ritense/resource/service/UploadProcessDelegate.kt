package com.ritense.resource.service

import com.ritense.resource.event.ResourceStorageMetadataAvailableEvent
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.springframework.context.ApplicationEventPublisher

class UploadProcessDelegate(
    private val eventPublisher: ApplicationEventPublisher
) {

    fun publishFileUploadedEvent(execution: DelegateExecution) {
        val event = ResourceStorageMetadataAvailableEvent(
            this,
            resourceId = execution.getVariable("resourceId") as? String ?: "",
            documentId = execution.getVariable("documentId") as? String ?: "",
            documentUrl = execution.getVariable("documentUrl") as String,
            downloadUrl = execution.getVariable("downloadUrl") as String
        )
        eventPublisher.publishEvent(event)
    }
}