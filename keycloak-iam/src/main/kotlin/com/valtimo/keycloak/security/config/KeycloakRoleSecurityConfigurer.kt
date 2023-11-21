package com.valtimo.keycloak.security.config

import com.ritense.valtimo.contract.security.config.HttpSecurityConfigurer
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import com.ritense.valtimo.contract.authentication.AuthoritiesConstants.ADMIN
import com.ritense.valtimo.contract.security.config.HttpConfigurerConfigurationException


class KeycloakRoleSecurityConfigurer: HttpSecurityConfigurer {

    override fun configure(http: HttpSecurity) {
        try {
            http.authorizeRequests()
                .antMatchers(HttpMethod.GET, "/api/keycloak/role").hasAnyAuthority(ADMIN)
        } catch (e: Exception)  {
            throw HttpConfigurerConfigurationException(e)
        }
    }
}