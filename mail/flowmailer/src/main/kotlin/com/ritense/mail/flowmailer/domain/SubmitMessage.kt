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

package com.ritense.mail.flowmailer.domain

import com.ritense.valtimo.contract.mail.model.TemplatedMailMessage
import org.apache.tika.Tika
import java.util.Date

data class SubmitMessage(
    val attachments: MutableList<Attachment> = mutableListOf(),
    val data: MutableMap<String, Any>,
    val flowSelector: String, //link to flow with template
    val headerFromAddress: String,
    val headerFromName: String,
    val headerToAddress: String,
    val headerToName: String,
    val headers: List<Header>? = null,
    val html: String? = null,
    val messageType: MessageType = MessageType.EMAIL,
    val recipientAddress: String,
    val scheduleAt: Date? = null,
    val senderAddress: String,
    val subject: String,
    val tags: List<String>? = null,
    val text: String? = null
) {

    data class Attachment(
        val content: ByteArray,
        val contentType: String, //MimeType
        val disposition: Disposition = Disposition.attachment,
        val filename: String
    )

    enum class Disposition {
        attachment, inline, related
    }

    enum class MessageType {
        EMAIL, SMS, LETTER
    }

    data class Header(
        val name: String,
        val value: String
    )

    companion object {
        fun from(templatedMailMessage: TemplatedMailMessage): List<SubmitMessage> {
            val messageList = mutableListOf<SubmitMessage>()
            templatedMailMessage.recipients.get().forEach {
                val submitMessage = SubmitMessage(
                    flowSelector = templatedMailMessage.templateIdentifier.get(),
                    headerFromAddress = templatedMailMessage.sender.email.get(),
                    headerFromName = templatedMailMessage.sender.name.get().orEmpty(),
                    headerToAddress = it.email.get(),
                    headerToName = it.name.get().orEmpty(),
                    recipientAddress = it.email.get(),
                    senderAddress = templatedMailMessage.sender.email.get(),
                    subject = templatedMailMessage.subject.get(),
                    data = templatedMailMessage.placeholders
                )

                if (templatedMailMessage.attachments.isPresent) {
                    templatedMailMessage.attachments.get().forEach { attachment ->
                        submitMessage.attachments.add(
                            Attachment(
                                content = attachment.content.get(),
                                contentType = Tika().detect(attachment.content.get()),
                                filename = attachment.name.get()
                            )
                        )
                    }
                }
                messageList.add(submitMessage)
            }
            return messageList
        }
    }
}
