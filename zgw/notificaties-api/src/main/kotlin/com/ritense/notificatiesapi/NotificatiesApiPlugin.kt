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
import com.ritense.notificatiesapi.repository.NotificatiesApiAbonnementLinkRepository
import com.ritense.plugin.annotation.Plugin
import com.ritense.plugin.annotation.PluginCategory
import com.ritense.plugin.annotation.PluginEvent
import com.ritense.plugin.annotation.PluginEvent.EventType
import com.ritense.plugin.annotation.PluginProperty
import com.ritense.plugin.domain.PluginConfigurationId
import java.net.URI
import java.security.SecureRandom
import java.util.Base64
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.springframework.data.repository.findByIdOrNull

@Plugin(
    key = "notificatiesapi",
    title = "Notificaties API",
    description = "Enable interfacing with Notificaties API specification compliant APIs"
)
@PluginCategory("notificaties-api-plugin")
class NotificatiesApiPlugin(
    pluginConfigurationId: PluginConfigurationId,
    private val client: NotificatiesApiClient,
    private val notificatiesApiAbonnementLinkRepository: NotificatiesApiAbonnementLinkRepository
) {
    val notificatiesApiConfigurationId = NotificatiesApiConfigurationId(pluginConfigurationId.id)

    @PluginProperty(key = "url", secret = false)
    lateinit var url: URI

    @PluginProperty(key = "callbackUrl", secret = false)
    lateinit var callbackUrl: URI

    @PluginProperty(key = "authenticationPluginConfiguration", secret = false)
    lateinit var authenticationPluginConfiguration: NotificatiesApiAuthentication

    @PluginEvent(runOnEventType = EventType.CREATE)
    fun createAbonnement() {
        val authKey = createRandomKey()

        ensureKanalenExist(DEFAULT_KANALEN_NAMES)
        runBlocking {
            client.createAbonnement(
                authenticationPluginConfiguration,
                url,
                Abonnement(
                    callbackUrl = callbackUrl.toASCIIString(),
                    auth = authKey,
                    url = url.toASCIIString(),
                    kanalen = DEFAULT_KANALEN_NAMES.map { Abonnement.Kanaal(naam = it) }
                )
            )
        }.let {
            notificatiesApiAbonnementLinkRepository.save(
                NotificatiesApiAbonnementLink(
                    notificatiesApiConfigurationId = notificatiesApiConfigurationId,
                    url = it.url!!,
                    auth = it.auth ?: authKey
                )
            )
        }
    }

    @PluginEvent(runOnEventType = EventType.DELETE)
    fun deleteAbonnement() {

        notificatiesApiAbonnementLinkRepository.findByIdOrNull(notificatiesApiConfigurationId)
            ?.let {
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
                notificatiesApiAbonnementLinkRepository.deleteById(notificatiesApiConfigurationId)
            }
            ?: logger.warn {
                "Abonnement link was not found in the NotificatiesApiAbonnementLinkRepository" +
                    "for plugin configuration with id: ${notificatiesApiConfigurationId.id}"
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

    @PluginEvent(runOnEventType = EventType.UPDATE)
    fun updateAbonnement() {
        deleteAbonnement()
        createAbonnement()
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