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

package com.ritense.valtimo.security.adapter;

import com.ritense.valtimo.contract.security.config.AuthenticationSecurityConfigurer;
import com.ritense.valtimo.contract.security.config.HttpSecurityConfigurer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

import java.util.List;

import static org.springframework.http.HttpMethod.OPTIONS;

public class CoreHttpSecurityConfigurerAdapter extends WebSecurityConfigurerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(CoreHttpSecurityConfigurerAdapter.class);
    private final List<HttpSecurityConfigurer> httpSecurityConfigurers;
    private final List<AuthenticationSecurityConfigurer> authenticationSecurityConfigurers;

    public CoreHttpSecurityConfigurerAdapter(
        final List<HttpSecurityConfigurer> httpSecurityConfigurers,
        final List<AuthenticationSecurityConfigurer> authenticationSecurityConfigurers
    ) {
        this.httpSecurityConfigurers = httpSecurityConfigurers;
        this.authenticationSecurityConfigurers = authenticationSecurityConfigurers;
    }

    @Override
    protected void configure(HttpSecurity httpSecurity) {
        logger.debug("Configuring httpSecurity");
        httpSecurityConfigurers.forEach(httpConfig -> {
            logger.debug("Configure {}", httpConfig.getClass());
            httpConfig.configure(httpSecurity);
        });
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) {
        authenticationSecurityConfigurers.forEach(authConfig -> {
            logger.debug("Configure AuthenticationManagerBuilder with AuthenticationSecurityConfigurer {}", authConfig.getClass());
            authConfig.configure(auth);
        });
    }

    @Override
    public void configure(WebSecurity web) {
        web.ignoring()
            .antMatchers(OPTIONS, "/**")
            .antMatchers("/content/**");
    }

}