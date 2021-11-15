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

import com.ritense.valtimo.contract.mail.MailSender
import com.ritense.valtimo.contract.mail.model.MailMessageStatus
import com.ritense.valtimo.contract.mail.model.RawMailMessage
import com.ritense.valtimo.contract.mail.model.TemplatedMailMessage
import mu.KotlinLogging
import java.util.Optional

class LocalMailSender : MailSender {

    override fun send(rawMailMessage: RawMailMessage): Optional<MutableList<MailMessageStatus>> {
        logger.info { "LocalMailSender send(rawMailMessage: RawMailMessage) fake ${rawMailMessage.mailBody.textBody}" }
        return Optional.empty()
    }

    override fun send(templatedMailMessage: TemplatedMailMessage): Optional<MutableList<MailMessageStatus>> {
        logger.info { "LocalMailSender send(templatedMailMessage: TemplatedMailMessage) fake $templatedMailMessage" }
        return Optional.empty()
    }

    override fun getMaximumSizeAttachments(): Int {
        logger.info { "LocalMailSender getMaximumSizeAttachments() fake" }
        return 0
    }

    companion object {
        val logger = KotlinLogging.logger {}
    }
}