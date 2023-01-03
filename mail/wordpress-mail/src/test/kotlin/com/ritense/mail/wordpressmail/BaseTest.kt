/*
 * Copyright 2015-2022 Ritense BV, the Netherlands.
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

package com.ritense.mail.wordpressmail

import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import com.ritense.document.service.DocumentSequenceGeneratorService
import com.ritense.valtimo.contract.basictype.EmailAddress
import com.ritense.valtimo.contract.basictype.SimpleName
import com.ritense.valtimo.contract.mail.model.RawMailMessage
import com.ritense.valtimo.contract.mail.model.TemplatedMailMessage
import com.ritense.valtimo.contract.mail.model.value.MailBody
import com.ritense.valtimo.contract.mail.model.value.MailTemplateIdentifier
import com.ritense.valtimo.contract.mail.model.value.Recipient
import com.ritense.valtimo.contract.mail.model.value.RecipientCollection
import com.ritense.valtimo.contract.mail.model.value.Subject
import org.mockito.ArgumentCaptor
import org.mockito.Mock
import org.mockito.MockitoAnnotations

abstract class BaseTest {

    @Mock
    lateinit var documentSequenceGeneratorService: DocumentSequenceGeneratorService

    fun baseSetUp() {
        MockitoAnnotations.openMocks(this)
        whenever(documentSequenceGeneratorService.next(any())).thenReturn(1)
    }

    fun rawMailMessage(recipient: Recipient): RawMailMessage {
        val recipients = RecipientCollection.fromSingle(recipient)
        return RawMailMessage.with(recipients, MailBody.of(MailBody.MailBodyText.empty())).build()
    }

    fun templatedMailMessage(
        recipient: Recipient,
        subject: String,
        placeholders: Map<String, Any>
    ): TemplatedMailMessage {
        return TemplatedMailMessage.with(recipient, MailTemplateIdentifier.from("Template"))
            .subject(Subject.from(subject))
            .placeholders(placeholders)
            .build()
    }

    fun templatedMailMessage(
        recipientEmail: String,
        recipientName: String,
        template: String,
        subject: String,
        placeholderKey: String,
        placeholderValue: String
    ): TemplatedMailMessage {
        return TemplatedMailMessage
            .with(
                Recipient.to(EmailAddress.from(recipientEmail), SimpleName.from(recipientName)),
                MailTemplateIdentifier.from(template)
            )
            .subject(Subject.from(subject))
            .placeholders(mapOf(placeholderKey to placeholderValue))
            .build()
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> capture(captor: ArgumentCaptor<T>): T = captor.capture()

}