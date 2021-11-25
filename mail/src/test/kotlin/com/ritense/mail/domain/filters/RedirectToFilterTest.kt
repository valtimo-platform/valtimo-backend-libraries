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

package com.ritense.mail.domain.filters

import com.ritense.mail.BaseTest
import com.ritense.mail.config.MailingProperties
import com.ritense.valtimo.contract.basictype.EmailAddress
import com.ritense.valtimo.contract.basictype.SimpleName
import com.ritense.valtimo.contract.mail.model.RawMailMessage
import com.ritense.valtimo.contract.mail.model.value.Recipient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class RedirectToFilterTest : BaseTest() {

    lateinit var testRecipient: Recipient
    lateinit var redirectRecipient: Recipient

    @BeforeEach
    internal fun setUp() {
        testRecipient = Recipient.to(EmailAddress.from("test@ritense.com"), SimpleName.from("test"))
        redirectRecipient = Recipient.to(EmailAddress.from("redirectUser@ritense.com"), SimpleName.from("Redirected"))
    }

    @Test
    fun shouldContainRecipientBecauseRedirectUserMatched() {
        val rawMailMessageTest: RawMailMessage = rawMailMessage(testRecipient)

        val redirectToFilter = RedirectToFilter(
            MailingProperties(sendRedirectedMailsTo = listOf(testRecipient))
        )

        redirectToFilter.apply(rawMailMessageTest)

        assertThat(rawMailMessageTest.recipients.isPresent).isTrue
        assertThat(rawMailMessageTest.recipients.get()).containsOnly(testRecipient)
    }

    @Test
    fun shouldNotContainRecipientBecauseRedirectUserDoesntMatch() {
        val rawMailMessageTest: RawMailMessage = rawMailMessage(testRecipient)

        val redirectToFilter = RedirectToFilter(
            MailingProperties(sendRedirectedMailsTo = listOf(redirectRecipient))
        )

        redirectToFilter.apply(rawMailMessageTest)

        assertThat(rawMailMessageTest.recipients.isPresent).isFalse
        assertThat(rawMailMessageTest.recipients.get()).isEmpty()
    }

    @Test
    fun filterShouldDefaultBeDisabled() {
        val redirectToFilter = RedirectToFilter(MailingProperties())
        assertThat(redirectToFilter.isEnabled).isFalse
    }

    @Test
    fun filterShouldBeEnabledWhenRedirectAllMailIsTrue() {
        val redirectToFilter = RedirectToFilter(MailingProperties(isRedirectAllMails = true))
        assertThat(redirectToFilter.isEnabled).isTrue
    }

    @Test
    fun filterPriorityShouldDefaultMinus1() {
        val redirectToFilter = RedirectToFilter(MailingProperties())
        assertThat(redirectToFilter.priority).isEqualTo(-1)
    }

    @Test
    fun filterPriorityShouldBe1() {
        val redirectToFilter = RedirectToFilter(MailingProperties(redirectAllMailsPriority = 1))
        assertThat(redirectToFilter.priority).isEqualTo(1)
    }

}