package com.ritense.mail.event

import com.ritense.valtimo.contract.basictype.EmailAddress
import com.ritense.valtimo.contract.mail.model.value.Subject

data class MailSendEvent(
    val recipient: EmailAddress,
    val subject: Subject,
)
