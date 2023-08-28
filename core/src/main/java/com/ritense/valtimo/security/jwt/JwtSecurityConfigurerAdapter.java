/*
 * Copyright 2015-2023 Ritense BV, the Netherlands.
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

package com.ritense.valtimo.security.jwt;

import com.ritense.valtimo.security.jwt.authentication.TokenAuthenticationService;
import org.camunda.bpm.engine.IdentityService;
import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

public class JwtSecurityConfigurerAdapter extends SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity> {

    private final IdentityService identityService;
    private final TokenAuthenticationService tokenAuthenticationService;

    public JwtSecurityConfigurerAdapter(IdentityService identityService, TokenAuthenticationService tokenAuthenticationService) {
        this.identityService = identityService;
        this.tokenAuthenticationService = tokenAuthenticationService;
    }

    @Override
    public void configure(HttpSecurity http) {
        JwtFilter customFilter = new JwtFilter(identityService, tokenAuthenticationService);
        http.addFilterBefore(customFilter, UsernamePasswordAuthenticationFilter.class);
    }

}