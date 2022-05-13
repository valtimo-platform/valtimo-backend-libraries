/*
 *  Copyright 2015-2022 Ritense BV, the Netherlands.
 *
 *  Licensed under EUPL, Version 1.2 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" basis,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.ritense.valtimo.formflow.security

import com.ritense.valtimo.contract.authentication.AuthoritiesConstants.ADMIN
import com.ritense.valtimo.contract.authentication.AuthoritiesConstants.USER
import com.ritense.valtimo.contract.security.config.HttpConfigurerConfigurationException
import com.ritense.valtimo.contract.security.config.HttpSecurityConfigurer
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity

class ValtimoFormFlowHttpSecurityConfigurer: HttpSecurityConfigurer {
    override fun configure(http: HttpSecurity) {
        try {
            http.authorizeRequests()
                .antMatchers(HttpMethod.GET, "/api/form-flow/{formFlowInstanceId}")
                .hasAuthority(USER)
                .antMatchers(HttpMethod.POST, "/api/form-flow/{formFlowId}/step/{stepInstanceId}")
                .hasAuthority(USER)
                .antMatchers(HttpMethod.POST, "/api/form-flow/{formFlowId}/back")
                .hasAuthority(USER)
                .antMatchers(HttpMethod.GET, "/api/process-link/form-flow-definition")
                .hasAuthority(ADMIN)

                // Temp matchers
                .antMatchers(HttpMethod.POST, "/api/form-flow/demo/definition/{definitionKey}/instance")
                .hasAuthority(ADMIN)
                .antMatchers(HttpMethod.POST, "/api/form-flow/demo/instance/{instanceId}/step/{stepId}/complete")
                .hasAuthority(ADMIN)
        } catch (e: Exception) {
            throw HttpConfigurerConfigurationException(e)
        }
    }
}
