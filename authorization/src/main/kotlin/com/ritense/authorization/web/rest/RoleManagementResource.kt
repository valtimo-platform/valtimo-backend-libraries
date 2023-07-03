package com.ritense.authorization.web.rest

import com.ritense.authorization.Role
import com.ritense.authorization.RoleRepository
import com.ritense.valtimo.contract.domain.ValtimoMediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/management", produces = [ValtimoMediaType.APPLICATION_JSON_UTF8_VALUE])
class RoleManagementResource (
    val roleRepository: RoleRepository
) {
    @GetMapping("/v1/roles")
    fun getPluginDefinitions()
        : ResponseEntity<List<Role>> {
        return ResponseEntity.ok(roleRepository.findAll())
    }
}