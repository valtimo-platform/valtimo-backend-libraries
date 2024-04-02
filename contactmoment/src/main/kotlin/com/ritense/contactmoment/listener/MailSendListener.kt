package com.ritense.contactmoment.listener

import com.ritense.connector.service.ConnectorService
import com.ritense.contactmoment.connector.ContactMomentConnector
import com.ritense.contactmoment.domain.Kanaal
import com.ritense.mail.event.MailSendEvent
import org.springframework.context.event.EventListener

@Deprecated("Since 12.0.0. No replacement available.")
class MailSendListener(
    private val connectorService: ConnectorService,
) {
    @EventListener(MailSendEvent::class)
    fun handleSetStatus(event: MailSendEvent) {
        val contactMomentConnector = connectorService.loadByClassName(ContactMomentConnector::class.java)
        contactMomentConnector.createContactMoment(Kanaal.MAIL, event.subject.get())
    }
}