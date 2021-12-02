package com.ritense.mail.flowmailer.domain

import com.ritense.mail.flowmailer.BaseTest
import com.ritense.valtimo.contract.basictype.EmailAddress
import com.ritense.valtimo.contract.basictype.SimpleName
import com.ritense.valtimo.contract.mail.model.value.Recipient
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

class OauthTokenResponseTest: BaseTest() {

    @Test
    fun `should make instance of OauthTokenReponse`() {
        val templatedMailMessage = templatedMailMessage(
            Recipient.to(
                EmailAddress.from("test@test.com"),
                SimpleName.from("testman")
            )
        )
        val submitMessages = SubmitMessage.from(templatedMailMessage)

        Assertions.assertThat(submitMessages[0].flowSelector).isEqualTo(templatedMailMessage.templateIdentifier.get())
    }
}