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

package com.ritense.mail

import com.ritense.valtimo.contract.basictype.EmailAddress
import com.ritense.valtimo.contract.mail.model.RawMailMessage
import com.ritense.valtimo.contract.mail.model.TemplatedMailMessage
import com.ritense.valtimo.contract.mail.model.value.MailBody
import com.ritense.valtimo.contract.mail.model.value.MailTemplateIdentifier
import com.ritense.valtimo.contract.mail.model.value.Recipient
import com.ritense.valtimo.contract.mail.model.value.RecipientCollection
import com.ritense.valtimo.contract.mail.model.value.Sender
import com.ritense.valtimo.contract.mail.model.value.Subject
import org.mockito.MockitoAnnotations

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
            .sender(Sender.from(EmailAddress.from("sender@test.com")))
            .build()
    }

}