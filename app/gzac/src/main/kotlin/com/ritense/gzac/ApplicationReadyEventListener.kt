package com.ritense.gzac

import com.ritense.document.domain.event.DocumentDefinitionDeployedEvent
import com.ritense.processdocument.domain.impl.request.DocumentDefinitionProcessRequest
import com.ritense.processdocument.service.DocumentDefinitionProcessLinkService
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

//TODO: Should be temporary. sets link between case definition and upload process. Can't be imported yet.
@Component
class ApplicationReadyEventListener(
    private val documentDefinitionProcessLinkService: DocumentDefinitionProcessLinkService,
) {
    @EventListener(DocumentDefinitionDeployedEvent::class)
    fun handleDocumentDefinitionDeployed(event: DocumentDefinitionDeployedEvent) {
        setUploadProcess(event)
    }

    private fun setUploadProcess(event: DocumentDefinitionDeployedEvent) {
        documentDefinitionProcessLinkService.saveDocumentDefinitionProcess(
            CASE_DEFINITION_NAME,
            DocumentDefinitionProcessRequest("document-upload", "DOCUMENT_UPLOAD")
        )
    }

    companion object {
        private const val CASE_DEFINITION_NAME = "bezwaar"
    }
}
