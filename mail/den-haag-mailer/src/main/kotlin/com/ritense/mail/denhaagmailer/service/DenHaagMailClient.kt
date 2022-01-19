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

package com.ritense.mail.denhaagmailer.service

import com.ritense.mail.denhaagmailer.connector.DenHaagMailerConnectorProperties
import com.ritense.mail.denhaagmailer.domain.EmailSendRequest
import com.ritense.mail.denhaagmailer.domain.EmailSendResponse
import com.ritense.mail.denhaagmailer.domain.EmailTemplateResponse
import org.springframework.core.io.Resource
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.http.MediaType.MULTIPART_FORM_DATA
import org.springframework.http.client.MultipartBodyBuilder
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException

class DenHaagMailClient(
    private var denHaagMailerConnectorProperties: DenHaagMailerConnectorProperties,
    private val denHaagMailerWebClientBuilder: WebClient.Builder
) {

    /**
     * Sends email
     *
     * @param attachments The email-attachment. Note: Any attachment-resource MUST have a fileName WITH extension. The fileName-extension determines the file-type.
     */
    fun send(
        emailTemplateId: String,
        emailRequest: EmailSendRequest,
        attachments: List<Resource>?
    ): EmailSendResponse {
        val builder = MultipartBodyBuilder()
        builder.part("request", emailRequest, APPLICATION_JSON)
        attachments?.forEach { builder.part("attachments[]", it) }

        try {
            val response = webClient().post()
                .uri("/wp-json/email/v1/send/$emailTemplateId")
                .contentType(MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .retrieve()
                .toEntity(EmailSendResponse::class.java)
                .block()!!
            return response.body!!
        } catch (e: WebClientResponseException.BadRequest) {
            throw HttpClientErrorException(HttpStatus.BAD_REQUEST, e.responseBodyAsString)
        } catch (e: WebClientResponseException.InternalServerError) {
            throw HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, e.responseBodyAsString)
        }
    }

    fun getEmailTemplates(): EmailTemplateResponse {
        try {
            val response = webClient().get()
                .uri("/wp-json/email/v1/get")
                .retrieve()
                .toEntity(EmailTemplateResponse::class.java)
                .block()!!
            return response.body!!
        } catch (e: WebClientResponseException.BadRequest) {
            throw HttpClientErrorException(HttpStatus.BAD_REQUEST, e.responseBodyAsString)
        } catch (e: WebClientResponseException.InternalServerError) {
            throw HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, e.responseBodyAsString)
        }
    }

    fun setProperties(denHaagMailerConnectorProperties: DenHaagMailerConnectorProperties) {
        this.denHaagMailerConnectorProperties = denHaagMailerConnectorProperties
    }

    private fun webClient(): WebClient {
        return denHaagMailerWebClientBuilder
            .baseUrl(denHaagMailerConnectorProperties.url!!)
            .build()
    }

}
