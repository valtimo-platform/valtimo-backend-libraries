package com.inwonerplan.poc.aanbod.command

import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.ritense.commandhandling.CommandHandler
import com.ritense.document.domain.impl.request.ModifyDocumentRequest
import com.ritense.document.service.DocumentService
import com.ritense.formviewmodel.error.BusinessException
import mu.KotlinLogging

class CompleteIntakeGesprekCommandHandler(
    val documentService: DocumentService
) : CommandHandler<CompleteIntakeGesprekCommand, Unit> {

    override fun execute(command: CompleteIntakeGesprekCommand) {
        command.task.execution?.businessKey.let { caseId ->
            val document = documentService.get(caseId)

            val objectMapper = jacksonObjectMapper()
            val payload = objectMapper.valueToTree(command.intakeGesprekSubmission) as ObjectNode
            val modifyRequest = ModifyDocumentRequest.create(document, payload)

            val result = documentService.modifyDocument(modifyRequest)
            if(result.errors().isNotEmpty()) {
                result.errors().forEach {
                    logger.error { "Failed updating document $it" }
                }
                throw BusinessException("Could not save document")
            } else {
                logger.info { "Updated case with $payload" }
            }
        }
    }

    companion object {
        val logger = KotlinLogging.logger {  }
    }

}