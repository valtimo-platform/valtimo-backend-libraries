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

package com.ritense.mail.service

import com.ritense.valtimo.contract.mail.MailSender
import com.ritense.valtimo.contract.mail.model.TemplatedMailMessage
import com.ritense.valtimo.contract.mail.model.value.Recipient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.whenever
import org.operaton.bpm.engine.delegate.DelegateExecution
import org.operaton.bpm.model.bpmn.instance.operaton.OperatonProperties
import org.operaton.bpm.model.bpmn.instance.operaton.OperatonProperty

internal class MailServiceTest {

    lateinit var mailService: MailService
    lateinit var mailSender: MailSender
    lateinit var delegateExecution: DelegateExecution

    @BeforeEach
    internal fun setUp() {
        mailSender = mock(MailSender::class.java)
        mailService = MailService(mailSender)
        delegateExecution = mock(Mockito.RETURNS_DEEP_STUBS)
        whenever(delegateExecution.processBusinessKey).thenReturn("businessKey")
        whenever(delegateExecution.variables).thenReturn(mapOf("emailAddress" to "Jan Jansen"))

        val subjectProperty = mock(OperatonProperty::class.java)
        whenever(subjectProperty.getAttributeValue(anyString())).thenReturn("mailSendTaskSubject")
        whenever(subjectProperty.operatonName).thenReturn("mailSendTaskSubject")
        whenever(subjectProperty.operatonValue).thenReturn("The Subject")

        val recipientProperty = mock(OperatonProperty::class.java)
        whenever(recipientProperty.getAttributeValue(anyString())).thenReturn("mailSendTaskTo")
        whenever(recipientProperty.operatonName).thenReturn("mailSendTaskTo")
        whenever(recipientProperty.operatonValue).thenReturn("\${emailAddress}")

        val mailTemplateProperty = mock(OperatonProperty::class.java)
        whenever(mailTemplateProperty.getAttributeValue(anyString())).thenReturn("mailSendTaskTemplate")
        whenever(mailTemplateProperty.operatonName).thenReturn("mailSendTaskTemplate")
        whenever(mailTemplateProperty.operatonValue).thenReturn("Mail template identifier")

        val senderProperty = mock(OperatonProperty::class.java)
        whenever(senderProperty.getAttributeValue(anyString())).thenReturn("mailSendTaskFrom")
        whenever(senderProperty.operatonName).thenReturn("mailSendTaskFrom")
        whenever(senderProperty.operatonValue).thenReturn("sender@domain.com")

        val operatonProperties = mock(OperatonProperties::class.java)
        whenever(operatonProperties.operatonProperties).thenReturn(listOf(
            subjectProperty,
            recipientProperty,
            mailTemplateProperty,
            senderProperty
        ))

        whenever(delegateExecution
            .bpmnModelElementInstance
            .extensionElements
            .elementsQuery
            .filterByType(OperatonProperties::class.java)
            .singleResult()
        ).thenReturn(operatonProperties)
    }

    @Test
    fun `should send templated mail`() {
        mailService.sendElementTemplateTaskMail(delegateExecution)

        val argumentCaptor = ArgumentCaptor.forClass(TemplatedMailMessage::class.java)
        verify(mailSender).send(argumentCaptor.capture())

        assertThat(argumentCaptor.value.subject.toString()).isEqualTo("The Subject")
        assertThat(argumentCaptor.value.sender.email.toString()).isEqualTo("sender@domain.com")
        assertThat(argumentCaptor.value.templateIdentifier.toString()).isEqualTo("Mail template identifier")
        assertThat(argumentCaptor.value.recipients.get().first().email.toString()).isEqualTo("Jan Jansen")
        assertThat(argumentCaptor.value.recipients.get().first().type).isEqualTo(Recipient.Type.TO)
    }

    @Test
    fun `should create mail settings from map`() {
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
    }

}