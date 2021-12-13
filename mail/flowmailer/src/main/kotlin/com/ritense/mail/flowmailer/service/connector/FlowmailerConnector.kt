/*
 * Copyright 2015-2020 Ritense BV, the Netherlands.
 *
 * Licensed under EUPL, Version 1.2 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ritense.mail.flowmailer.service.connector

import com.fasterxml.jackson.core.JsonPointer
import com.fasterxml.jackson.databind.node.ArrayNode
import com.ritense.connector.domain.Connector
import com.ritense.connector.domain.ConnectorProperties
import com.ritense.connector.domain.meta.ConnectorType
import com.ritense.document.domain.impl.JsonSchemaDocumentId
import com.ritense.document.service.DocumentService
import com.ritense.mail.MailDispatcher
import com.ritense.valtimo.contract.basictype.EmailAddress
import com.ritense.valtimo.contract.basictype.SimpleName
import com.ritense.valtimo.contract.mail.model.MailMessageStatus
import com.ritense.valtimo.contract.mail.model.TemplatedMailMessage
import com.ritense.valtimo.contract.mail.model.value.Attachment
import com.ritense.valtimo.contract.mail.model.value.MailTemplateIdentifier
import com.ritense.valtimo.contract.mail.model.value.RecipientCollection
import com.ritense.valtimo.contract.mail.model.value.Sender
import com.ritense.valtimo.contract.mail.model.value.Subject
import java.util.UUID

@ConnectorType(name = "Flowmailer")
class FlowmailerConnector(
    private var flowmailerConnectorProperties: FlowmailerConnectorProperties,
    private val mailDispatcher: MailDispatcher,
    private var documentService: DocumentService
) : Connector {

    var sender: String = ""
    var templateIdentifier: String = ""
    var subject: String = ""
    var recipients = mutableListOf<String>()
    lateinit var placeholders: Map<String, Any>
    lateinit var attachments: Attachment

    fun setSender(sender: String): FlowmailerConnector {
        this.sender = sender
        return this
    }

    fun setTemplateIdentifier(templateIdentifier: String): FlowmailerConnector {
        this.templateIdentifier = templateIdentifier
        return this
    }

    fun setSubject(subject: String): FlowmailerConnector {
        this.subject = subject
        return this
    }

    //TODO: make this method set multiple recipients
//    fun setRecipients(pathToItem: String, itemEmailProperty: String, documentId: String): FlowmailerConnector {
//        val document =
//            documentService.findBy(JsonSchemaDocumentId.existingId(UUID.fromString(documentId))).orElseThrow()
//        val value = document
//            .content()
//            .getValueBy(JsonPointer.valueOf(pathToItem))
//            .stream()
//            .flatMap {
//                it as ArrayNode
//            return it.forEach {
//            }
//
////        it.forEach { recipient ->
////
////            recipient.get(itemEmailProperty).toString()
////        }
//
//        this.recipients.add(value)
//        return this
//    }

    fun setPlaceholders(placeholders: Map<String, Any>): FlowmailerConnector {
        this.placeholders = placeholders
        return this
    }

    fun setAttachments(attachments: Attachment): FlowmailerConnector {
        this.attachments = attachments
        return this
    }

    fun sendEmail(): List<MailMessageStatus> {
        val sender = sender
        val flowSelector = templateIdentifier
        val subject = subject
        val recipients = recipients
        val placeholders = placeholders
        val attachments = attachments


        val templatedMailMessage = TemplatedMailMessage.with(recipients, MailTemplateIdentifier.from(flowSelector))
            .subject(Subject.from(subject))
            .sender(Sender.from(EmailAddress.from(sender)))
            .attachment(attachments)
            .placeholders(placeholders)
            .build()

        return mailDispatcher.send(templatedMailMessage)
    }

    override fun getProperties(): ConnectorProperties {
        return flowmailerConnectorProperties
    }

    override fun setProperties(connectorProperties: ConnectorProperties) {
        flowmailerConnectorProperties = connectorProperties as FlowmailerConnectorProperties
    }
}