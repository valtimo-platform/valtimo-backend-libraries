package com.ritense.tokenauthentication.plugin

import com.ritense.plugin.annotation.PluginCategory
import org.springframework.web.client.RestClient

@PluginCategory("valtimo-token-authentication-configuration")
interface TokenAuthentication {
    fun applyAuth(builder: RestClient.Builder): RestClient.Builder
}
