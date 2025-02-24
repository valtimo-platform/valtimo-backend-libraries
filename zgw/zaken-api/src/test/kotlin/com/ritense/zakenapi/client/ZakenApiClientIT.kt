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

package com.ritense.zakenapi.client

import com.ritense.authorization.permission.ConditionContainer
import com.ritense.authorization.permission.Permission
import com.ritense.authorization.permission.PermissionRepository
import com.ritense.authorization.role.Role
import com.ritense.authorization.role.RoleRepository
import com.ritense.resource.authorization.ResourcePermission
import com.ritense.resource.authorization.ResourcePermissionActionProvider
import com.ritense.zakenapi.BaseIntegrationTest
import com.ritense.zakenapi.ZakenApiPlugin
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.transaction.annotation.Transactional
import java.net.URI
import java.util.UUID
import kotlin.test.assertEquals

@Transactional
internal class ZakenApiClientIT @Autowired constructor(
    private val zakenApiClient: ZakenApiClient,
    private val roleRepository: RoleRepository,
    private val permissionRepository: PermissionRepository,
) : BaseIntegrationTest() {

    lateinit var server: MockWebServer
    lateinit var zakenApiPlugin: ZakenApiPlugin
    lateinit var roleTest: Role

    @BeforeEach
    internal fun setUp() {
        server = MockWebServer()
        setupMockZakenApiServer()
        server.start(port = 56273)
        server.shutdown()
        server = MockWebServer()
        setupMockZakenApiServer()
        server.start(port = 56273)

        zakenApiPlugin = pluginService.createInstance("3079d6fe-42e3-4f8f-a9db-52ce2507b7ee")

        roleTest = roleRepository.findByKey("ROLE_TEST")!!
        permissionRepository.deleteByRoleKeyIn(listOf("ROLE_TEST"))
    }

    @AfterEach
    internal fun tearDown() {
        server.shutdown()
    }

    //@Test
    @WithMockUser(authorities = ["ROLE_TEST"])
    fun `should allow zaak-document link`() {
        val permissions = listOf(
            Permission(
                UUID.randomUUID(),
                ResourcePermission::class.java,
                ResourcePermissionActionProvider.CREATE,
                ConditionContainer(),
                roleTest
            )
        )
        permissionRepository.saveAllAndFlush(permissions)

        zakenApiClient.linkDocument(
            zakenApiPlugin.authenticationPluginConfiguration,
            zakenApiPlugin.url,
            LinkDocumentRequest(
                informatieobject = "https://localhost:56273/documenten/informatieobject/1234",
                zaak = "https://localhost:56273/zaken/1234",
                titel = "titel",
                beschrijving = "beschrijving",
            )
        )
    }

    @Test
    @WithMockUser(authorities = ["ROLE_TEST"])
    fun `should not allow zaak-document link when missing permission`() {
        assertThrows<AccessDeniedException> {
            zakenApiClient.linkDocument(
                zakenApiPlugin.authenticationPluginConfiguration,
                zakenApiPlugin.url,
                LinkDocumentRequest(
                    informatieobject = "https://localhost:56273/documenten/informatieobject/1234",
                    zaak = "https://localhost:56273/zaken/1234",
                    titel = "titel",
                    beschrijving = "beschrijving",
                )
            )
        }
    }

    @Test
    @WithMockUser(authorities = ["ROLE_TEST"])
    fun `should allow zaak-document list`() {
        val permissions = listOf(
            Permission(
                UUID.randomUUID(),
                ResourcePermission::class.java,
                ResourcePermissionActionProvider.VIEW_LIST,
                ConditionContainer(),
                roleTest
            )
        )
        permissionRepository.saveAllAndFlush(permissions)

        val results = zakenApiClient.getZaakInformatieObjecten(
            zakenApiPlugin.authenticationPluginConfiguration,
            zakenApiPlugin.url,
            zaakUrl = URI("https://localhost:56273/zaken/1234"),
        )

        assertEquals(1, results.count())
    }

    @Test
    @WithMockUser(authorities = ["ROLE_TEST"])
    fun `should respond with empty zaak-document list when missing permission`() {
        val results = zakenApiClient.getZaakInformatieObjecten(
            zakenApiPlugin.authenticationPluginConfiguration,
            zakenApiPlugin.url,
            zaakUrl = URI("https://localhost:56273/zaken/1234"),
        )

        assertEquals(0, results.count())
    }

    private fun setupMockZakenApiServer() {
        val dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                return when (request.method + " " + request.path?.substringBefore('?')) {
                    "POST /zaken/zaakinformatieobjecten" -> handleLinkDocumentRequest()
                    "GET /zaken/zaakinformatieobjecten" -> handleListDocumentRequest()
                    else -> MockResponse().setResponseCode(404)
                }
            }
        }
        server.dispatcher = dispatcher
    }

    private fun handleLinkDocumentRequest(zone: String = "Z"): MockResponse {
        val body = """
            {
                "url": "https://example.com",
                "uuid": "095be615-a8ad-4c33-8e9c-c7612fbf6c9f",
                "informatieobject": "https://example.com",
                "zaak": "https://example.com",
                "aardRelatieWeergave": "Hoort bij, omgekeerd: kent",
                "titel": "string",
                "beschrijving": "string",
                "registratiedatum": "2019-08-24T14:15:22Z"
            }
        """.trimIndent()
        return mockResponse(body)
    }

    private fun handleListDocumentRequest(): MockResponse {
        val body = """
            [
                {
                  "url": "https://example.com",
                  "uuid": "095be615-a8ad-4c33-8e9c-c7612fbf6c9f",
                  "informatieobject": "https://example.com",
                  "zaak": "https://example.com",
                  "aardRelatieWeergave": "Hoort bij, omgekeerd: kent",
                  "titel": "string",
                  "beschrijving": "string",
                  "registratiedatum": "2019-08-24T14:15:22Z"
                }
            ]
        """.trimIndent()
        return mockResponse(body)
    }
}
