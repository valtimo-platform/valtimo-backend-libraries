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

package com.ritense.smartdocuments.client

import com.ritense.smartdocuments.connector.SmartDocumentsConnectorProperties
import com.ritense.smartdocuments.domain.FilesResponse
import com.ritense.smartdocuments.domain.SmartDocumentsRequest
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.reactive.function.client.ExchangeFilterFunctions
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException

class SmartDocumentsClient(
    private var smartDocumentsConnectorProperties: SmartDocumentsConnectorProperties,
    private val smartDocumentsWebClientBuilder: WebClient.Builder
) {

    fun generateDocument(
        smartDocumentsRequest: SmartDocumentsRequest,
    ): FilesResponse {
        try {
            return webClient().post()
                .uri("/wsxmldeposit/deposit/unattended")
                .contentType(APPLICATION_JSON)
                .bodyValue(smartDocumentsRequest)
                .retrieve()
                .bodyToMono(FilesResponse::class.java)
                .block()!!
        } catch (e: WebClientResponseException) {
            throw HttpClientErrorException(e.statusCode, e.responseBodyAsString)
        }
    }

    fun setProperties(smartDocumentsConnectorProperties: SmartDocumentsConnectorProperties) {
        this.smartDocumentsConnectorProperties = smartDocumentsConnectorProperties
    }

    private fun webClient(): WebClient {
        val basicAuthentication = ExchangeFilterFunctions.basicAuthentication(
            smartDocumentsConnectorProperties.username!!,
            smartDocumentsConnectorProperties.password!!
        )

        return smartDocumentsWebClientBuilder
            .clone()
            .baseUrl(smartDocumentsConnectorProperties.url!!)
            .filter(basicAuthentication)
            .build()
    }

}
