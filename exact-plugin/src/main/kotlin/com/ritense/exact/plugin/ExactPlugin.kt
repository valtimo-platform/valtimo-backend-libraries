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

package com.ritense.exact.plugin

import com.ritense.exact.client.endpoints.ExactGetRequest
import com.ritense.exact.client.endpoints.ExactPostRequest
import com.ritense.exact.client.endpoints.ExactPutRequest
import com.ritense.exact.client.endpoints.GetEndpoint
import com.ritense.exact.client.endpoints.PostEndpoint
import com.ritense.exact.client.endpoints.PutEndpoint
import com.ritense.exact.service.ExactService
import com.ritense.plugin.annotation.Plugin
import com.ritense.plugin.annotation.PluginAction
import com.ritense.plugin.annotation.PluginActionProperty
import com.ritense.plugin.annotation.PluginCategory
import com.ritense.plugin.annotation.PluginProperty
import com.ritense.processlink.domain.ActivityTypeWithEventName
import org.operaton.bpm.engine.delegate.DelegateExecution
import org.springframework.context.ApplicationContext
import org.springframework.web.client.RestClient
import java.time.LocalDateTime

@PluginCategory(key = "exact-supplier")
@Plugin(
    key = "exact",
    title = "Exact Plugin",
    description = "Exact Plugin"
)
class ExactPlugin(
    private val exactService: ExactService,
    private val exactClient: RestClient,
    private val context: ApplicationContext
) {

    @PluginProperty(
        key = "clientId",
        title = "Client ID",
        required = true,
        secret = false
    )
    lateinit var clientId: String

    @PluginProperty(
        key = "clientSecret",
        title = "Client Secret",
        required = true,
        secret = true
    )
    lateinit var clientSecret: String

    @PluginProperty(
        key = "refreshToken",
        title = "Refresh Token",
        required = true,
        secret = true
    )
    lateinit var refreshToken: String

    @PluginProperty(
        key = "refreshTokenExpiresOn",
        title = "Refresh Token Expiration Time",
        required = true,
        secret = true
    )
    lateinit var refreshTokenExpiresOn: LocalDateTime

    @PluginProperty(
        key = "accessToken",
        title = "Access Token",
        required = true,
        secret = true
    )
    lateinit var accessToken: String

    @PluginProperty(
        key = "accessTokenExpiresOn",
        title = "Access Token Expiration Time",
        required = true,
        secret = true
    )
    lateinit var accessTokenExpiresOn: LocalDateTime

    @PluginAction(
        key = "exact-get-request",
        title = "A GET call to Exact",
        description = "Make a GET call to Exact",
        activityTypes = [ActivityTypeWithEventName.SERVICE_TASK_START]
    )
    fun getCallExact(execution: DelegateExecution, @PluginActionProperty properties: ExactCallProperties) {
        val token = exactService.refreshAccessTokens(exactService.getPluginConfiguration(this))

        if (properties.bean != null) {
            val bean = context.getBean(properties.bean, ExactGetRequest::class.java)
            val response = bean.createRequest(execution, token).call(exactClient)
            bean.handleResponse(execution, response)
        } else {
            execution.setVariable(
                "exactGetResult", GetEndpoint(
                    token,
                    properties.uri!!
                ).call(exactClient)
            )
        }
    }

    @PluginAction(
        key = "exact-post-request",
        title = "A POST call to Exact",
        description = "Make a POST call to Exact",
        activityTypes = [ActivityTypeWithEventName.SERVICE_TASK_START]
    )
    fun postCallExact(
        execution: DelegateExecution,
        @PluginActionProperty properties: ExactCallProperties
    ) {
        val token = exactService.refreshAccessTokens(exactService.getPluginConfiguration(this))

        if (properties.bean != null) {
            val bean = context.getBean(properties.bean, ExactPostRequest::class.java)
            val response = bean.createRequest(execution, token).call(exactClient)
            bean.handleResponse(execution, response)
        } else {
            execution.setVariable(
                "exactPostResult", PostEndpoint(
                    token,
                    properties.uri!!,
                    properties.content!!
                ).call(exactClient)
            )
        }
    }

    @PluginAction(
        key = "exact-put-request",
        title = "A PUT call to Exact",
        description = "Make a PUT call to Exact",
        activityTypes = [ActivityTypeWithEventName.SERVICE_TASK_START]
    )
    fun putCallExact(execution: DelegateExecution, @PluginActionProperty properties: ExactCallProperties) {
        val token = exactService.refreshAccessTokens(exactService.getPluginConfiguration(this))

        if (properties.bean != null) {
            val bean = context.getBean(properties.bean, ExactPutRequest::class.java)
            val response = bean.createRequest(execution, token).call(exactClient)
            bean.handleResponse(execution, response)
        } else {
            execution.setVariable(
                "exactPutResult", PutEndpoint(
                    token,
                    properties.uri!!,
                    properties.content!!
                ).call(exactClient)
            )
        }
    }

    data class ExactCallProperties(
        val uri: String?,
        val content: String?,
        val bean: String?
    )

}