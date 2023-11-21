package com.valtimo.keycloak.web.rest

import com.valtimo.keycloak.service.KeycloakRoleService
import mu.KotlinLogging
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.ws.rs.NotFoundException

@RestController
@RequestMapping("/api/keycloak/role")
class KeycloakRoleResource(
    private val keycloakRoleService: KeycloakRoleService
) {

    @GetMapping()
    fun getRoles(): ResponseEntity<Any> {
        return try {
        val result = keycloakRoleService.findRoles()
        ResponseEntity.ok(result)
        } catch (e: NotFoundException) {
            logger.debug("Could not find realm roles: ${e.message}")
            ResponseEntity.notFound().build()
        }
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}