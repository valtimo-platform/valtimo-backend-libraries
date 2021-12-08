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


import com.ritense.mail.flowmailer.BaseTest
import com.ritense.valtimo.contract.basictype.EmailAddress
import com.ritense.valtimo.contract.basictype.SimpleName
import com.ritense.valtimo.contract.mail.model.value.Attachment
import com.ritense.valtimo.contract.mail.model.value.Recipient
import com.ritense.valtimo.contract.mail.model.value.attachment.Content
import com.ritense.valtimo.contract.mail.model.value.attachment.Name
import com.ritense.valtimo.contract.mail.model.value.attachment.Type
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class SubmitMessageTest : BaseTest() {

    @Test
    fun `should make instance of SubmitMessage`() {
        val templatedMailMessage = templatedMailMessage(
            Recipient.to(
                EmailAddress.from("test@test.com"),
                SimpleName.from("testman")
            ))
        val submitMessages = SubmitMessage.from(templatedMailMessage)

        assertThat(submitMessages[0].flowSelector).isEqualTo(templatedMailMessage.templateIdentifier.get())
        assertThat(submitMessages[0].headerFromAddress).isEqualTo(templatedMailMessage.sender.email.get())
        assertThat(submitMessages[0].headerFromName).isEqualTo(templatedMailMessage.sender.name.get())
        assertThat(submitMessages[0].headerToAddress).isEqualTo(templatedMailMessage.recipients.get().first().email.get())
        assertThat(submitMessages[0].headerToName).isEqualTo(templatedMailMessage.recipients.get().first().name.get())
        assertThat(submitMessages[0].messageType).isEqualTo(SubmitMessage.MessageType.EMAIL)
        assertThat(submitMessages[0].recipientAddress).isEqualTo(templatedMailMessage.recipients.get().first().email.get())
        assertThat(submitMessages[0].senderAddress).isEqualTo(templatedMailMessage.sender.email.get())
        assertThat(submitMessages[0].subject).isEqualTo(templatedMailMessage.subject.get())
    }

    @Test
    fun `should create SubmitMessage with attachments`() {
        val attachment = Attachment.from(
            Name.from("my-attachment"),
            Type.from("aType"),
            Content.from(ByteArray(100))
        )
        val templatedMailMessage = templatedMailMessage(
            Recipient.to(
                EmailAddress.from("test@test.com"),
                SimpleName.from("testman")
            ),
            attachment
        )

        val submitMessages = SubmitMessage.from(templatedMailMessage)

        assertThat(submitMessages[0].attachments[0].filename).isEqualTo(attachment.name.get())
        assertThat(submitMessages[0].attachments[0].contentType).isEqualTo(attachment.type.get())
        assertThat(submitMessages[0].attachments[0].content).isNotNull
    }
}