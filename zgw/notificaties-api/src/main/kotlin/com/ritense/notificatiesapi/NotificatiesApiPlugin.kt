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

package com.ritense.notificatiesapi

import com.ritense.notificatiesapi.client.NotificatiesApiClient
import com.ritense.notificatiesapi.domain.Abonnement
import com.ritense.notificatiesapi.domain.Kanaal
import com.ritense.notificatiesapi.domain.NotificatiesApiAbonnementLink
import com.ritense.notificatiesapi.domain.NotificatiesApiConfigurationId
import com.ritense.notificatiesapi.repository.AbonnementLinkRepository
import com.ritense.plugin.annotation.Plugin
import com.ritense.plugin.annotation.PluginProperty
import com.ritense.plugin.domain.PluginConfigurationId
import java.net.URI
import java.security.SecureRandom
import java.util.Base64
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging

@Plugin(
    key = "notificatiesapi",
    title = "Notificaties API",
    description = "Enable interfacing with Notificaties API specification compliant APIs"
)
class NotificatiesApiPlugin(
    private val client: NotificatiesApiClient,
    private val abonnementLinkRepository: AbonnementLinkRepository
) {
    @PluginProperty(key = "url", secret = false)
    lateinit var url: URI

    @PluginProperty(key = "authenticationPluginConfiguration", secret = false)
    lateinit var authenticationPluginConfiguration: NotificatiesApiAuthentication

    fun createAbonnement(
        pluginConfigurationId: PluginConfigurationId,
        callbackUrl: String,
        kanaalNames: Set<String> = DEFAULT_KANALEN_NAMES
    ) {
        ensureKanalenExist(kanaalNames)
        runBlocking {
            client.createAbonnement(
                authenticationPluginConfiguration,
                url,
                Abonnement(
                    callbackUrl = callbackUrl,
                    auth = createRandomKey(),
                    url = url.toASCIIString(),
                    kanalen = kanaalNames.map { Abonnement.Kanaal(naam = it) }
                )
            )
        }.let {
            abonnementLinkRepository.save(
                NotificatiesApiAbonnementLink(
                    pluginConfigurationId = pluginConfigurationId,
                    url = it.url!!,
                    auth = it.auth
                )
            )
        }
    }

    fun deleteAbonnement(pluginConfigurationId: PluginConfigurationId) {
        val pluginId = NotificatiesApiConfigurationId(pluginConfigurationId.id)

        abonnementLinkRepository.findById(pluginId)
            .ifPresent {
                try {
                    runBlocking {
                        client.deleteAbonnement(
                            authenticationPluginConfiguration,
                            url,
                            it.url.substringAfterLast("/")
                        )
                    }
                } catch (e: Exception) {
                    logger.warn(e) { "Abonnement could not be deleted in Notificaties API" }
                }
                abonnementLinkRepository.deleteById(pluginId)
            }
    }

    fun ensureKanalenExist(kanalen: Set<String>): Unit = runBlocking {
        val existingKanalen = client.getKanalen(authenticationPluginConfiguration, url).map { kanaal -> kanaal.naam }

        kanalen
            .filter { !existingKanalen.contains(it) }
            .forEach { kanaalNaam ->
                launch { client.createKanaal(authenticationPluginConfiguration, url, Kanaal(naam = kanaalNaam)) }
            }
    }


    private fun createRandomKey(): String {
        val random = SecureRandom()
        val bytes = ByteArray(32)
        random.nextBytes(bytes)
        return Base64.getEncoder().encodeToString(bytes)
    }

    companion object {
        val DEFAULT_KANALEN_NAMES = setOf("objecten")
        val logger = KotlinLogging.logger {}
    }
}
