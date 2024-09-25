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
import java.net.URI
import java.security.SecureRandom
import java.util.Base64
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import mu.withLoggingContext
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.web.client.HttpClientErrorException

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
        runBlocking {
            client.createAbonnement(
                authenticationPluginConfiguration,
                url,
                Abonnement(
                    callbackUrl = callbackUrl.toASCIIString(),
                    auth = authKey,
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

            logger.info { "Abonnement created and saved with URL '${it.url}' for Notificaties API configuration with id '${notificatiesApiConfigurationId.id}'" }
        }
    }

    @PluginEvent(invokedOn = [EventType.DELETE])
    fun deleteAbonnement() = withLoggingContext(
        PluginConfiguration::class.java.canonicalName to notificatiesApiConfigurationId.toString()
    )
    {
        logger.debug { "Deleting abonnement for Notificaties API configuration with id '${notificatiesApiConfigurationId.id}'" }

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
    )
    {
        logger.debug { "Updating abonnement for Notificaties API configuration with id '${notificatiesApiConfigurationId.id}'" }

        if (!doesRemoteAbonnementExist()) {
            logger.debug { "Not able to find an existing abonnement in the Notificaties API. Creating a new one." }
            notificatiesApiAbonnementLinkRepository.deleteById(notificatiesApiConfigurationId)
            createAbonnement()
        }
    }

    fun ensureKanalenExist(kanalen: Set<String>) = runBlocking {
        logger.debug { "Ensuring Notificaties API kanalen '$kanalen' exist for authentication configuration with id '${authenticationPluginConfiguration.configurationId.id}'" }

        val existingKanalen = client.getKanalen(authenticationPluginConfiguration, url).map { it.naam }

        kanalen
            .filter { !existingKanalen.contains(it) }
            .forEach { kanaalNaam ->
                logger.debug { "Attempting to create Notificaties API kanaal with name '$kanaalNaam' for authentication configuration with id '${authenticationPluginConfiguration.configurationId.id}'" }
                launch {
                    client.createKanaal(authenticationPluginConfiguration, url, Kanaal(naam = kanaalNaam))
                    logger.info { "Successfully created Notificaties API kanaal with name '$kanaalNaam' for authentication configuration with id '${authenticationPluginConfiguration.configurationId.id}'" }
                }
            }
    }

    private fun doesRemoteAbonnementExist(): Boolean {
        logger.debug { "Determining whether an abonnement exists in the Notificaties API for Notificaties API configuration with id '${notificatiesApiConfigurationId.id}'" }

        val abonnement = notificatiesApiAbonnementLinkRepository.findByIdOrNull(notificatiesApiConfigurationId)
            ?: return false

        try {
            val abonnementId = abonnement.url.substringAfterLast("/")
            client.getAbonnement(authenticationPluginConfiguration, url, abonnementId)

            return true
        } catch (ex: HttpClientErrorException) {
            if (ex.statusCode != HttpStatus.NOT_FOUND) {
                throw ex
            }

            return false
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