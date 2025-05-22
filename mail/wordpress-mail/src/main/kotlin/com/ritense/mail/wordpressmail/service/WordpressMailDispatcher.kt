/*
 * Copyright 2015-2024 Ritense BV, the Netherlands.
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

package com.ritense.mail.wordpressmail.service

import com.ritense.mail.MailDispatcher
import com.ritense.mail.event.MailSendEvent
import com.ritense.mail.wordpressmail.domain.EmailSendRequest
import com.ritense.mail.wordpressmail.domain.NamedByteArrayResource
import com.ritense.valtimo.contract.mail.model.MailMessageStatus
import com.ritense.valtimo.contract.mail.model.RawMailMessage
import com.ritense.valtimo.contract.mail.model.TemplatedMailMessage
import com.ritense.valtimo.contract.mail.model.value.AttachmentCollection
import org.springframework.context.ApplicationEventPublisher
import org.springframework.core.io.Resource

class WordpressMailDispatcher(
    private val wordpressMailClient: WordpressMailClient,
    private val applicationEventPublisher: ApplicationEventPublisher,
) : MailDispatcher {

    override fun send(rawMailMessage: RawMailMessage): List<MailMessageStatus> {
        throw NotImplementedError()
    }

    override fun send(templatedMailMessage: TemplatedMailMessage): List<MailMessageStatus> {
        val statusList = mutableListOf<MailMessageStatus>()
        val templateId = getTemplateIdByName(templatedMailMessage.templateIdentifier.get())
        val sendRequests = EmailSendRequest.from(templatedMailMessage)
        for (sendRequest in sendRequests) {
            val attachments = attachmentsToResources(templatedMailMessage.attachments)
            val response = wordpressMailClient.send(templateId, sendRequest, attachments)
            val mailStatus = response.toMailMessageStatus()
            statusList.add(mailStatus)

            if (response.success == true) {
                applicationEventPublisher.publishEvent(
                    MailSendEvent(
                        mailStatus.email,
                        templatedMailMessage.subject
                    )
                )
            }
        }
        return statusList
    }

    override fun getMaximumSizeAttachments(): Int {
        return MAX_SIZE_EMAIL_BODY_IN_BYTES
    }

    private fun getTemplateIdByName(templateName: String): String {
        val template = wordpressMailClient.getEmailTemplates().emails.firstOrNull { it.postTitle == templateName }
        if (template == null) {
            throw IllegalStateException("No e-mail template found with name: '$templateName'")
        } else {
            return template.id
        }
    }

    private fun attachmentsToResources(attachments: AttachmentCollection): List<Resource>? {
        return attachments.get()?.stream()
            ?.map { NamedByteArrayResource(it.name.get(), it.content.get()) }
            ?.toList()
    }

    companion object {
        const val MAX_SIZE_EMAIL_BODY_IN_BYTES: Int = 20000000  // 20mb. TODO: verify
    }

}
