package com.ritense.authorization.web.rest

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.ritense.authorization.BaseIntegrationTest
import com.ritense.authorization.web.rest.request.PermissionAvailableRequest
import com.ritense.authorization.web.rest.request.PermissionContext
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

class PermissionResourceIT: BaseIntegrationTest() {

    @Autowired
    lateinit var webApplicationContext: WebApplicationContext

    lateinit var mockMvc: MockMvc

    @BeforeEach
    fun beforeEach() {
        mockMvc = MockMvcBuilders
            .webAppContextSetup(webApplicationContext)
            .build()
    }

    @Test
    fun `z`() {

        val permissionRequests = listOf(
            PermissionAvailableRequest(
                "com.ritense.authorization.testimpl.TestEntity",
                "VIEW",
                PermissionContext(
                    "com.ritense.authorization.testimpl.TestEntity",
                    "!23"
                )
            )
        )

        mockMvc.perform(
            MockMvcRequestBuilders
                .post("/api/v1/permissions")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(jacksonObjectMapper().writeValueAsString(permissionRequests))
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isOk)
    }

    @Test
    fun `requesting permission for not existing resource return 403 forbidden`() {

        val permissionRequests = listOf(
            PermissionAvailableRequest(
                "test",
                "update",
                PermissionContext(
                    "test",
                    "!23"
                )
            )
        )

        mockMvc.perform(
            MockMvcRequestBuilders
                .post("/api/v1/permissions")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(jacksonObjectMapper().writeValueAsString(permissionRequests))
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isForbidden)
    }
}