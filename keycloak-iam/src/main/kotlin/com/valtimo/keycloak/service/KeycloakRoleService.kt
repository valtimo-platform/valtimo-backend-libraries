package com.valtimo.keycloak.service

import org.keycloak.admin.client.resource.RolesResource

class KeycloakRoleService(
    private val keycloakService: KeycloakService,
) {

    fun findRoles(): RolesResource {
        val keycloak = keycloakService.keycloak()
        return keycloakService.realmRolesResource(keycloak)
    }
}