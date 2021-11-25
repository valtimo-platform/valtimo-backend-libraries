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

import com.ritense.mail.BaseTest
import com.ritense.mail.MailDispatcher
import com.ritense.valtimo.contract.basictype.EmailAddress
import com.ritense.valtimo.contract.basictype.SimpleName
import com.ritense.valtimo.contract.mail.model.value.Recipient
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

internal class FilteredMailSenderTest : BaseTest() {

    private lateinit var filteredMailSender: FilteredMailSender
    lateinit var mailDispatcher: MailDispatcher

    @BeforeEach
    internal fun setUp() {
        mailDispatcher = mock(MailDispatcher::class.java)
        filteredMailSender = FilteredMailSender(mailDispatcher)
    }

    @Test
    fun shouldSendTemplatedMailMessageFiltered() {
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
    fun shouldSendRawMailMessageFiltered() {
        val rawMailMessage = rawMailMessage(Recipient.to(
            EmailAddress.from("user@test,com"),
            SimpleName.from("User")
        ))

        filteredMailSender.send(rawMailMessage)

        verify(mailDispatcher).send(rawMailMessage)
    }

}