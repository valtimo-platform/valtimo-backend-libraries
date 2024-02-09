package com.ritense.mail.config

import com.ritense.mail.BaseIntegrationTest
import jakarta.inject.Inject
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class MailingPropertiesIntTest : BaseIntegrationTest() {

    @Inject
    lateinit var mailingProperties: MailingProperties

    @Test
    fun `should load properties`() {
        assertThat(mailingProperties).isNotNull
        assertThat(mailingProperties.redirectAllMails).isEqualTo(true)
        assertThat(mailingProperties.redirectAllMailsPriority).isEqualTo(20)
        assertThat(mailingProperties.sendRedirectedMailsTo).contains("test2@test.com")

        assertThat(mailingProperties.whitelistedDomains).contains("test.com")
        assertThat(mailingProperties.whitelistedEmailAddresses).contains("test@test.com")
        assertThat(mailingProperties.onlyAllowWhitelistedRecipients).isEqualTo(true)
        assertThat(mailingProperties.whitelistedPriority).isEqualTo(10)

        assertThat(mailingProperties.blacklistFilter).isEqualTo(true)
        assertThat(mailingProperties.blacklistFilterPriority).isEqualTo(30)
    }

}