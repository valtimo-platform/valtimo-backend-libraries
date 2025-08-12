/*
 * Copyright 2015-2025 Ritense BV, the Netherlands.
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
import com.ritense.notificatiesapi.exception.NotificatiesApiAbonnementException
import com.ritense.notificatiesapi.repository.NotificatiesApiAbonnementLinkRepository
import com.ritense.plugin.service.PluginConfigurationSearchParameters
import com.ritense.plugin.service.PluginService
import com.ritense.valtimo.contract.event.PluginsDeployedEvent
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.context.event.EventListener
import java.net.URI
import java.security.SecureRandom
import java.util.Base64

class PluginsDeployedEventListener(
    private val client: NotificatiesApiClient,
    private val notificatiesApiAbonnementLinkRepository: NotificatiesApiAbonnementLinkRepository,
    private val pluginService: PluginService
) : ApplicationContextAware {

    lateinit var context: ApplicationContext

    @EventListener(PluginsDeployedEvent::class)
    fun registerAbonnementenForNotificatiesApiPlugins() {
        val pluginConfigurations = pluginService
            .getPluginConfigurations(PluginConfigurationSearchParameters(category = "notificaties-api-plugin"))
            .map { pluginService.createInstance(it) as NotificatiesApiListener }
            .groupBy { it.getNotificatiesApiPlugin().url }

        val knownNotificatiesApiAbonnementLinks = notificatiesApiAbonnementLinkRepository.findAll()

        pluginConfigurations.forEach { _, configurations ->
            val notificatiesApiPluginInstance =
                configurations.first().getNotificatiesApiPlugin()

            var nrOfTriesLeftRetrievingAbonnementen = 3
            while (nrOfTriesLeftRetrievingAbonnementen > 0) {
                try {
                    registerAbonnementenForPluginNotificatiesApiPlugins(
                        notificatiesApiPluginInstance,
                        knownNotificatiesApiAbonnementLinks,
                        configurations
                    )
                    break
                } catch (_: Exception) {
                    --nrOfTriesLeftRetrievingAbonnementen
                }
            }
            if (nrOfTriesLeftRetrievingAbonnementen == 0) {
                val e = NotificatiesApiAbonnementException()
                throw e
            }
        }
    }

    private fun registerAbonnementenForPluginNotificatiesApiPlugins(
        notificatiesApiPluginInstance: NotificatiesApiPlugin,
        knownNotificatiesApiAbonnementLinks: List<NotificatiesApiAbonnementLink>,
        configurations: List<NotificatiesApiListener>
    ) {
        client.getAbonnementen(
            notificatiesApiPluginInstance.authenticationPluginConfiguration,
            notificatiesApiPluginInstance.url
        )
            .filter { abonnement -> abonnement.callbackUrl == notificatiesApiPluginInstance.callbackUrl.toString() }
            .filter { abonnement ->
                (
                    knownNotificatiesApiAbonnementLinks.firstOrNull {
                        it.url == abonnement.url
                    } == null)
            }.forEach {
                client.deleteAbonnement(
                    notificatiesApiPluginInstance.authenticationPluginConfiguration,
                    notificatiesApiPluginInstance.url,
                    it.url!!.substring(notificatiesApiPluginInstance.url.toString().length + 11)
                )
            }

        val kanalen = configurations.flatMap {
            it.getKanaalFilters()
        }

        val currentNotificatiesApiAbonnementLink = knownNotificatiesApiAbonnementLinks.firstOrNull {
            it.notificatiesApiConfigurationId.id == configurations.first()
                .getNotificatiesApiPlugin().notificatiesApiConfigurationId.id
        }

        val authKey = currentNotificatiesApiAbonnementLink?.auth ?: createRandomKey()

        logger.debug {
            "Creating new abonnement for Notificaties API plugin configuration with id " +
                "'${notificatiesApiPluginInstance.notificatiesApiConfigurationId.id}'"
        }

        ensureKanalenExist(
            kanalen.map { it.naam }.toSet(),
            notificatiesApiPluginInstance.authenticationPluginConfiguration,
            notificatiesApiPluginInstance.url
        )

        val abonnement = if (currentNotificatiesApiAbonnementLink == null) {
            client.createAbonnement(
                notificatiesApiPluginInstance.authenticationPluginConfiguration,
                notificatiesApiPluginInstance.url,
                Abonnement(
                    callbackUrl = notificatiesApiPluginInstance.callbackUrl.toASCIIString(),
                    auth = authKey,
                    kanalen = kanalen
                )
            )
        } else {
            client.updateAbonnement(
                notificatiesApiPluginInstance.authenticationPluginConfiguration,
                notificatiesApiPluginInstance.url,
                currentNotificatiesApiAbonnementLink.getAbonnementId(),
                Abonnement(
                    callbackUrl = notificatiesApiPluginInstance.callbackUrl.toASCIIString(),
                    auth = authKey,
                    kanalen = kanalen
                )
            )
        }

        notificatiesApiAbonnementLinkRepository.save(
            NotificatiesApiAbonnementLink(
                notificatiesApiConfigurationId = notificatiesApiPluginInstance.notificatiesApiConfigurationId,
                url = abonnement.url!!,
                auth = abonnement.auth ?: authKey
            )
        )
    }

    fun ensureKanalenExist(
        kanalen: Set<String>,
        authenticationPluginConfiguration: NotificatiesApiAuthentication,
        url: URI,
    ) {
        logger.debug { "Ensuring Notificaties API kanalen '$kanalen' exist for authentication configuration with id '${authenticationPluginConfiguration.configurationId.id}'" }
        val existingKanalen = client.getKanalen(authenticationPluginConfiguration, url).map { it.naam }
        kanalen
            .filter { !existingKanalen.contains(it) }
            .forEach { kanaalNaam ->
                logger.debug { "Attempting to create Notificaties API kanaal with name '$kanaalNaam' for authentication configuration with id '${authenticationPluginConfiguration.configurationId.id}'" }
                client.createKanaal(authenticationPluginConfiguration, url, Kanaal(naam = kanaalNaam))
                logger.info { "Successfully created Notificaties API kanaal with name '$kanaalNaam' for authentication configuration with id '${authenticationPluginConfiguration.configurationId.id}'" }
            }
    }

    private fun createRandomKey(): String {
        val random = SecureRandom()
        val bytes = ByteArray(32)
        random.nextBytes(bytes)
        return Base64.getEncoder().encodeToString(bytes)
    }

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        context = applicationContext
    }

    companion object {
        val logger = KotlinLogging.logger {}
    }
}