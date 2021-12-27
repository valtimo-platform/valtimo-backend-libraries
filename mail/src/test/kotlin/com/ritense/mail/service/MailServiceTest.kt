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

import com.ritense.valtimo.contract.mail.MailSender
import com.ritense.valtimo.contract.mail.model.TemplatedMailMessage
import com.ritense.valtimo.contract.mail.model.value.Recipient
import org.assertj.core.api.Assertions.assertThat
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.extension.mockito.delegate.DelegateExecutionFake
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperties
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperty
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

internal class MailServiceTest {

    lateinit var mailService: MailService
    lateinit var mailSender: MailSender
    lateinit var delegateExecution: DelegateExecution

    @BeforeEach
    internal fun setUp() {
        mailSender = mock(MailSender::class.java)
        mailService = MailService(mailSender)
        delegateExecution = DelegateExecutionFake("id")
            .withProcessBusinessKey("businessKey")
            .withVariables(mutableMapOf("emailAddress" to "Jan Jansen"))

        val subjectProperty = mock(CamundaProperty::class.java)
        `when`(subjectProperty.getAttributeValue(anyString())).thenReturn("mailSendTaskSubject")
        `when`(subjectProperty.camundaValue).thenReturn("The Subject")

        val recipientProperty = mock(CamundaProperty::class.java)
        `when`(recipientProperty.getAttributeValue(anyString())).thenReturn("mailSendTaskTo")
        `when`(recipientProperty.camundaValue).thenReturn("\${emailAddress}")

        val mailTemplateProperty = mock(CamundaProperty::class.java)
        `when`(mailTemplateProperty.getAttributeValue(anyString())).thenReturn("mailSendTaskTemplate")
        `when`(mailTemplateProperty.camundaValue).thenReturn("Mail template identifier")

        val senderProperty = mock(CamundaProperty::class.java)
        `when`(senderProperty.getAttributeValue(anyString())).thenReturn("mailSendTaskFrom")
        `when`(senderProperty.camundaValue).thenReturn("sender@domain.com")

        val camundaProperties = mock(CamundaProperties::class.java)
        `when`(camundaProperties.camundaProperties).thenReturn(listOf(
            subjectProperty,
            recipientProperty,
            mailTemplateProperty,
            senderProperty
        ))

        `when`(delegateExecution
            .bpmnModelElementInstance
            .extensionElements
            .elementsQuery
            .filterByType(CamundaProperties::class.java)
            .singleResult()
        ).thenReturn(camundaProperties)
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
        assertThat(argumentCaptor.value.recipients.get().first().type).isEqualTo(Recipient.Type.To)
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