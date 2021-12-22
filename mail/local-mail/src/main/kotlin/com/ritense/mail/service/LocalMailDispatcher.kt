package com.ritense.mail.service

import com.ritense.mail.MailDispatcher
import com.ritense.valtimo.contract.mail.model.MailMessageStatus
import com.ritense.valtimo.contract.mail.model.RawMailMessage
import com.ritense.valtimo.contract.mail.model.TemplatedMailMessage

class LocalMailDispatcher : MailDispatcher {
    override fun send(rawMailMessage: RawMailMessage): MutableList<MailMessageStatus> {
        return mutableListOf()
    }

    override fun send(templatedMailMessage: TemplatedMailMessage): MutableList<MailMessageStatus> {
        return mutableListOf()
    }

    override fun getMaximumSizeAttachments(): Int {
        return 0
    }
}