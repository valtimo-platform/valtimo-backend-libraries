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

package com.ritense.valtimo.security.config;

import com.ritense.valtimo.contract.config.ValtimoProperties;
import com.ritense.valtimo.contract.security.config.HttpConfigurerConfigurationException;
import com.ritense.valtimo.contract.security.config.HttpSecurityConfigurer;
import com.ritense.valtimo.security.jwt.JwtSecurityConfigurerAdapter;
import com.ritense.valtimo.security.jwt.authentication.TokenAuthenticationService;
import org.camunda.bpm.engine.IdentityService;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

public class JwtHttpSecurityConfigurer implements HttpSecurityConfigurer {

    private final IdentityService identityService;
    private final TokenAuthenticationService tokenAuthenticationService;
    private final ValtimoProperties valtimoProperties;

    public JwtHttpSecurityConfigurer(
        IdentityService identityService,
        TokenAuthenticationService tokenAuthenticationService,
        ValtimoProperties valtimoProperties
    ) {
        this.tokenAuthenticationService = tokenAuthenticationService;
        this.identityService = identityService;
        this.valtimoProperties = valtimoProperties;
    }

    @Override
    public void configure(HttpSecurity http) {
        try {
            http.apply(jwtSecurityConfigurerAdapter());
        } catch (Exception e) {
            throw new HttpConfigurerConfigurationException(e);
        }
    }

    private JwtSecurityConfigurerAdapter jwtSecurityConfigurerAdapter() {
        return new JwtSecurityConfigurerAdapter(identityService, tokenAuthenticationService, valtimoProperties);
    }

}