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

package com.ritense.case.security.config

import com.ritense.valtimo.contract.authentication.AuthoritiesConstants.ADMIN
import com.ritense.valtimo.contract.authentication.AuthoritiesConstants.USER
import com.ritense.valtimo.contract.security.config.HttpConfigurerConfigurationException
import com.ritense.valtimo.contract.security.config.HttpSecurityConfigurer
import org.springframework.http.HttpMethod.DELETE
import org.springframework.http.HttpMethod.GET
import org.springframework.http.HttpMethod.PATCH
import org.springframework.http.HttpMethod.POST
import org.springframework.http.HttpMethod.PUT
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher

class CaseHttpSecurityConfigurer : HttpSecurityConfigurer {

    override fun configure(http: HttpSecurity) {
        try {
            http.authorizeHttpRequests { requests ->
                requests.requestMatchers(antMatcher(GET, LIST_COLUMN_URL)).authenticated()
                    .requestMatchers(antMatcher(POST, LIST_COLUMN_URL)).hasAuthority(ADMIN) // Deprecated
                    .requestMatchers(antMatcher(PUT, LIST_COLUMN_URL)).hasAuthority(ADMIN) // Deprecated
                    .requestMatchers(antMatcher(DELETE, "$LIST_COLUMN_URL/{columnKey}")).hasAuthority(ADMIN) // Deprecated
                    .requestMatchers(antMatcher(GET, "/api/v1/case/{caseDefinitionName}/settings")).authenticated()
                    .requestMatchers(antMatcher(GET, "/api/v1/case-definition/{caseDefinitionName}/tab")).authenticated() // Deprecated
                    .requestMatchers(antMatcher(GET, "/api/v1/document/{documentId}/tab")).authenticated()
                    .requestMatchers(antMatcher(PATCH, "/api/v1/case/{caseDefinitionName}/settings")).hasAuthority(ADMIN) // Deprecated
                    .requestMatchers(antMatcher(POST, "/api/v1/case/{caseDefinitionName}/search")).authenticated()
                    .requestMatchers(antMatcher(GET, "/api/management/v1/case/{caseDefinitionName}/settings")).hasAuthority(ADMIN)
                    .requestMatchers(antMatcher(PATCH, "/api/management/v1/case/{caseDefinitionName}/settings")).hasAuthority(ADMIN)
                    .requestMatchers(antMatcher(GET, MANAGEMENT_CASE_LIST_COLUMN_URL)).hasAuthority(ADMIN)
                    .requestMatchers(antMatcher(POST, MANAGEMENT_CASE_LIST_COLUMN_URL)).hasAuthority(ADMIN)
                    .requestMatchers(antMatcher(PUT, MANAGEMENT_CASE_LIST_COLUMN_URL)).hasAuthority(ADMIN)
                    .requestMatchers(antMatcher(DELETE, "$MANAGEMENT_CASE_LIST_COLUMN_URL/{columnKey}")).hasAuthority(ADMIN)
                    .requestMatchers(antMatcher(GET, CASE_COLUMN_URL)).authenticated()
                    .requestMatchers(antMatcher(GET, MANAGEMENT_TASK_LIST_COLUMN_URL)).hasAuthority(ADMIN)
                    .requestMatchers(antMatcher(POST, MANAGEMENT_TASK_LIST_COLUMN_URL)).hasAuthority(ADMIN)
                    .requestMatchers(antMatcher(PUT, "$MANAGEMENT_TASK_LIST_COLUMN_URL/{columnKey}")).hasAuthority(ADMIN)
                    .requestMatchers(antMatcher(DELETE, "$MANAGEMENT_TASK_LIST_COLUMN_URL/{columnKey}")).hasAuthority(ADMIN)
                    .requestMatchers(antMatcher(POST, MANAGEMENT_TAB_URL)).hasAuthority(ADMIN)
                    .requestMatchers(antMatcher(PUT, MANAGEMENT_TAB_URL)).hasAuthority(ADMIN)
                    .requestMatchers(antMatcher(PUT, "$MANAGEMENT_TAB_URL/{tabKey}")).hasAuthority(ADMIN)
                    .requestMatchers(antMatcher(DELETE, "$MANAGEMENT_TAB_URL/{tabKey}")).hasAuthority(ADMIN)
                    .requestMatchers(antMatcher(GET, "$MANAGEMENT_TAB_URL/{tabKey}")).hasAuthority(ADMIN)
                    .requestMatchers(antMatcher(GET, MANAGEMENT_TAB_URL)).hasAuthority(ADMIN)
                    .requestMatchers(antMatcher(GET, "/api/management/v1/case/{caseDefinitionName}/{caseDefinitionVersion}/export")).hasAuthority(ADMIN)
                    .requestMatchers(antMatcher(POST, "/api/management/v1/case/import")).hasAuthority(ADMIN)
                    .requestMatchers(antMatcher(GET, "$MANAGEMENT_WIDGET_TAB_URL/{tabKey}")).hasAuthority(ADMIN)
                    .requestMatchers(antMatcher(POST, "$MANAGEMENT_WIDGET_TAB_URL/{tabKey}")).hasAuthority(ADMIN)
                    .requestMatchers(antMatcher(GET, "$DOCUMENT_WIDGET_TAB_URL/{tabKey}")).hasAuthority(USER)
                    .requestMatchers(antMatcher(GET, "$DOCUMENT_WIDGET_TAB_URL/{tabKey}/widget/{widgetKey}")).hasAuthority(USER)
            }
        } catch (e: Exception) {
            throw HttpConfigurerConfigurationException(e)
        }
    }

    companion object {
        private const val LIST_COLUMN_URL = "/api/v1/case/{caseDefinitionName}/list-column"
        private const val CASE_COLUMN_URL = "/api/v1/case/{caseDefinitionName}/task-list-column"
        private const val MANAGEMENT_CASE_LIST_COLUMN_URL = "/api/management/v1/case/{caseDefinitionName}/list-column"
        private const val MANAGEMENT_TASK_LIST_COLUMN_URL = "/api/management/v1/case/{caseDefinitionName}/task-list-column"
        private const val MANAGEMENT_TAB_URL = "/api/management/v1/case-definition/{caseDefinitionName}/tab"
        private const val MANAGEMENT_WIDGET_TAB_URL = "/api/management/v1/case-definition/{caseDefinitionName}/widget-tab"
        private const val DOCUMENT_WIDGET_TAB_URL = "/api/v1/document/{documentId}/widget-tab"
    }
}
