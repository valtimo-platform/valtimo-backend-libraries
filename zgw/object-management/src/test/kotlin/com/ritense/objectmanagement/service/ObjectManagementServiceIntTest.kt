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

package com.ritense.objectmanagement.service

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.ritense.objectenapi.client.Comparator.EQUAL_TO
import com.ritense.objectenapi.client.ObjectSearchParameter
import com.ritense.objectmanagement.BaseIntegrationTest
import com.ritense.objectmanagement.domain.ObjectManagement
import com.ritense.objectmanagement.domain.search.SearchRequestValue
import com.ritense.objectmanagement.domain.search.SearchWithConfigFilter
import com.ritense.objectmanagement.domain.search.SearchWithConfigRequest
import com.ritense.plugin.service.PluginService
import com.ritense.search.domain.DataType
import com.ritense.search.domain.DisplayType
import com.ritense.search.domain.EmptyDisplayTypeParameter
import com.ritense.search.domain.FieldType
import com.ritense.search.domain.SearchFieldV2
import com.ritense.search.domain.SearchListColumn
import com.ritense.search.service.SearchFieldV2Service
import com.ritense.search.service.SearchListColumnService
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import java.net.URI
import java.util.UUID
import javax.transaction.Transactional

@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class ObjectManagementServiceIntTest : BaseIntegrationTest() {

    @Autowired
    lateinit var objectManagementService: ObjectManagementService

    @Autowired
    lateinit var searchFieldV2Service: SearchFieldV2Service

    @Autowired
    lateinit var searchListColumnService: SearchListColumnService

    @Autowired
    lateinit var pluginService: PluginService

    lateinit var mockApi: MockWebServer

    @BeforeAll
    fun setUp() {
        mockApi = MockWebServer()
        mockApi.start(port = 47797)
    }

    @AfterAll
    fun tearDown() {
        mockApi.shutdown()
    }

    @Test
    fun `objectManagementConfiguration can be created`() {
        val objectManagement = createObjectManagement()
        assertThat(objectManagement).isNotNull
    }

    @Test
    fun `should get by id`() {
        val objectManagement = createObjectManagement()
        val toReviewObjectManagement = objectManagementService.getById(objectManagement.id)
        assertThat(objectManagement.id).isEqualTo(toReviewObjectManagement?.id)
        assertThat(objectManagement.title).isEqualTo(toReviewObjectManagement?.title)
        assertThat(objectManagement.objecttypenApiPluginConfigurationId)
            .isEqualTo(toReviewObjectManagement?.objecttypenApiPluginConfigurationId)
    }

    @Test
    fun `should get all`() {
        val test1 = createObjectManagement("test1")
        val test2 = createObjectManagement("test2")

        val objectManagementList = objectManagementService.getAll()
        assertThat(objectManagementList).contains(test1, test2)
    }

    @Test
    fun `delete by id`() {
        val objectManagement = createObjectManagement()

        objectManagementService.deleteById(objectManagement.id)

        val objectManagementList = objectManagementService.getAll()
        assertThat(!objectManagementList.contains(objectManagement))
    }

    @Test
    fun `get objects from objects api with search fields`() {
        val objectUrl = mockApi.url("/some-object").toString()
        val objectTypesApiUrl = mockApi.url("/some-objectTypesApi").toString()

        val authenticationPlugin = pluginService.createPluginConfiguration(
            title = "Objecten authentication",
            properties = ObjectMapper().readTree(
                """{"token":"some-secret-token-long"},
                    "pluginDefinition": {
                    "key": "objecttokenauthentication",
                    "title": "Object Token Authentication",
                    "description": "Plugin used to provide authentication based on a token"
                    }"""
            ) as ObjectNode,
            pluginDefinitionKey = "objecttokenauthentication"
        )

        val objectApiPlugin = pluginService.createPluginConfiguration(
            title = "objectsApi",
            properties = ObjectMapper().readTree(
                """{
                    "url":"$objectUrl",
                    "authenticationPluginConfiguration":"${authenticationPlugin.id.id}"},
                    "pluginDefinition":
                    {
                    "key":"objectenapi",
                    "title":"Objecten API",
                    "description":"Connects to the Objecten API"
                    }
                    }"""
            ) as ObjectNode,
            pluginDefinitionKey = "objectenapi"
        )

        val objectTypeApiPlugin = pluginService.createPluginConfiguration(
            title = "objectTypenApi",
            properties = ObjectMapper().readTree(
                """{
                    "url":"$objectTypesApiUrl",
                    "authenticationPluginConfiguration":"${authenticationPlugin.id.id}"},
                    "pluginDefinition":
                    {
                    "key":"objecttypenapi",
                    "title":"Objecttypen API",
                    "description":"Connects to the Objecttypen API"
                    }
                    }"""
            ) as ObjectNode,
            pluginDefinitionKey = "objecttypenapi"
        )

        val objectManagement = objectManagementService.create(
            ObjectManagement(
                title = "test",
                objectenApiPluginConfigurationId = objectApiPlugin.id.id,
                objecttypeId = UUID.randomUUID().toString(),
                objecttypenApiPluginConfigurationId = objectTypeApiPlugin.id.id
            )
        )

        searchFieldV2Service.create(
            SearchFieldV2(
                ownerId = objectManagement.id.toString(),
                key = "property1",
                title = "property1",
                path = "/property1",
                order = 1,
                dataType = DataType.TEXT,
                fieldType = FieldType.SINGLE
            )
        )

        searchListColumnService.create(
            SearchListColumn(
                ownerId = objectManagement.id.toString(),
                key = "property1",
                title = "property1",
                path = "/property1",
                displayType = DisplayType("type", EmptyDisplayTypeParameter()),
                sortable = false,
                order = 1
            )
        )

        val responseBody = """
            {
              "count": 2,
              "next": "next.url",
              "previous": "previous.url",
              "results": [{
                  "url": "http://example.com",
                  "uuid": "095be615-a8ad-4c33-8e9c-c7612fbf6c9f",
                  "type": "http://example.com",
                  "record": {
                    "index": 0,
                    "typeVersion": 32767,
                    "data": {
                      "property1": "henk",
                      "property2": 123
                    },
                    "geometry": {
                      "type": "string",
                      "coordinates": [
                        0,
                        0
                      ]
                    },
                    "startAt": "2019-08-24",
                    "endAt": "2019-08-25",
                    "registrationAt": "2019-08-26",
                    "correctionFor": "string",
                    "correctedBy": "string2"
                  }
              }]
            }
        """.trimIndent()

        mockApi.enqueue(mockResponse(responseBody))

        val otherFilters = SearchWithConfigFilter(
            key = "property1", null, null, listOf(SearchRequestValue("henk"))
        )

        val searchWithConfigRequest =
            SearchWithConfigRequest(listOf(otherFilters))

        val objects = objectManagementService.getObjectsWithSearchParams(
            searchWithConfigRequest, objectManagement.id, PageRequest.of(0, 10)
        )

        assertThat(objects.content.size).isEqualTo(1)
        assertThat(objects.first().items[0].key).isEqualTo("property1")
        val value = objects.first().items[0].value as JsonNode
        assertThat(value.asText()).isEqualTo("henk")

    }

    @Test
    fun `get objects from objects api with search field parameters`() {
        mockApi.enqueue(
            mockResponse(
                """
                {
                  "count": 1,
                  "next": "next.url",
                  "previous": "previous.url",
                  "results": [{
                      "url": "https://example.com/123",
                      "uuid": "095be615-a8ad-4c33-8e9c-c7612fbf6c9f",
                      "type": "https://example.com/qwe",
                      "record": {
                        "index": 0,
                        "typeVersion": 32767,
                        "data": {
                          "property1": "henk",
                          "property2": 123
                        },
                        "geometry": {
                          "type": "string",
                          "coordinates": [
                            0,
                            0
                          ]
                        },
                        "startAt": "2019-08-24",
                        "endAt": "2019-08-25",
                        "registrationAt": "2019-08-26",
                        "correctionFor": "string",
                        "correctedBy": "string2"
                      }
                  }]
                }
                """.trimIndent()
            )
        )
        val objectManagement = objectManagementService.getByTitle("My Object Management")!!
        val searchParameters = listOf(ObjectSearchParameter("property1", EQUAL_TO, "henk"))

        val objects = objectManagementService.getObjectsWithSearchParams(
            objectManagement,
            searchParameters,
            PageRequest.of(0, 10)
        )

        assertThat(objects.content.size).isEqualTo(1)
        assertThat(objects.first().url).isEqualTo(URI("https://example.com/123"))
        assertThat(objects.first().record.data.toString()).isEqualTo("""{"property1":"henk","property2":123}""")
    }

    private fun createObjectManagement(title: String? = null): ObjectManagement =
        objectManagementService.create(
            ObjectManagement(
                title = title ?: "test",
                objectenApiPluginConfigurationId = UUID.randomUUID(),
                objecttypeId = UUID.randomUUID().toString(),
                objecttypenApiPluginConfigurationId = UUID.randomUUID()
            )
        )

    private fun mockResponse(body: String): MockResponse {
        return MockResponse()
            .addHeader("Content-Type", "application/json")
            .setBody(body)
    }
}