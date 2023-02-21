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

package com.ritense.mail.domain.filters

import com.ritense.mail.BaseTest
import com.ritense.mail.config.MailingProperties
import com.ritense.mail.service.BlacklistService
import com.ritense.valtimo.contract.basictype.EmailAddress
import com.ritense.valtimo.contract.basictype.SimpleName
import com.ritense.valtimo.contract.mail.model.RawMailMessage
import com.ritense.valtimo.contract.mail.model.value.Recipient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock

internal class BlacklistFilterTest : BaseTest() {

    lateinit var testRecipient: Recipient
    lateinit var blacklistedRecipient: Recipient
    lateinit var blacklistService: BlacklistService

    @BeforeEach
    internal fun setUp() {
        blacklistService = mock(BlacklistService::class.java)
        testRecipient = Recipient.to(EmailAddress.from("test@ritense.com"), SimpleName.from("test"))
        blacklistedRecipient = Recipient.to(EmailAddress.from("blacklisted@ritense.com"), SimpleName.from("Blacklist"))
    }

    @Test
    fun `should filter blacklisted recipient`() {
        `when`(blacklistService.isBlacklisted(testRecipient.email.get())).thenReturn(true)
        val blacklistFilter = BlacklistFilter(MailingProperties(), blacklistService)
        val rawMailMessageTest: RawMailMessage = rawMailMessage(testRecipient)
        blacklistFilter.doFilter(rawMailMessageTest)

        assertThat(rawMailMessageTest.recipients.isPresent).isFalse
        assertThat(rawMailMessageTest.recipients.get()).isEmpty()
    }

    @Test
    fun `should not filter recipient`() {
        `when`(blacklistService.isBlacklisted(testRecipient.email.get())).thenReturn(false)
        val blacklistFilter = BlacklistFilter(MailingProperties(), blacklistService)
        val rawMailMessageTest: RawMailMessage = rawMailMessage(blacklistedRecipient)
        blacklistFilter.doFilter(rawMailMessageTest)

        assertThat(rawMailMessageTest.recipients.isPresent).isTrue
        assertThat(rawMailMessageTest.recipients.get()).containsOnly(blacklistedRecipient)
    }

    @Test
    fun `filter should default be enabled`() {
        val blacklistFilter = BlacklistFilter(MailingProperties(), blacklistService)
        assertThat(blacklistFilter.isEnabled).isTrue
    }

    @Test
    fun `filter should be disabled when isblacklist property is false`() {
        val blacklistFilter = BlacklistFilter(MailingProperties(blacklistFilter = false), blacklistService)
        assertThat(blacklistFilter.isEnabled).isFalse
    }

    @Test
    fun `filter priority should default minus one`() {
        val blacklistFilter = BlacklistFilter(MailingProperties(), blacklistService)
        assertThat(blacklistFilter.priority).isEqualTo(10)
    }

    @Test
    fun `filter priority should be one`() {
        val blacklistFilter = BlacklistFilter(MailingProperties(blacklistFilterPriority = 1), blacklistService)
        assertThat(blacklistFilter.priority).isEqualTo(1)
    }

}