/*
 * Copyright 2020 Dimpact.
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

package com.ritense.openzaak.security.config

import com.ritense.valtimo.contract.authentication.AuthoritiesConstants.ADMIN
import com.ritense.valtimo.contract.security.config.HttpConfigurerConfigurationException
import com.ritense.valtimo.contract.security.config.HttpSecurityConfigurer
import org.springframework.http.HttpMethod.DELETE
import org.springframework.http.HttpMethod.GET
import org.springframework.http.HttpMethod.POST
import org.springframework.http.HttpMethod.PUT
import org.springframework.security.config.annotation.web.builders.HttpSecurity

class ZaakTypeLinkHttpSecurityConfigurer : HttpSecurityConfigurer {

    override fun configure(http: HttpSecurity) {
        try {
            http.authorizeRequests()
                .antMatchers(GET, "/api/openzaak/link/{documentDefinitionName}").hasAuthority(ADMIN)
                .antMatchers(POST, "/api/openzaak/link").hasAuthority(ADMIN)
                .antMatchers(PUT, "/api/openzaak/link").hasAuthority(ADMIN)
                .antMatchers(POST, "/api/openzaak/link/{id}/service-handler").hasAuthority(ADMIN)
                .antMatchers(PUT, "/api/openzaak/link/{id}/service-handler").hasAuthority(ADMIN)
                .antMatchers(DELETE, "/api/openzaak/link/{id}/service-handler/{serviceTaskId}").hasAuthority(ADMIN)
                .antMatchers(GET, "/api/openzaak/link/process/{processDefinitionKey}").hasAuthority(ADMIN)
        } catch (e: Exception) {
            throw HttpConfigurerConfigurationException(e)
        }
    }
}