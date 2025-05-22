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

package com.ritense.notificatiesapi

import com.ritense.logging.withLoggingContext
import com.ritense.notificatiesapi.client.NotificatiesApiClient
import com.ritense.notificatiesapi.domain.Abonnement
import com.ritense.notificatiesapi.domain.Kanaal
import com.ritense.notificatiesapi.domain.NotificatiesApiAbonnementLink
import com.ritense.notificatiesapi.domain.NotificatiesApiConfigurationId
import com.ritense.notificatiesapi.repository.NotificatiesApiAbonnementLinkRepository
import com.ritense.plugin.annotation.Plugin
import com.ritense.plugin.annotation.PluginEvent
import com.ritense.plugin.annotation.PluginProperty
import com.ritense.plugin.domain.EventType
import com.ritense.plugin.domain.PluginConfiguration
import com.ritense.plugin.domain.PluginConfigurationId
import com.ritense.valtimo.contract.validation.Url
import mu.KotlinLogging
import org.springframework.data.repository.findByIdOrNull
import java.net.URI
import java.security.SecureRandom
import java.util.Base64

@Plugin(
    key = "notificatiesapi",
    title = "Notificaties API",
    description = "Enable interfacing with Notificaties API specification compliant APIs"
)
class NotificatiesApiPlugin(
    pluginConfigurationId: PluginConfigurationId,
    private val client: NotificatiesApiClient,
    private val notificatiesApiAbonnementLinkRepository: NotificatiesApiAbonnementLinkRepository
) {
    val notificatiesApiConfigurationId = NotificatiesApiConfigurationId(pluginConfigurationId.id)

    @Url
    @PluginProperty(key = "url", secret = false)
    lateinit var url: URI

    @Url
    @PluginProperty(key = "callbackUrl", secret = false)
    lateinit var callbackUrl: URI

    @PluginProperty(key = "authenticationPluginConfiguration", secret = false)
    lateinit var authenticationPluginConfiguration: NotificatiesApiAuthentication

    @PluginEvent(invokedOn = [EventType.CREATE])
    fun createAbonnement() = withLoggingContext(
        PluginConfiguration::class.java.canonicalName to notificatiesApiConfigurationId.toString()
    ) {
        val authKey = createRandomKey()
        logger.debug { "Creating new abonnement for Notificaties API plugin configuration with id '${notificatiesApiConfigurationId.id}'" }

        ensureKanalenExist(DEFAULT_KANALEN_NAMES)
        val abonnement = client.createAbonnement(
            authenticationPluginConfiguration,
            url,
            Abonnement(
                callbackUrl = callbackUrl.toASCIIString(),
                auth = authKey,
                kanalen = DEFAULT_KANALEN_NAMES.map { Abonnement.Kanaal(naam = it) }
            )
        )
        notificatiesApiAbonnementLinkRepository.save(
            NotificatiesApiAbonnementLink(
                notificatiesApiConfigurationId = notificatiesApiConfigurationId,
                url = abonnement.url!!,
                auth = abonnement.auth ?: authKey
            )
        )

        logger.info { "Abonnement created and saved with URL '${abonnement.url}' for Notificaties API configuration with id '${notificatiesApiConfigurationId.id}'" }
    }

    @PluginEvent(invokedOn = [EventType.DELETE])
    fun deleteAbonnement() = withLoggingContext(
        PluginConfiguration::class.java.canonicalName to notificatiesApiConfigurationId.toString()
    ) {
        logger.debug { "Deleting abonnement for Notificaties API configuration with id '${notificatiesApiConfigurationId.id}'" }

        notificatiesApiAbonnementLinkRepository.findByIdOrNull(notificatiesApiConfigurationId)
            ?.let {
                try {
                    client.deleteAbonnement(
                        authenticationPluginConfiguration,
                        url,
                        it.getAbonnementId()
                    )
                    logger.info { "Abonnement with url '${it.url}' successfully deleted for Notificaties API configuration with id '${notificatiesApiConfigurationId.id}'" }
                } catch (e: Exception) {
                    logger.warn(e) { "Abonnement with url '${it.url}' could not be deleted for Notificaties API configuration with id '${notificatiesApiConfigurationId.id}'" }
                }
                notificatiesApiAbonnementLinkRepository.deleteById(notificatiesApiConfigurationId)
                logger.info { "Abonnement link deleted for Notificaties API configuration with id '${notificatiesApiConfigurationId.id}'" }
            }
            ?: logger.warn {
                "Abonnement link was not found for Notificaties API configuration with id '${notificatiesApiConfigurationId.id}'"
            }
    }

    @PluginEvent(invokedOn = [EventType.UPDATE])
    fun updateAbonnement() = withLoggingContext(
        PluginConfiguration::class.java.canonicalName to notificatiesApiConfigurationId.toString()
    ) {
        logger.debug { "Updating abonnement for Notificaties API configuration with id '${notificatiesApiConfigurationId.id}'" }
        val dbAbonnement = notificatiesApiAbonnementLinkRepository.findByIdOrNull(notificatiesApiConfigurationId)
        if (dbAbonnement == null) {
            createAbonnement()
        } else {
            val abonnement = client.getAbonnement(
                authenticationPluginConfiguration,
                url,
                dbAbonnement.getAbonnementId(),
            )
            if (abonnement.equals(dbAbonnement.url, callbackUrl.toASCIIString(), DEFAULT_KANALEN_NAMES)) {
                logger.debug { "Skipping abonnement update. No change to abonnement detected for Notificaties API configuration with id '${notificatiesApiConfigurationId.id}'" }
            } else {
                client.updateAbonnement(
                    authenticationPluginConfiguration,
                    url,
                    dbAbonnement.getAbonnementId(),
                    Abonnement(
                        callbackUrl = callbackUrl.toASCIIString(),
                        auth = dbAbonnement.auth,
                        kanalen = DEFAULT_KANALEN_NAMES.map { Abonnement.Kanaal(naam = it) }
                    )
                )
                logger.info { "Abonnement with url '${dbAbonnement.url}' successfully updated for Notificaties API configuration with id '${notificatiesApiConfigurationId.id}'" }
            }
        }
    }

    fun ensureKanalenExist(kanalen: Set<String>) {
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

    companion object {
        val DEFAULT_KANALEN_NAMES = setOf("objecten")
        val logger = KotlinLogging.logger {}
    }
}