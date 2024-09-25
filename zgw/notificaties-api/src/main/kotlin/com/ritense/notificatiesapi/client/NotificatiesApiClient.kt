/*
 * Copyright 2015-2024 Ritense BV, the Netherlands.
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

package com.ritense.notificatiesapi.client

import com.ritense.notificatiesapi.NotificatiesApiAuthentication
import com.ritense.notificatiesapi.domain.Abonnement
import com.ritense.notificatiesapi.domain.Kanaal
import java.net.URI
import org.springframework.http.MediaType
import org.springframework.web.client.RestClient
import org.springframework.web.client.body

class NotificatiesApiClient(
    private val restClientBuilder: RestClient.Builder
) {

    fun getAbonnement(
        authentication: NotificatiesApiAuthentication,
        baseUrl: URI,
        abonnmentId: String
    ): Abonnement {
        return buildNotificatiesRestClient(authentication, baseUrl)
            .get()
            .uri("abonnement/$abonnmentId")
            .retrieve()
            .body<Abonnement>()!!
    }

    fun createAbonnement(
        authentication: NotificatiesApiAuthentication,
        baseUrl: URI,
        abonnement: Abonnement
    ): Abonnement {
        return buildNotificatiesRestClient(authentication, baseUrl)
            .post()
            .uri("abonnement")
            .contentType(MediaType.APPLICATION_JSON)
            .body(abonnement)
            .retrieve()
            .body<Abonnement>()!!
    }

    fun deleteAbonnement(
        authentication: NotificatiesApiAuthentication,
        baseUrl: URI,
        abonnementId: String
    ) {
        buildNotificatiesRestClient(authentication, baseUrl)
            .delete()
            .uri("abonnement/$abonnementId")
            .retrieve()
            .toBodilessEntity()
    }

    fun createKanaal(
        authentication: NotificatiesApiAuthentication,
        baseUrl: URI,
        kanaal: Kanaal
    ): Kanaal {
        return buildNotificatiesRestClient(authentication, baseUrl)
            .post()
            .uri("kanaal")
            .body(kanaal)
            .contentType(MediaType.APPLICATION_JSON)
            .retrieve()
            .body<Kanaal>()!!
    }

    fun getKanalen(
        authentication: NotificatiesApiAuthentication,
        baseUrl: URI
    ): List<Kanaal> {
        return buildNotificatiesRestClient(authentication, baseUrl)
            .get()
            .uri("kanaal")
            .retrieve()
            .body<List<Kanaal>>()!!
    }

    private fun buildNotificatiesRestClient(
        authentication: NotificatiesApiAuthentication,
        baseUrl: URI
    ): RestClient = restClientBuilder
        .clone()
        .apply {
            authentication.applyAuth(it)
        }
        .baseUrl(baseUrl.toASCIIString())
        .build()
}
