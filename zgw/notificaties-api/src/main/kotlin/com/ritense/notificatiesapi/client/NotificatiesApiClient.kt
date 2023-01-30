/*
 * Copyright 2015-2023 Ritense BV, the Netherlands.
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
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBodilessEntity
import org.springframework.web.reactive.function.client.awaitBody


class NotificatiesApiClient(
    private val webclient: WebClient
) {

    internal suspend fun createAbonnement(
        authentication: NotificatiesApiAuthentication,
        baseUrl: URI,
        abonnement: Abonnement
    ): Abonnement {

        return buildNotificatiesWebClient(authentication, baseUrl)
            .post()
            .uri("abonnement")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(abonnement)
            .retrieve()
            .awaitBody()
    }

    internal suspend fun deleteAbonnement(
        authentication: NotificatiesApiAuthentication,
        baseUrl: URI,
        abonnementId: String
    ) {

        buildNotificatiesWebClient(authentication, baseUrl)
            .delete()
            .uri("abonnement/$abonnementId")
            .retrieve()
            .awaitBodilessEntity()
    }

    internal suspend fun createKanaal(authentication: NotificatiesApiAuthentication, baseUrl: URI, kanaal: Kanaal): Kanaal {
        return buildNotificatiesWebClient(authentication, baseUrl)
            .post()
            .uri("kanaal")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(kanaal)
            .retrieve()
            .awaitBody()
    }

    internal suspend fun getKanalen(authentication: NotificatiesApiAuthentication, baseUrl: URI): List<Kanaal> {
        return buildNotificatiesWebClient(authentication, baseUrl)
            .get()
            .uri("kanaal")
            .retrieve()
            .awaitBody()
    }

    private fun buildNotificatiesWebClient(authentication: NotificatiesApiAuthentication, baseUrl: URI): WebClient =
        webclient
            .mutate()
            .filter(authentication)
            .baseUrl(baseUrl.toASCIIString())
            .build()
}
