package com.ritense.plugin.security.config

import com.ritense.valtimo.contract.authentication.AuthoritiesConstants.ADMIN
import com.ritense.valtimo.contract.security.config.HttpConfigurerConfigurationException
import com.ritense.valtimo.contract.security.config.HttpSecurityConfigurer
import org.springframework.http.HttpMethod.GET
import org.springframework.http.HttpMethod.POST
import org.springframework.security.config.annotation.web.builders.HttpSecurity

class PluginHttpSecurityConfigurer: HttpSecurityConfigurer {

    override fun configure(http: HttpSecurity) {
        try {
            http.authorizeRequests()
                .antMatchers(GET, "/api/plugin/definition").hasAuthority(ADMIN)
                .antMatchers(GET, "/api/plugin/configuration").hasAuthority(ADMIN)
                .antMatchers(POST, "/api/plugin/configuration").hasAuthority(ADMIN)
                .antMatchers(GET, "/api/plugin/definition/{pluginDefinitionKey}/action").hasAuthority(ADMIN)
        } catch(e: Exception) {
            throw HttpConfigurerConfigurationException(e)
        }
    }
}