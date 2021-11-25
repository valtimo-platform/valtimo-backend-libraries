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
        whitelistRecipient = Recipient.to(EmailAddress.from("redirectUser@ritense.com"), SimpleName.from("Redirected"))
    }

    @Test
    fun shouldFilterOutRecipientNotOnWhitelist() {
        val redirectToFilter = WhitelistFilter(
            MailingProperties(whitelistedEmailAddresses = listOf(whitelistRecipient.email.get()))
        )
        val rawMailMessageTest: RawMailMessage = rawMailMessage(testRecipient)
        redirectToFilter.apply(rawMailMessageTest)

        assertThat(rawMailMessageTest.recipients.isPresent).isFalse
        assertThat(rawMailMessageTest.recipients.get()).isEmpty()
    }

    @Test
    fun shouldContainWhitelistRecipient() {
        val redirectToFilter = WhitelistFilter(
            MailingProperties(whitelistedEmailAddresses = listOf(whitelistRecipient.email.get()))
        )
        val rawMailMessageTest: RawMailMessage = rawMailMessage(whitelistRecipient)
        redirectToFilter.apply(rawMailMessageTest)

        assertThat(rawMailMessageTest.recipients.isPresent).isTrue
        assertThat(rawMailMessageTest.recipients.get()).containsOnly(whitelistRecipient)
    }

    @Test
    fun filterShouldDefaultBeDisabled() {
        val redirectToFilter = WhitelistFilter(MailingProperties())

        assertThat(redirectToFilter.isEnabled).isFalse
    }

    @Test
    fun filterShouldBeEnabledWhenIsOnlyAllowWhitelistedRecipientsIsTrue() {
        val redirectToFilter = WhitelistFilter(MailingProperties(isOnlyAllowWhitelistedRecipients = true))
        assertThat(redirectToFilter.isEnabled).isTrue
    }

    @Test
    fun filterPriorityShouldDefaultMinus1() {
        val redirectToFilter = WhitelistFilter(MailingProperties())
        assertThat(redirectToFilter.priority).isEqualTo(-1)
    }

    @Test
    fun filterPriorityShouldBe1() {
        val redirectToFilter = WhitelistFilter(MailingProperties(whitelistedPriority = 1))
        assertThat(redirectToFilter.priority).isEqualTo(1)
    }

}