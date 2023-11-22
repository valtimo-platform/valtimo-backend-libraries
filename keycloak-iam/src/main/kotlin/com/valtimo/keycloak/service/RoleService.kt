package com.valtimo.keycloak.service

interface RoleService {
    fun findRoles(roleNamePrefix: String?): List<String>
}