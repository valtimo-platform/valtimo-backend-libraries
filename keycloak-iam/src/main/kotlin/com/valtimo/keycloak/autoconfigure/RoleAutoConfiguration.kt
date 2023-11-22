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

package com.valtimo.keycloak.autoconfigure

import com.valtimo.keycloak.security.config.RoleSecurityConfigurer
import com.valtimo.keycloak.service.KeycloakRoleService
import com.valtimo.keycloak.service.KeycloakService
import com.valtimo.keycloak.service.RoleService
import com.valtimo.keycloak.web.rest.RoleResource
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order

@Configuration
internal class RoleAutoConfiguration {

    @Bean
    @Order(408)
    @ConditionalOnMissingBean(RoleSecurityConfigurer::class)
    fun roleSecurityConfigurer(): RoleSecurityConfigurer = RoleSecurityConfigurer()

    @Bean
    fun keycloakRoleService(
        keycloakService: KeycloakService
    ): RoleService = KeycloakRoleService(
        keycloakService = keycloakService
    )

    @Bean
    fun roleResource(
        roleService: RoleService
    ): RoleResource = RoleResource(
        roleService = roleService
    )
}