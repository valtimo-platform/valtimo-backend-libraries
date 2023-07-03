package com.ritense.authorization.web.rest

import com.ritense.authorization.Role
import com.ritense.authorization.RoleRepository
import com.ritense.valtimo.contract.authentication.AuthoritiesConstants
import com.ritense.valtimo.web.rest.SecuritySpecificEndpointIntegrationTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpMethod.GET
import org.springframework.http.HttpStatus
import org.springframework.security.test.context.support.WithMockUser

class RoleManagementResourceIntTest : SecuritySpecificEndpointIntegrationTest() {
    @Autowired
    lateinit var roleRepository: RoleRepository

    @BeforeEach
    fun setUp() {
    }

    @Test
    @WithMockUser(authorities = [AuthoritiesConstants.ADMIN])
    fun `should have access to method with role_admin`() {
        assertHttpStatus(GET, "/api/management/v1/roles", HttpStatus.OK)
    }

    @Test
    @WithMockUser(authorities = [AuthoritiesConstants.USER])
    fun `should not access to method without role_admin`() {
        roleRepository.save(Role("role_admin"))
        assertHttpStatus(GET, "/api/management/v1/roles", HttpStatus.FORBIDDEN)
    }
}