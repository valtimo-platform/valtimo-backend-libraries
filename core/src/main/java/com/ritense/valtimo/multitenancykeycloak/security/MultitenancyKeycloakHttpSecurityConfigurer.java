package com.ritense.valtimo.multitenancykeycloak.security;

import com.ritense.valtimo.contract.security.config.HttpConfigurerConfigurationException;
import com.ritense.valtimo.contract.security.config.HttpSecurityConfigurer;
import com.ritense.valtimo.multitenancy.interceptor.MultitenancySecurityConfigurerAdapter;
import com.ritense.valtimo.multitenancy.service.TenantDomainService;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import static com.ritense.valtimo.contract.authentication.AuthoritiesConstants.ADMIN;
import static org.springframework.http.HttpMethod.POST;

public class MultitenancyKeycloakHttpSecurityConfigurer implements HttpSecurityConfigurer {
    @Override
    public void configure(HttpSecurity http) {
        try {
            http.authorizeRequests()
                .antMatchers(POST, "/api/tenant/keycloak-config").hasAuthority(ADMIN);
        } catch (Exception e) {
            throw new HttpConfigurerConfigurationException(e);
        }
    }
}
