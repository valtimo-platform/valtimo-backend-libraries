package com.ritense.exact.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.exact.client.endpoints.ExchangeTokenEndpoint
import com.ritense.exact.client.endpoints.RefreshAccessTokenEndpoint
import com.ritense.exact.plugin.ExactPlugin
import com.ritense.exact.service.request.ExactExchangeRequest
import com.ritense.exact.service.response.ExactExchangeResponse
import com.ritense.plugin.domain.PluginConfiguration
import com.ritense.plugin.service.PluginConfigurationSearchParameters
import com.ritense.plugin.service.PluginService
import java.time.LocalDateTime
import mu.KotlinLogging
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.transaction.annotation.Propagation.REQUIRES_NEW
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.reactive.function.client.WebClient

@Transactional
class ExactService(
    val redirectUrl: String,
    val exactClient: WebClient,
    val pluginService: PluginService,
    val objectMapper: ObjectMapper
) {

    @Scheduled(cron = "\${exact.checkRefreshTokensCron:-}")
    @SchedulerLock(name = "ExactService_refreshTokenCron", lockAtLeastFor = "PT4S", lockAtMostFor = "PT60M")
    fun refreshRefreshTokens() {
        logger.info { "Starting Exact refresh token check"}
        pluginService
            .getPluginConfigurations(PluginConfigurationSearchParameters(category = "exact-supplier"))
            .forEach { pluginConfiguration ->
                val expiresOn = objectMapper.treeToValue(
                    pluginConfiguration.properties?.get("refreshTokenExpiresOn"),
                    LocalDateTime::class.java
                )

                if (expiresOn.minusDays(5).isBefore(LocalDateTime.now())) {
                    logger.info { "Refreshing Exact refresh token for plugin ${pluginConfiguration.id}"}
                    refreshTokens(pluginConfiguration)
                }
            }
        logger.info { "Finished Exact refresh token check"}
    }

    fun getPluginConfiguration(plugin: ExactPlugin): PluginConfiguration {
        return pluginService
            .getPluginConfigurations(PluginConfigurationSearchParameters(category = "exact-supplier")).first {
                it.properties?.get("clientId")?.asText()!! == plugin.clientId
            }
    }

    @Transactional(propagation = REQUIRES_NEW)
    fun refreshAccessTokens(pluginConfiguration: PluginConfiguration): String {
        val expiresOn = objectMapper.treeToValue(
            pluginConfiguration.properties?.get("accessTokenExpiresOn"),
            LocalDateTime::class.java
        )

        logger.info { "Access token expires on $expiresOn it is now ${LocalDateTime.now()}" }
        if (expiresOn.minusSeconds(30).isBefore(LocalDateTime.now())) {
            return refreshTokens(pluginConfiguration).properties?.get("accessToken")?.asText()!!
        }

        return pluginConfiguration.properties?.get("accessToken")?.asText()!!
    }

    private fun refreshTokens(pluginConfiguration: PluginConfiguration): PluginConfiguration {
        val clientId = pluginConfiguration.properties?.get("clientId")?.asText()!!
        val clientSecret = pluginConfiguration.properties?.get("clientSecret")?.asText()!!
        val refreshToken = pluginConfiguration.properties?.get("refreshToken")?.asText()!!

        val resp = RefreshAccessTokenEndpoint(
            clientId,
            clientSecret,
            refreshToken
        ).call(exactClient)

        pluginConfiguration.properties?.put("accessToken", resp.accessToken)
        pluginConfiguration.properties?.put("refreshToken", resp.refreshToken)
        pluginConfiguration.properties?.putPOJO(
            "accessTokenExpiresOn",
            LocalDateTime.now().plusSeconds(resp.expiresIn.toLong())
        )
        pluginConfiguration.properties?.putPOJO("refreshTokenExpiresOn", LocalDateTime.now().plusDays(30))

        return pluginService.updatePluginConfiguration(
            pluginConfiguration.id,
            pluginConfiguration.title,
            pluginConfiguration.properties!!
        )
    }

    fun exchange(req: ExactExchangeRequest): ExactExchangeResponse {
        val resp = ExchangeTokenEndpoint(
                redirectUrl,
                req.clientId,
                req.clientSecret,
                req.code
            ).call(exactClient)

        return ExactExchangeResponse(
            resp.accessToken,
            LocalDateTime.now().plusSeconds(resp.expiresIn.toLong()),
            resp.refreshToken,
            LocalDateTime.now().plusDays(30)
        )
    }

    companion object {
        val logger = KotlinLogging.logger {}
    }

}