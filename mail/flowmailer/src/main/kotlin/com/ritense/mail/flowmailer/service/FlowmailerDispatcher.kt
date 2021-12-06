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

package com.ritense.mail.flowmailer.service

import com.ritense.mail.MailDispatcher
import com.ritense.mail.flowmailer.config.FlowmailerProperties
import com.ritense.mail.flowmailer.domain.SubmitMessage
import com.ritense.valtimo.contract.basictype.EmailAddress
import com.ritense.valtimo.contract.mail.model.MailMessageStatus
import com.ritense.valtimo.contract.mail.model.RawMailMessage
import com.ritense.valtimo.contract.mail.model.TemplatedMailMessage
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate
import org.apache.commons.lang3.NotImplementedException
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpServerErrorException

class FlowmailerMailDispatcher(
    private val flowmailerProperties: FlowmailerProperties,
    private val flowmailerTokenService: FlowmailerTokenService,
    private val restTemplate: RestTemplate
) : MailDispatcher {

    override fun send(rawMailMessage: RawMailMessage): MutableList<MailMessageStatus> {
            throw NotImplementedException("Send has not been implemented with RawMailMessage")
    }

    override fun send(templatedMailMessage: TemplatedMailMessage): MutableList<MailMessageStatus> {
        val mailMessageStatusList = mutableListOf<MailMessageStatus>()
        val messages = SubmitMessage.from(templatedMailMessage)
        val submitMessageURI = "/${flowmailerProperties.accountId}/messages/submit"
        messages.forEach { message ->
            val mailMessageStatus = submitMessage(baseUrl + submitMessageURI, message)
            mailMessageStatusList.add(mailMessageStatus)
        }
        return mailMessageStatusList
    }

    override fun getMaximumSizeAttachments(): Int {
        return MAX_SIZE_ATTACHMENTS
    }

    private fun submitMessage(url: String, message: SubmitMessage): MailMessageStatus {
        val httpEntity = HttpEntity(message.toString(), getHttpHeaders())

        val flowmailerResponseStatus = restTemplate.exchange(url, HttpMethod.POST, httpEntity, String::class.java)
        if (flowmailerResponseStatus.statusCode.is2xxSuccessful) {
            val builder = MailMessageStatus.with(
                EmailAddress.from(message.recipientAddress),
                "SENT",
                //should get the id from header format "location": "https://api.flowmailer.net/520/messages/202106110944460bfd0ca81fd281ef9e"
                flowmailerResponseStatus.headers.location.path.split("/").last()
            )
            return builder.build()
        } else if (flowmailerResponseStatus.statusCode.is4xxClientError) {
            throw HttpClientErrorException(
                flowmailerResponseStatus.statusCode,
                "Message has not been sent due to client side error"
            )
        } else {
            throw HttpServerErrorException(
                flowmailerResponseStatus.statusCode,
                "Message has not been sent due to server side error"
            )
        }
    }

    private fun getHttpHeaders(): HttpHeaders {
        val httpHeaders = HttpHeaders()
        httpHeaders["Authorization"] = "Bearer " + flowmailerTokenService.getFlowmailerToken()
        httpHeaders.contentType = MediaType.valueOf("application/vnd.flowmailer.v1.12+json;charset=UTF-8")
        httpHeaders.accept = listOf(MediaType.valueOf("application/vnd.flowmailer.v1.12+json;charset=UTF-8"))
        return httpHeaders
    }

    companion object {
        private const val baseUrl = "https://api.flowmailer.net"
        const val MAX_SIZE_ATTACHMENTS: Int = 16250000 //TODO: What is the actual max size in Flowmailer?
    }
}