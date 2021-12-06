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

package com.ritense.mail.flowmailer

import com.ritense.valtimo.contract.basictype.EmailAddress
import com.ritense.valtimo.contract.basictype.SimpleName
import com.ritense.valtimo.contract.mail.model.RawMailMessage
import com.ritense.valtimo.contract.mail.model.TemplatedMailMessage
import com.ritense.valtimo.contract.mail.model.value.Attachment
import com.ritense.valtimo.contract.mail.model.value.MailBody
import com.ritense.valtimo.contract.mail.model.value.MailTemplateIdentifier
import com.ritense.valtimo.contract.mail.model.value.Recipient
import com.ritense.valtimo.contract.mail.model.value.RecipientCollection
import com.ritense.valtimo.contract.mail.model.value.Sender
import com.ritense.valtimo.contract.mail.model.value.Subject
import org.mockito.MockitoAnnotations
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType

abstract class BaseTest {

    fun baseSetUp() {
        MockitoAnnotations.openMocks(this)
    }

    fun rawMailMessage(recipient: Recipient): RawMailMessage {
        val recipients = RecipientCollection.fromSingle(recipient)
        return RawMailMessage.with(recipients, MailBody.of(MailBody.MailBodyText.empty())).build()
    }

    fun templatedMailMessage(recipient: Recipient): TemplatedMailMessage {
        return TemplatedMailMessage.with(recipient, MailTemplateIdentifier.from("Template"))
            .subject(Subject.from("Subject"))
            .sender(Sender.from(EmailAddress.from("sender@test.com"), SimpleName.from("Piet")))
            .build()
    }

    fun templatedMailMessage(recipient: Recipient, attachment: Attachment): TemplatedMailMessage {
        return TemplatedMailMessage.with(recipient, MailTemplateIdentifier.from("Template"))
            .subject(Subject.from("Subject"))
            .sender(Sender.from(EmailAddress.from("sender@test.com"), SimpleName.from("Piet")))
            .attachment(attachment)
            .build()
    }

    fun getHttpHeaders(): HttpHeaders {
        val httpHeaders = HttpHeaders()
        httpHeaders["Authorization"] = "Bearer " + "token"
        httpHeaders.contentType = MediaType.valueOf("application/vnd.flowmailer.v1.12+json;charset=UTF-8")
        httpHeaders.accept = listOf(MediaType.valueOf("application/vnd.flowmailer.v1.12+json;charset=UTF-8"))
        return httpHeaders
    }
}