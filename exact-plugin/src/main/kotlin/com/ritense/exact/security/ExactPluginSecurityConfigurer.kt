package com.ritense.exact.security

import com.ritense.valtimo.contract.authentication.AuthoritiesConstants
import com.ritense.valtimo.contract.security.config.HttpConfigurerConfigurationException
import com.ritense.valtimo.contract.security.config.HttpSecurityConfigurer
import org.springframework.http.HttpMethod.POST
import org.springframework.security.config.annotation.web.builders.HttpSecurity

class ExactPluginSecurityConfigurer : HttpSecurityConfigurer {
    override fun configure(http: HttpSecurity) {
        try {
            http.authorizeRequests()
                .antMatchers(POST, "/api/v1/plugin/exact/exchange").hasAuthority(AuthoritiesConstants.ADMIN)
        } catch (e: Exception) {
            throw HttpConfigurerConfigurationException(e)
        }
    }
}
