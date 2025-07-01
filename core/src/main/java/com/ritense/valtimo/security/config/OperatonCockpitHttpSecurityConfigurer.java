/*
 * Copyright 2015-2024 Ritense BV, the Netherlands.
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

import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

import com.ritense.valtimo.contract.security.config.HttpConfigurerConfigurationException;
import com.ritense.valtimo.contract.security.config.HttpSecurityConfigurer;
import com.ritense.valtimo.security.matcher.WhitelistIpRequestMatcher;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.util.matcher.AndRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

public class OperatonCockpitHttpSecurityConfigurer implements HttpSecurityConfigurer {

    private final WhitelistIpRequestMatcher whitelistIpRequestMatcher;

    public OperatonCockpitHttpSecurityConfigurer(
        WhitelistIpRequestMatcher whitelistIpRequestMatcher
    ) {
        this.whitelistIpRequestMatcher = whitelistIpRequestMatcher;
    }

    @Override
    public void configure(HttpSecurity http) {
        try {
            //By default cockpit uses BasicAuth using different tech,
            //we just set it open and let Operaton do the rest.
            http.authorizeHttpRequests(requests ->
                requests.requestMatchers(
                    whiteListed(
                        antMatcher("/operaton/api/admin/**"),
                        antMatcher("/operaton/api/cockpit/**"),
                        antMatcher("/operaton/api/engine/**"),
                        antMatcher("/operaton/app/**"),
                        antMatcher("/operaton/assets/**"),
                        antMatcher("/operaton/favicon.ico"),
                        antMatcher("/operaton/lib/**")
                    )
                ).permitAll()
            );
        } catch (Exception e) {
            throw new HttpConfigurerConfigurationException(e);
        }
    }

    private RequestMatcher whiteListed(RequestMatcher... requestMatchers) {
        return new AndRequestMatcher(new OrRequestMatcher(requestMatchers), whitelistIpRequestMatcher);
    }

}