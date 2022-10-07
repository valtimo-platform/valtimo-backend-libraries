/*
 * Copyright 2015-2020 Ritense BV, the Netherlands.
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

package com.ritense.valtimo.multitenancy.interceptor;

import com.ritense.valtimo.multitenancy.service.TenantDomainService;
import com.ritense.valtimo.security.jwt.JwtFilter;
import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

public class MultitenancySecurityConfigurerAdapter extends SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity> {

    private final TenantDomainService tenantDomainService;

    public MultitenancySecurityConfigurerAdapter(TenantDomainService tenantDomainService) {
        this.tenantDomainService = tenantDomainService;
    }

    @Override
    public void configure(HttpSecurity http) {
        MultitenancyFilter customFilter = new MultitenancyFilter(tenantDomainService);
        http.addFilterBefore(customFilter, UsernamePasswordAuthenticationFilter.class);
    }

}