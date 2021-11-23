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

package com.ritense.mail.service

import com.ritense.mail.MailDispatcher
import com.ritense.valtimo.contract.basictype.EmailAddress
import com.ritense.valtimo.contract.basictype.SimpleName
import com.ritense.valtimo.contract.mail.model.TemplatedMailMessage
import com.ritense.valtimo.contract.mail.model.value.MailTemplateIdentifier
import com.ritense.valtimo.contract.mail.model.value.Recipient
import com.ritense.valtimo.contract.mail.model.value.Sender
import com.ritense.valtimo.contract.mail.model.value.Subject
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

internal class FilteredMailSenderTest {

    lateinit var filteredMailSender: FilteredMailSender
    lateinit var mailDispatcher: MailDispatcher

    @BeforeEach
    internal fun setUp() {
        mailDispatcher = mock(MailDispatcher::class.java)
        filteredMailSender = FilteredMailSender(mailDispatcher)
    }

    @Test
    fun shouldSendFiltered() {
        val mail = TemplatedMailMessage.with(
            Recipient.to(
                EmailAddress.from("user@test,com"),
                SimpleName.from("User")
            ), MailTemplateIdentifier.from("Template"))
            .subject(Subject.from("Subject"))
            .sender(Sender.from(EmailAddress.from("sender@test.com")))
            .build()

        filteredMailSender.send(mail)

        val argumentCaptor = ArgumentCaptor.forClass(TemplatedMailMessage::class.java)
        verify(mailDispatcher).send(argumentCaptor.capture())

        assertThat(argumentCaptor.value.subject.toString()).isEqualTo(mail.subject)
        //assertThat(argumentCaptor.value.sender.email.toString()).isEqualTo("sender@domain.com")
        //assertThat(argumentCaptor.value.templateIdentifier.toString()).isEqualTo("Mail template identifier")
        //assertThat(argumentCaptor.value.recipients.get().first().email.toString()).isEqualTo("Jan Jansen")
        //assertThat(argumentCaptor.value.recipients.get().first().type).isEqualTo(Recipient.Type.To)
    }

    /* @Test
     fun shouldCreateMailSettingsFromMap() {
         val mailSettings = MailService.MailSettings(
             mapOf(
                 "mailSendTaskTo" to "mailSendTaskTo",
                 "mailSendTaskFrom" to "mailSendTaskFrom",
                 "mailSendTaskSubject" to "mailSendTaskSubject",
                 "mailSendTaskTemplate" to "mailSendTaskTemplate"
             ),
             delegateExecution
         )
         assertThat(mailSettings).isNotNull
         assertThat(mailSettings.mailSendTaskTo).isEqualTo("mailSendTaskTo")
         assertThat(mailSettings.mailSendTaskFrom).isEqualTo("mailSendTaskFrom")
         assertThat(mailSettings.mailSendTaskSubject).isEqualTo("mailSendTaskSubject")
         assertThat(mailSettings.mailSendTaskTemplate).isEqualTo("mailSendTaskTemplate")
     }*/

}