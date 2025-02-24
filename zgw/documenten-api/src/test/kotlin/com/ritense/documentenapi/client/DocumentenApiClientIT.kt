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

package com.ritense.documentenapi.client

import com.ritense.authorization.permission.ConditionContainer
import com.ritense.authorization.permission.Permission
import com.ritense.authorization.permission.PermissionRepository
import com.ritense.authorization.role.Role
import com.ritense.authorization.role.RoleRepository
import com.ritense.documentenapi.BaseIntegrationTest
import com.ritense.documentenapi.DocumentenApiPlugin
import com.ritense.documentenapi.web.rest.dto.DocumentSearchRequest
import com.ritense.resource.authorization.ResourcePermission
import com.ritense.resource.authorization.ResourcePermissionActionProvider
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.transaction.annotation.Transactional
import java.net.URI
import java.time.LocalDate
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class DocumentenApiClientIT @Autowired constructor(
    private val documentenApiClient: DocumentenApiClient,
    private val roleRepository: RoleRepository,
    private val permissionRepository: PermissionRepository,
) : BaseIntegrationTest() {

    lateinit var server: MockWebServer
    lateinit var documentenApiPlugin: DocumentenApiPlugin
    lateinit var roleTest: Role

    @BeforeAll
    internal fun setUp() {
        server = MockWebServer()
        setupMockDocumentenApiServer()
        server.start(port = 56273)

        documentenApiPlugin = pluginService.createInstance("5474fe57-532a-4050-8d89-32e62ca3e895")

        roleTest = roleRepository.findByKey("ROLE_TEST")!!
    }

    @AfterAll
    internal fun tearDown() {
        server.shutdown()
    }

    @Test
    @WithMockUser(authorities = ["ROLE_TEST"])
    fun `should allow document upload`() {
        val permissions = listOf(
            Permission(
                UUID.randomUUID(),
                ResourcePermission::class.java,
                ResourcePermissionActionProvider.CREATE,
                ConditionContainer(),
                roleTest
            )
        )
        permissionRepository.deleteByRoleKeyIn(listOf("ROLE_TEST"))
        permissionRepository.saveAllAndFlush(permissions)

        documentenApiClient.storeDocument(
            documentenApiPlugin.authenticationPluginConfiguration,
            documentenApiPlugin.url,
            CreateDocumentRequest(
                bronorganisatie = documentenApiPlugin.bronorganisatie,
                creatiedatum = LocalDate.now(),
                titel = "titel",
                auteur = "auteur",
                taal = "nl",
                inhoud = "inhoud".byteInputStream(),
                informatieobjecttype = null,
            )
        )
    }

    @Test
    @WithMockUser(authorities = ["ROLE_TEST"])
    fun `should not allow document upload when missing permission`() {
        permissionRepository.deleteByRoleKeyIn(listOf("ROLE_TEST"))

        assertThrows<AccessDeniedException> {
            documentenApiClient.storeDocument(
                documentenApiPlugin.authenticationPluginConfiguration,
                documentenApiPlugin.url,
                CreateDocumentRequest(
                    bronorganisatie = documentenApiPlugin.bronorganisatie,
                    creatiedatum = LocalDate.now(),
                    titel = "titel",
                    auteur = "auteur",
                    taal = "nl",
                    inhoud = "inhoud".byteInputStream(),
                    informatieobjecttype = null,
                )
            )
        }
    }

    @Test
    @WithMockUser(authorities = ["ROLE_TEST"])
    fun `should allow document list`() {
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

        val results = documentenApiClient.getInformatieObjecten(
            documentenApiPlugin.authenticationPluginConfiguration,
            documentenApiPlugin.url,
            Pageable.ofSize(10),
            DocumentSearchRequest(
                zaakUrl = URI("https://localhost:56273/documenten/1234"),
            )
        )

        assertEquals(1, results.count())
    }

    @Test
    @WithMockUser(authorities = ["ROLE_TEST"])
    fun `should respond with empty zaak-document list when missing permission`() {
        val results = documentenApiClient.getInformatieObjecten(
            documentenApiPlugin.authenticationPluginConfiguration,
            documentenApiPlugin.url,
            Pageable.ofSize(10),
            DocumentSearchRequest(
                zaakUrl = URI("https://localhost:56273/documenten/1234"),
            )
        )

        assertEquals(0, results.count())
    }

    @Test
    @WithMockUser(authorities = ["ROLE_TEST"])
    fun `should allow document download`() {
        val permissions = listOf(
            Permission(
                UUID.randomUUID(),
                ResourcePermission::class.java,
                ResourcePermissionActionProvider.VIEW,
                ConditionContainer(),
                roleTest
            )
        )
        permissionRepository.deleteByRoleKeyIn(listOf("ROLE_TEST"))
        permissionRepository.saveAllAndFlush(permissions)

        val stream = documentenApiClient.downloadInformatieObjectContent(
            documentenApiPlugin.authenticationPluginConfiguration,
            documentenApiPlugin.url,
            "objectId"
        )

        assertNotNull(stream)
    }

    @Test
    @WithMockUser(authorities = ["ROLE_TEST"])
    fun `should not allow document download when missing permission`() {
        permissionRepository.deleteByRoleKeyIn(listOf("ROLE_TEST"))

        assertThrows<AccessDeniedException> {
            documentenApiClient.downloadInformatieObjectContent(
                documentenApiPlugin.authenticationPluginConfiguration,
                documentenApiPlugin.url,
                "objectId"
            )
        }
    }

    @Test
    @WithMockUser(authorities = ["ROLE_TEST"])
    fun `should allow document delete`() {
        val permissions = listOf(
            Permission(
                UUID.randomUUID(),
                ResourcePermission::class.java,
                ResourcePermissionActionProvider.DELETE,
                ConditionContainer(),
                roleTest
            )
        )
        permissionRepository.deleteByRoleKeyIn(listOf("ROLE_TEST"))
        permissionRepository.saveAllAndFlush(permissions)

        documentenApiClient.deleteInformatieObject(
            documentenApiPlugin.authenticationPluginConfiguration,
            URI(documentenApiPlugin.url.toString() + "enkelvoudiginformatieobjecten/objectId"),
        )
    }

    @Test
    @WithMockUser(authorities = ["ROLE_TEST"])
    fun `should not allow document delete when missing permission`() {
        permissionRepository.deleteByRoleKeyIn(listOf("ROLE_TEST"))

        assertThrows<AccessDeniedException> {
            documentenApiClient.deleteInformatieObject(
                documentenApiPlugin.authenticationPluginConfiguration,
                URI(documentenApiPlugin.url.toString() + "enkelvoudiginformatieobjecten/objectId"),
            )
        }
    }

    @Test
    @WithMockUser(authorities = ["ROLE_TEST"])
    fun `should allow document modify`() {
        val permissions = listOf(
            Permission(
                UUID.randomUUID(),
                ResourcePermission::class.java,
                ResourcePermissionActionProvider.MODIFY,
                ConditionContainer(),
                roleTest
            )
        )
        permissionRepository.deleteByRoleKeyIn(listOf("ROLE_TEST"))
        permissionRepository.saveAllAndFlush(permissions)

        documentenApiClient.modifyInformatieObject(
            documentenApiPlugin.authenticationPluginConfiguration,
            URI(documentenApiPlugin.url.toString() + "enkelvoudiginformatieobjecten/objectId"),
            PatchDocumentRequest(
                creatiedatum = LocalDate.now(),
                titel = "titel",
                auteur = "auteur",
                taal = "taal",
            )
        )
    }

    @Test
    @WithMockUser(authorities = ["ROLE_TEST"])
    fun `should not allow document modify when missing permission`() {
        permissionRepository.deleteByRoleKeyIn(listOf("ROLE_TEST"))

        assertThrows<AccessDeniedException> {
            documentenApiClient.modifyInformatieObject(
                documentenApiPlugin.authenticationPluginConfiguration,
                URI(documentenApiPlugin.url.toString() + "enkelvoudiginformatieobjecten/objectId"),
                PatchDocumentRequest(
                    creatiedatum = LocalDate.now(),
                    titel = "titel",
                    auteur = "auteur",
                    taal = "taal",
                )
            )
        }
    }

    private fun setupMockDocumentenApiServer() {
        val dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                return when (request.method + " " + request.path?.substringBefore('?')) {
                    "POST /documenten/enkelvoudiginformatieobjecten" -> handleCreateDocumentRequest()
                    "GET /documenten/enkelvoudiginformatieobjecten" -> handleSearchDocumentRequest()
                    "GET /documenten/enkelvoudiginformatieobjecten/objectId/download" -> handleDocumentDownloadRequest()
                    "GET /documenten/enkelvoudiginformatieobjecten/objectId" -> handleDocumentRequest()
                    "PATCH /documenten/enkelvoudiginformatieobjecten/objectId" -> handleDocumentRequest()
                    "DELETE /documenten/enkelvoudiginformatieobjecten/objectId" -> MockResponse().setResponseCode(204)
                    else -> MockResponse().setResponseCode(404)
                }
            }
        }
        server.dispatcher = dispatcher
    }

    private fun handleCreateDocumentRequest(): MockResponse {
        val body = """
            {
              "url": "${server.url("/")}",
              "auteur": "auteur",
              "bestandsnaam": "bestandsnaam.jpg",
              "bestandsomvang": 0,
              "beginRegistratie": "2019-08-24T14:15:22Z",
              "bestandsdelen": []
            }
        """.trimIndent()
        return mockResponse(body)
    }

    private fun handleSearchDocumentRequest(): MockResponse {
        val body = """
            {
                "count": 1,
                "next": null,
                "previous": null,
                "results": [
                    {
                      "url": "http://example.com",
                      "identificatie": "string",
                      "bronorganisatie": "000000000",
                      "creatiedatum": "2019-08-24",
                      "titel": "string",
                      "vertrouwelijkheidaanduiding": "openbaar",
                      "auteur": "string",
                      "status": "in_bewerking",
                      "formaat": "string",
                      "taal": "str",
                      "versie": 0,
                      "beginRegistratie": "2019-08-24T14:15:22Z",
                      "bestandsnaam": "string",
                      "inhoud": "string",
                      "bestandsomvang": 0,
                      "link": "http://example.com",
                      "beschrijving": "string",
                      "ontvangstdatum": "2019-08-24",
                      "verzenddatum": "2019-08-24",
                      "indicatieGebruiksrecht": true,
                      "ondertekening": {
                        "soort": "analoog",
                        "datum": "2019-08-24"
                      },
                      "integriteit": {
                        "algoritme": "crc_16",
                        "waarde": "string",
                        "datum": "2019-08-24"
                      },
                      "informatieobjecttype": "http://example.com",
                      "locked": true,
                      "bestandsdelen": []
                    }
                ]
            }
        """.trimIndent()
        return mockResponse(body)
    }

    private fun handleDocumentDownloadRequest(): MockResponse {
        return MockResponse()
            .addHeader("Content-Type", "application/octet-stream")
            .setBody("TEST_DOCUMENT_CONTENT")
    }

    private fun handleDocumentRequest(): MockResponse {
        val body = """
           {
             "url": "http://example.com",
             "identificatie": "string",
             "bronorganisatie": "000000000",
             "creatiedatum": "2019-08-24",
             "titel": "string",
             "vertrouwelijkheidaanduiding": "openbaar",
             "auteur": "string",
             "status": "in_bewerking",
             "formaat": "string",
             "taal": "str",
             "versie": 0,
             "beginRegistratie": "2019-08-24T14:15:22Z",
             "bestandsnaam": "string",
             "inhoud": "string",
             "bestandsomvang": 0,
             "link": "http://example.com",
             "beschrijving": "string",
             "ontvangstdatum": "2019-08-24",
             "verzenddatum": "2019-08-24",
             "indicatieGebruiksrecht": true,
             "ondertekening": {
               "soort": "analoog",
               "datum": "2019-08-24"
             },
             "integriteit": {
               "algoritme": "crc_16",
               "waarde": "string",
               "datum": "2019-08-24"
             },
             "informatieobjecttype": "http://example.com",
             "locked": true,
             "bestandsdelen": []
           }
        """.trimIndent()
        return mockResponse(body)
    }
}
