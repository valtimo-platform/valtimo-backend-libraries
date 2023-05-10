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

package com.ritense.mail.service

import com.ritense.mail.BaseTest
import com.ritense.mail.MailDispatcher
import com.ritense.valtimo.contract.basictype.EmailAddress
import com.ritense.valtimo.contract.basictype.SimpleName
import com.ritense.valtimo.contract.mail.MailFilter
import com.ritense.valtimo.contract.mail.model.RawMailMessage
import com.ritense.valtimo.contract.mail.model.TemplatedMailMessage
import com.ritense.valtimo.contract.mail.model.value.Recipient
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import java.util.Optional

internal class FilteredMailSenderTest : BaseTest() {

    private lateinit var filteredMailSender: FilteredMailSender
    lateinit var mailDispatcher: MailDispatcher
    lateinit var mailFilter: MailFilter

    @BeforeEach
    internal fun setUp() {
        mailDispatcher = mock(MailDispatcher::class.java)
        mailFilter = mock(MailFilter::class.java)
        filteredMailSender = FilteredMailSender(mailDispatcher, listOf(mailFilter))
    }

    @Test
    fun `should send templated mail message filtered`() {
        val templatedMailMessage = templatedMailMessage(
            Recipient.to(
                EmailAddress.from("user@test,com"),
                SimpleName.from("User")
            )
        )

        filteredMailSender.send(templatedMailMessage)

        verify(mailDispatcher).send(templatedMailMessage)
    }

    @Test
    fun `should not send templated mail message because filter removed it`() {
        `when`(mailFilter.isEnabled).thenReturn(true)
        `when`(mailFilter.doFilter(any(TemplatedMailMessage::class.java))).thenReturn(Optional.empty())

        val templatedMailMessage = templatedMailMessage(Recipient.to(
            EmailAddress.from("user@test,com"),
            SimpleName.from("User")
        ))

        filteredMailSender.send(templatedMailMessage)

        verify(mailDispatcher, times(0)).send(templatedMailMessage)
    }

    @Test
    fun `should send raw mail message filtered`() {
        val rawMailMessage = rawMailMessage(Recipient.to(
            EmailAddress.from("user@test,com"),
            SimpleName.from("User")
        ))

        filteredMailSender.send(rawMailMessage)

        verify(mailDispatcher).send(rawMailMessage)
    }

    @Test
    fun `should not send raw mail message because filter removed it`() {
        `when`(mailFilter.isEnabled).thenReturn(true)
        `when`(mailFilter.doFilter(any(RawMailMessage::class.java))).thenReturn(Optional.empty())

        val rawMailMessage = rawMailMessage(Recipient.to(
            EmailAddress.from("user@test,com"),
            SimpleName.from("User")
        ))

        filteredMailSender.send(rawMailMessage)

        verify(mailDispatcher, times(0)).send(rawMailMessage)
    }

}