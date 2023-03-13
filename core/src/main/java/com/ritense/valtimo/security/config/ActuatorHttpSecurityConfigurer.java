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

package com.ritense.valtimo.security.config;

import com.ritense.valtimo.contract.security.config.AuthenticationConfigurerConfigurationException;
import com.ritense.valtimo.contract.security.config.AuthenticationSecurityConfigurer;
import com.ritense.valtimo.contract.security.config.HttpConfigurerConfigurationException;
import com.ritense.valtimo.contract.security.config.HttpSecurityConfigurer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;

import static com.ritense.valtimo.contract.authentication.AuthoritiesConstants.ACTUATOR;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;

public class ActuatorHttpSecurityConfigurer implements HttpSecurityConfigurer, AuthenticationSecurityConfigurer {

    private static final String ACTUATOR_REALM = "Actuator realm";
    private final String username;
    private final String password;
    private final PasswordEncoder passwordEncoder;

    public ActuatorHttpSecurityConfigurer(
        String username,
        String password,
        PasswordEncoder passwordEncoder
    ) {
        this.username = username;
        this.password = password;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void configure(HttpSecurity http) {
        try {
            http.authorizeRequests()
                .antMatchers(GET, "/management").hasAuthority(ACTUATOR)
                .antMatchers(GET, "/management/configprops").hasAuthority(ACTUATOR)
                .antMatchers(GET, "/management/env").hasAuthority(ACTUATOR)
                .antMatchers(GET, "/management/health").hasAuthority(ACTUATOR)
                .antMatchers(GET, "/management/mappings").hasAuthority(ACTUATOR)
                .antMatchers(GET, "/management/logfile").hasAuthority(ACTUATOR)
                .antMatchers(GET, "/management/loggers").hasAuthority(ACTUATOR)
                .antMatchers(POST, "/management/loggers").hasAuthority(ACTUATOR)
                .antMatchers(GET, "/management/info").hasAnyAuthority(ACTUATOR)
                .and()
                .httpBasic().authenticationEntryPoint(basicAuthenticationEntryPoint());
        } catch (Exception e) {
            throw new HttpConfigurerConfigurationException(e);
        }
    }

    @Override
    public void configure(AuthenticationManagerBuilder auth) {
        try {
            auth
                .inMemoryAuthentication()
                .withUser(username).password(passwordEncoder.encode(password))
                .authorities(ACTUATOR);
        } catch (Exception e) {
            throw new AuthenticationConfigurerConfigurationException(e);
        }
    }

    private AuthenticationEntryPoint basicAuthenticationEntryPoint() {
        final BasicAuthenticationEntryPoint entryPoint = new BasicAuthenticationEntryPoint();
        entryPoint.setRealmName(ACTUATOR_REALM);
        return entryPoint;
    }

}