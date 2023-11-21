package com.valtimo.keycloak.autoconfigure

import com.valtimo.keycloak.security.config.KeycloakRoleSecurityConfigurer
import com.valtimo.keycloak.service.KeycloakRoleService
import com.valtimo.keycloak.service.KeycloakService
import com.valtimo.keycloak.web.rest.KeycloakRoleResource
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order

@Configuration
internal class KeycloakRoleAutoConfiguration {

    @Bean
    @Order(408)
    @ConditionalOnMissingBean(KeycloakRoleSecurityConfigurer::class)
    fun keycloakRoleSecurityConfigurer(): KeycloakRoleSecurityConfigurer = KeycloakRoleSecurityConfigurer()

    @Bean
    fun keycloakRoleManagementService(
        keycloakService: KeycloakService
    ): KeycloakRoleService = KeycloakRoleService(
        keycloakService = keycloakService
    )

    @Bean
    fun keycloakRoleResource(
        keycloakRoleService: KeycloakRoleService
    ): KeycloakRoleResource = KeycloakRoleResource(
        keycloakRoleService = keycloakRoleService
    )
}