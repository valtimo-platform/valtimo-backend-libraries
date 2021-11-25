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

internal class WhitelistFilterTest : BaseTest() {

    lateinit var testRecipient: Recipient
    lateinit var whitelistRecipient: Recipient

    @BeforeEach
    internal fun setUp() {
        testRecipient = Recipient.to(EmailAddress.from("test@ritense.com"), SimpleName.from("test"))
        whitelistRecipient = Recipient.to(EmailAddress.from("WhitelistUser@ritense.com"), SimpleName.from("Mr Whitelist"))
    }

    @Test
    fun shouldFilterOutRecipientNotOnWhitelist() {
        val whitelistFilter = WhitelistFilter(
            MailingProperties(whitelistedEmailAddresses = listOf(whitelistRecipient.email.get()))
        )
        val rawMailMessageTest: RawMailMessage = rawMailMessage(testRecipient)
        whitelistFilter.apply(rawMailMessageTest)

        assertThat(rawMailMessageTest.recipients.isPresent).isFalse
        assertThat(rawMailMessageTest.recipients.get()).isEmpty()
    }

    @Test
    fun shouldContainWhitelistRecipient() {
        val whitelistFilter = WhitelistFilter(
            MailingProperties(whitelistedEmailAddresses = listOf(whitelistRecipient.email.get()))
        )
        val rawMailMessageTest: RawMailMessage = rawMailMessage(whitelistRecipient)
        whitelistFilter.apply(rawMailMessageTest)

        assertThat(rawMailMessageTest.recipients.isPresent).isTrue
        assertThat(rawMailMessageTest.recipients.get()).containsOnly(whitelistRecipient)
    }

    @Test
    fun filterShouldDefaultBeDisabled() {
        val whitelistFilter = WhitelistFilter(MailingProperties())
        assertThat(whitelistFilter.isEnabled).isFalse
    }

    @Test
    fun filterShouldBeEnabledWhenIsOnlyAllowWhitelistedRecipientsIsTrue() {
        val whitelistFilter = WhitelistFilter(MailingProperties(isOnlyAllowWhitelistedRecipients = true))
        assertThat(whitelistFilter.isEnabled).isTrue
    }

    @Test
    fun filterPriorityShouldDefaultMinus1() {
        val whitelistFilter = WhitelistFilter(MailingProperties())
        assertThat(whitelistFilter.priority).isEqualTo(-1)
    }

    @Test
    fun filterPriorityShouldBe1() {
        val redirectToFilter = WhitelistFilter(MailingProperties(whitelistedPriority = 1))
        assertThat(redirectToFilter.priority).isEqualTo(1)
    }

}