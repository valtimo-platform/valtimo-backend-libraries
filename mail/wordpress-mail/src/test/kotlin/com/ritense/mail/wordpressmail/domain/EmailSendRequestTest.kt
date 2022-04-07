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

package com.ritense.mail.wordpressmail.domain

import com.ritense.mail.wordpressmail.BaseTest
import com.ritense.valtimo.contract.basictype.EmailAddress
import com.ritense.valtimo.contract.basictype.SimpleName
import com.ritense.valtimo.contract.mail.model.value.Recipient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class EmailSendRequestTest : BaseTest() {

    @Test
    fun `should create SendRequest with placeholders and subject`() {
        val templatedMailMessage = templatedMailMessage(
            recipient = Recipient.to(EmailAddress.from("john@example.com"), SimpleName.from("John Doe")),
            subject = "Welcome",
            placeholders = mapOf("VOORNAAM" to "John")
        )

        val sendRequests = EmailSendRequest.from(templatedMailMessage)

        assertThat(sendRequests[0].to).isEqualTo("John Doe <john@example.com>")
        assertThat(sendRequests[0].variables).containsEntry("VOORNAAM", "John")
        assertThat(sendRequests[0].variables).containsEntry("SUBJECT", "Welcome")
    }

    @Test
    fun `should fix placeholders with invalid format`() {
        val templatedMailMessage = templatedMailMessage(
            recipient = Recipient.to(EmailAddress.from("john@example.com"), SimpleName.from("John Doe")),
            subject = "Welcome",
            placeholders = mapOf(
                "SnakeCaseTest" to "value1",
                "This-is+an~invalid&char1test" to "value2"
            )
        )

        val sendRequests = EmailSendRequest.from(templatedMailMessage)

        assertThat(sendRequests[0].variables).containsEntry("SNAKE_CASE_TEST", "value1")
        assertThat(sendRequests[0].variables).containsEntry("THIS_IS_AN_INVALID_CHAR_TEST", "value2")
    }

    @Test
    fun `should fix placeholders from process variables`() {
        val templatedMailMessage = templatedMailMessage(
            recipient = Recipient.to(EmailAddress.from("john@example.com"), SimpleName.from("John Doe")),
            subject = "Welcome",
            placeholders = mapOf(
                "var" to mapOf("status" to "Geregistreerd","emailadres" to "john@example.com")
            )
        )

        val sendRequests = EmailSendRequest.from(templatedMailMessage)

        assertThat(sendRequests[0].variables).containsEntry("VAR_STATUS", "Geregistreerd")
        assertThat(sendRequests[0].variables).containsEntry("VAR_EMAILADRES", "john@example.com")
    }

}