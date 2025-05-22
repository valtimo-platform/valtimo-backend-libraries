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

package com.ritense.document.security

import com.ritense.valtimo.contract.authentication.AuthoritiesConstants.ADMIN
import com.ritense.valtimo.contract.security.config.AuthorizeRequestsHttpSecurityConfigurer
import org.springframework.http.HttpMethod.DELETE
import org.springframework.http.HttpMethod.GET
import org.springframework.http.HttpMethod.POST
import org.springframework.http.HttpMethod.PUT
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer

class CaseTagHttpSecurityConfigurer : AuthorizeRequestsHttpSecurityConfigurer() {

    override fun authorizeHttpRequests(requests: AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry) {
        requests
            .antMatcher(GET, "/api/v1/case-definition/{caseDefinitionName}/case-tag").authenticated()
            .antMatcher(GET, "/api/management/v1/case-definition/{caseDefinitionName}/case-tag").hasAuthority(ADMIN)
            .antMatcher(POST, "/api/management/v1/case-definition/{caseDefinitionName}/case-tag").hasAuthority(ADMIN)
            .antMatcher(PUT, "/api/management/v1/case-definition/{caseDefinitionName}/case-tag").hasAuthority(ADMIN)
            .antMatcher(PUT, "/api/management/v1/case-definition/{caseDefinitionName}/case-tag/{caseTagKey}").hasAuthority(ADMIN)
            .antMatcher(DELETE, "/api/management/v1/case-definition/{caseDefinitionName}/case-tag/{caseTagKey}").hasAuthority(ADMIN)
    }
}