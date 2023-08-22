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

package com.ritense.externalevent.messaging.builder

import com.fasterxml.jackson.core.JsonPointer
import com.ritense.document.domain.impl.JsonSchemaDocument
import com.ritense.document.domain.impl.JsonSchemaDocumentId
import com.ritense.document.service.impl.JsonSchemaDocumentService
import com.ritense.externalevent.service.ExternalTaskService
import com.ritense.valtimo.contract.basictype.EmailAddress
import com.ritense.valtimo.contract.basictype.SimpleName
import com.ritense.valtimo.contract.config.ValtimoProperties
import com.ritense.valtimo.contract.mail.MailSender
import com.ritense.valtimo.contract.mail.model.RawMailMessage
import com.ritense.valtimo.contract.mail.model.TemplatedMailMessage
import com.ritense.valtimo.contract.mail.model.value.AttachmentCollection
import com.ritense.valtimo.contract.mail.model.value.MailBody
import com.ritense.valtimo.contract.mail.model.value.MailTemplateIdentifier
import com.ritense.valtimo.contract.mail.model.value.Recipient
import com.ritense.valtimo.contract.mail.model.value.Sender
import com.ritense.valtimo.contract.mail.model.value.Subject
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.DelegateTask
import java.util.UUID

/* Fluent API helper for sending a public/non-public task to the portal via email */
data class TaskMessageSender(
    val externalTaskService: ExternalTaskService,
    val documentService: JsonSchemaDocumentService,
    val valtimoProperties: ValtimoProperties,
    val mailSender: MailSender,
    var delegateTask: DelegateTask?,
    var mailTemplate: String? = null,
    var formDefinitionName: String? = null,
    var isPublicTask: Boolean = false,
    var mailTo: String? = null,
    var firstName: String? = null,
    var lastName: String? = null,
    var languageKey: String = "nl",
    var document: JsonSchemaDocument? = null,
    var tenantId: String? = null
) {

    fun tenantId(tenantId: String): TaskMessageSender {
        this.tenantId = tenantId
        return this
    }

    fun task(delegateTask: DelegateTask): TaskMessageSender {
        this.delegateTask = delegateTask
        val jsonSchemaDocumentId = JsonSchemaDocumentId.existingId(UUID.fromString(delegateTask.execution!!.processBusinessKey!!))
        document = documentService.getDocumentBy(jsonSchemaDocumentId, tenantId)
        return this
    }

    fun form(formDefinitionName: String): TaskMessageSender {
        this.formDefinitionName = formDefinitionName
        return this
    }

    fun mailTemplate(mailTemplate: String): TaskMessageSender {
        this.mailTemplate = mailTemplate
        return this
    }

    fun mailTo(mailTo: String): TaskMessageSender {
        this.mailTo = document!!.content().getValueBy(JsonPointer.valueOf(mailTo)).orElseThrow().textValue()
        return this
    }

    fun firstName(firstName: String): TaskMessageSender {
        this.firstName = document!!.content().getValueBy(JsonPointer.valueOf(firstName)).orElseThrow().textValue()
        return this
    }

    fun lastName(lastName: String): TaskMessageSender {
        this.lastName = document!!.content().getValueBy(JsonPointer.valueOf(lastName)).orElseThrow().textValue()
        return this
    }

    fun public(): TaskMessageSender {
        this.isPublicTask = true
        return this
    }

    fun languageKey(languageKey: String): TaskMessageSender {
        this.languageKey = languageKey
        return this
    }

    fun sendEmail(): TaskMessageSender {
        if (this.mailTemplate == null) {
            mailSender.send(createRawMailMessage())
        } else {
            mailSender.send(createTemplatedMailMessage())
        }
        return this
    }

    fun publishTask() {
        externalTaskService.publishPortalTask(
            formDefinitionName!!,
            document!!,
            delegateTask!!,
            isPublicTask
        )
    }

    private fun baseUrl(): String {
        return valtimoProperties.portal.baselUrl
    }

    private fun createTemplatedMailMessage(): TemplatedMailMessage {
        return TemplatedMailMessage.with(
            Recipient.to(
                EmailAddress.from(mailTo),
                SimpleName.from("$firstName $lastName")
            ),
            MailTemplateIdentifier.from(mailTemplate).withLanguageKey(languageKey)
        )
            .placeholders(placeholderVariables())
            .attachments(AttachmentCollection.empty())
            .build()
    }

    private fun createRawMailMessage(): RawMailMessage {
        val taskLink = taskLinkFromIdAndUrl(delegateTask!!.id, baseUrl())
        val rawMailMessage = RawMailMessage.with(
            Recipient.to(
                EmailAddress.from(mailTo),
                SimpleName.from(mailTo)),
            MailBody.of(
                MailBody.MailBodyText.of("Go to $taskLink to see your task")
            )
        )
            .sender(
                Sender.from(
                    EmailAddress.from("no-reply@valtimo.nl"),
                    SimpleName.from("Valtimo")
                )
            )
            .subject(Subject.from("Your task is waiting"))
            .build()
        return rawMailMessage
    }

    private fun placeholderVariables(): Map<String, Any?> {
        return mapOf(
            "taskname" to delegateTask!!.name,
            "var" to delegateTask!!.variables,
            "baseUrl" to baseUrl(),
            "link" to taskLinkFromIdAndUrl(delegateTask!!.id, baseUrl()),
            "firstname" to firstName,
            "lastname" to lastName,
            "email" to mailTo,
            "execution" to placeholderExecutionVariables(delegateTask!!.execution!!)
        )
    }

    private fun placeholderExecutionVariables(execution: DelegateExecution): Map<String, Any> {
        val executionVariables = execution.variables
        executionVariables["business-key"] = execution.processBusinessKey
        return executionVariables
    }

    private fun taskLinkFromIdAndUrl(taskId: String, baseUrl: String): String {
        if (!isPublicTask) {
            if (languageKey == "nl") {
                return String.format("%snl/taak?id=%s", baseUrl, taskId)
            } else if (languageKey == "en") {
                return String.format("%sen/task?id=%s", baseUrl, taskId)
            } else {
                throw IllegalStateException("Invalid language chosen")
            }
        } else {
            if (languageKey == "nl") {
                return String.format("%snl/publieke-taak?id=%s", baseUrl, taskId)
            } else if (languageKey == "en") {
                return String.format("%sen/public-task?id=%s", baseUrl, taskId)
            } else {
                throw IllegalStateException("Invalid language chosen")
            }
        }
    }
}