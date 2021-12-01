package com.ritense.mail.flowmailer.service

import com.fasterxml.jackson.databind.node.ObjectNode
import com.ritense.mail.flowmailer.domain.SubmitMessage
import com.ritense.valtimo.contract.mail.model.RawMailMessage

class MailMessageConverter {

//    //todo: fun for making flowmailer template with template id.
//    //todo: are there more variables for flowmailer then for Mandrill? In that case we need to do sth with the RawMailMessage
//
//    fun convert(rawMailMessage: RawMailMessage): ObjectNode {
//        val message = SubmitMessage(
//            dataKey = ,
//            dataValue =,
//            headerFromAddress =,
//            headerFromName =,
//            headerToName =,
//            headersList =,
//            flowSelector =,
//            messageType =,
//            recipientName =,
//            recipientAddress =,
//            senderAddress =,
//            subject =
//        )
//        return message.toObjectNode()
//    }
}