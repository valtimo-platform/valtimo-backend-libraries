package com.ritense.valtimo.multitenancy.security;

import com.ritense.valtimo.contract.security.config.HttpConfigurerConfigurationException;
import com.ritense.valtimo.contract.security.config.HttpSecurityConfigurer;
import com.ritense.valtimo.multitenancy.interceptor.MultitenancySecurityConfigurerAdapter;
import com.ritense.valtimo.multitenancy.service.TenantDomainService;
import com.ritense.valtimo.security.jwt.JwtSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import static com.ritense.valtimo.contract.authentication.AuthoritiesConstants.ADMIN;
import static org.springframework.http.HttpMethod.POST;

public class MultitenancyHttpSecurityConfigurer  implements HttpSecurityConfigurer {
    private final TenantDomainService tenantDomainService;

    public MultitenancyHttpSecurityConfigurer(TenantDomainService tenantDomainService) {
        this.tenantDomainService = tenantDomainService;
    }

    @Override
    public void configure(HttpSecurity http) {
        try {
            http.apply(multitenancySecurityConfigurerAdapter());
            http.authorizeRequests()
                .antMatchers(POST, "/api/tenant/domain").hasAuthority(ADMIN);
        } catch (Exception e) {
            throw new HttpConfigurerConfigurationException(e);
        }
    }

    private MultitenancySecurityConfigurerAdapter multitenancySecurityConfigurerAdapter() {
        return new MultitenancySecurityConfigurerAdapter(tenantDomainService);
    }
}
