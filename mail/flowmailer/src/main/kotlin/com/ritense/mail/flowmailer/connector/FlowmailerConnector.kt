/*
 * Copyright 2015-2023 Ritense BV, the Netherlands.
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
import com.ritense.resource.service.ResourceService
import com.ritense.valtimo.contract.basictype.EmailAddress
import com.ritense.valtimo.contract.basictype.SimpleName
import com.ritense.valtimo.contract.mail.model.MailMessageStatus
import com.ritense.valtimo.contract.mail.model.TemplatedMailMessage
import com.ritense.valtimo.contract.mail.model.value.Attachment
import com.ritense.valtimo.contract.mail.model.value.AttachmentCollection
import com.ritense.valtimo.contract.mail.model.value.MailTemplateIdentifier
import com.ritense.valtimo.contract.mail.model.value.Recipient
import com.ritense.valtimo.contract.mail.model.value.RecipientCollection
import com.ritense.valtimo.contract.mail.model.value.Sender
import com.ritense.valtimo.contract.mail.model.value.Subject
import com.ritense.valtimo.contract.mail.model.value.attachment.Content
import com.ritense.valtimo.contract.mail.model.value.attachment.Name
import com.ritense.valtimo.contract.mail.model.value.attachment.Type
import org.camunda.bpm.engine.delegate.DelegateExecution
import java.util.UUID

/**
 * Flowmailer connector:
 *
 * Example usage within BPMN expression
 * ${connectorFluentBuilder
 *  .builder()
 *  .withConnector("flowmailerInstance")
 *  .sender("info@ritense.com")
 *  .templateIdentifier("flow-selector")
 *  .recipients(execution, "/", "emailadres")
 *  .attachments(execution, "resourceIds") < Optional
 *  .placeholders(execution.variables)
 *  .sendEmail()
 * }
 */
@ConnectorType(name = "Flowmailer")
class FlowmailerConnector(
    private var flowmailerConnectorProperties: FlowmailerConnectorProperties,
    private val mailDispatcher: MailDispatcher,
    private val documentService: DocumentService,
    private val resourceService: ResourceService
) : Connector {

    private var sender: Sender? = null
    private var templateIdentifier: MailTemplateIdentifier? = null
    private var subject: Subject? = null
    private var recipients: RecipientCollection? = null
    private var placeholders: Map<String, Any> = mutableMapOf()
    private var attachments: AttachmentCollection? = null

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
        delegateExecution: DelegateExecution,
        pathToRecipientCollection: String,
        itemEmailProperty: String
    ): FlowmailerConnector {
        val document = documentService
            .findBy(JsonSchemaDocumentId.existingId(UUID.fromString(delegateExecution.businessKey)))
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

    /**
     * Adds a pair(key, value) to the placeholder map.
     *
     * @param delegateExecution the <code>delegateExecution</code>
     * @param key the <code>String</code> which specifies the property to add
     * @param pathToValue this <code>String</code> resolves to a jsonPointer in a document to get a value
     */
    fun placeholder(
        delegateExecution: DelegateExecution,
        key: String,
        pathToValue: String
    ): FlowmailerConnector {
        val document = documentService.findBy(
            JsonSchemaDocumentId.existingId(UUID.fromString(delegateExecution.businessKey))
        ).orElseThrow()
        val value = document.content().getValueBy(JsonPointer.valueOf(pathToValue)).orElseThrow().asText()
        this.placeholders + Pair(key, value)
        return this
    }

    /**
     * Adds a pair(key, value) to the placeholder map.
     *
     * @param key the <code>String</code> which specifies the property to add
     * @param pathToValue this <code>String</code> value
     */
    fun placeholder(
        key: String,
        value: Any
    ): FlowmailerConnector {
        this.placeholders += Pair(key, value)
        return this
    }

    /**
     * Adds a pair(key, value) to the placeholder map from process variables map.
     *
     * @param delegateExecution the <code>delegateExecution</code>
     * @param key the <code>String</code> which specifies the property to add
     */
    fun placeholder(
        delegateExecution: DelegateExecution,
        key: String
    ): FlowmailerConnector {
        this.placeholders + Pair(key, delegateExecution.getVariable(key))
        return this
    }

    /**
     * Assumes resource id's are available in the process variables under a certain key.
     *
     * @param delegateExecution the <code>delegateExecution</code> which contains process vars
     * @param nameOfCollection the <code>String</code> which specifies the property containing list of ids
     */
    fun attachments(
        delegateExecution: DelegateExecution,
        nameOfCollection: String
    ): FlowmailerConnector {
        val resourceIds = delegateExecution.getVariable(nameOfCollection) as List<*>
        val attachments: Collection<Attachment> = mutableListOf()
        resourceIds.forEach {
            val resourceContent = resourceService.getResourceContent(UUID.fromString(it as String))
            Attachment.from(
                Name.from(resourceContent.resource.name),
                Type.from(resourceContent.resource.extension),
                Content.from(resourceContent.content)
            )
        }
        this.attachments = AttachmentCollection.from(attachments)
        return this
    }

    fun sendEmail(): List<MailMessageStatus> {
        val messageBuilder = TemplatedMailMessage.with(recipients!!, templateIdentifier!!)
            .subject(subject!!)
            .sender(sender!!)
            .placeholders(placeholders)

        attachments?.let {
            messageBuilder.attachments(attachments)
        }
        return mailDispatcher.send(messageBuilder.build())
    }

    override fun getProperties(): ConnectorProperties {
        return flowmailerConnectorProperties
    }

    override fun setProperties(connectorProperties: ConnectorProperties) {
        flowmailerConnectorProperties = connectorProperties as FlowmailerConnectorProperties
    }

}