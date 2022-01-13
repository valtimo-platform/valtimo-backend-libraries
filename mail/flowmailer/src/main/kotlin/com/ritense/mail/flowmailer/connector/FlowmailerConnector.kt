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
package com.ritense.mail.flowmailer.connector

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
import com.ritense.valtimo.contract.mail.model.value.MailTemplateIdentifier
import com.ritense.valtimo.contract.mail.model.value.Recipient
import com.ritense.valtimo.contract.mail.model.value.RecipientCollection
import com.ritense.valtimo.contract.mail.model.value.Sender
import com.ritense.valtimo.contract.mail.model.value.Subject
import java.util.UUID

@ConnectorType(name = "Flowmailer")
class FlowmailerConnector(
    private var flowmailerConnectorProperties: FlowmailerConnectorProperties,
    private val mailDispatcher: MailDispatcher,
    private val documentService: DocumentService
) : Connector {

    private var sender: Sender? = null
    private var templateIdentifier: MailTemplateIdentifier? = null
    private var subject: Subject? = null
    private var recipients: RecipientCollection? = null
    private var placeholders: Map<String, Any> = mutableMapOf()

    fun sender(sender: String): FlowmailerConnector {
        this.sender = Sender.from(EmailAddress.from(sender))
        return this
    }

    fun templateIdentifier(templateIdentifier: String): FlowmailerConnector {
        this.templateIdentifier = MailTemplateIdentifier.from(templateIdentifier)
        return this
    }

    fun subject(subject: String): FlowmailerConnector {
        this.subject = Subject.from(subject)
        return this
    }

    fun recipients(
        pathToRecipientCollection: String,
        itemEmailProperty: String,
        documentId: String
    ): FlowmailerConnector {
        val document = documentService
            .findBy(JsonSchemaDocumentId.existingId(UUID.fromString(documentId)))
            .orElseThrow()

        val recipientsArrayNode = document
            .content()
            .getValueBy(JsonPointer.valueOf(pathToRecipientCollection))
            .orElseThrow() as ArrayNode

        val recipients = recipientsArrayNode.map {
            Recipient.to(
                EmailAddress.from(it.get(itemEmailProperty).asText()),
                SimpleName.none()
            )
        }
        this.recipients = RecipientCollection.from(recipients)
        return this
    }

    fun placeholders(placeholders: Map<String, Any>): FlowmailerConnector {
        this.placeholders = placeholders
        return this
    }

    fun sendEmail(): List<MailMessageStatus> {
        val templatedMailMessage = TemplatedMailMessage.with(recipients, templateIdentifier)
            .subject(subject)
            .sender(sender)
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