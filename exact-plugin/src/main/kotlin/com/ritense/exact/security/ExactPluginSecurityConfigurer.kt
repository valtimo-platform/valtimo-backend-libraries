package com.ritense.exact.security

import com.ritense.valtimo.contract.authentication.AuthoritiesConstants
import com.ritense.valtimo.contract.security.config.HttpConfigurerConfigurationException
import com.ritense.valtimo.contract.security.config.HttpSecurityConfigurer
import org.springframework.http.HttpMethod.POST
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher

class ExactPluginSecurityConfigurer : HttpSecurityConfigurer {
    override fun configure(http: HttpSecurity) {
        try {
            http.authorizeHttpRequests { requests ->
                requests.requestMatchers(antMatcher(POST, "/api/v1/plugin/exact/exchange")).hasAuthority(AuthoritiesConstants.ADMIN)
            }
        } catch (e: Exception) {
            throw HttpConfigurerConfigurationException(e)
        }
    }
}
