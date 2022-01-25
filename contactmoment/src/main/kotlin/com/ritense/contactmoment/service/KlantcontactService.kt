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

package com.ritense.contactmoment.service

import com.ritense.connector.service.ConnectorService
import com.ritense.contactmoment.connector.ContactMomentConnector
import com.ritense.contactmoment.domain.Kanaal
import com.ritense.contactmoment.domain.request.SendMessageRequest
import com.ritense.klant.service.KlantService
import com.ritense.valtimo.contract.basictype.EmailAddress
import com.ritense.valtimo.contract.basictype.SimpleName
import com.ritense.valtimo.contract.mail.MailSender
import com.ritense.valtimo.contract.mail.model.TemplatedMailMessage
import com.ritense.valtimo.contract.mail.model.value.MailTemplateIdentifier
import com.ritense.valtimo.contract.mail.model.value.Recipient
import com.ritense.valtimo.contract.mail.model.value.Subject
import java.util.UUID

class KlantcontactService(
    val mailSender: MailSender,
    val klantService: KlantService,
    val connectorService: ConnectorService,
    val templateName: String
) {

    fun sendMessage(documentId: UUID, message: SendMessageRequest) {
        val contactMomentConnector = connectorService.loadByClassName(ContactMomentConnector::class.java)

        val klant = klantService.getKlantForDocument(documentId)

        if (klant.emailadres == null) {
            throw IllegalStateException("emailaddress was not available for klant")
        }

        val recipient = Recipient.to(EmailAddress.from(klant.emailadres), SimpleName.from(klant.emailadres))
        val builder = TemplatedMailMessage
            .with(recipient, MailTemplateIdentifier.from(templateName))
            .placeholders(mapOf(
                "bodyText" to message.bodyText,
                "subject" to message.subject
            ))
            .subject(Subject.from(message.subject))

        mailSender.send(builder.build())

        contactMomentConnector.createContactMoment(Kanaal.MAIL, message.bodyText)

    }
}