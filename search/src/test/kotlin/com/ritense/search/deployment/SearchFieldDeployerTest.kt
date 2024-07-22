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

package com.ritense.search.deployment

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.ritense.search.domain.DataType
import com.ritense.search.domain.FieldType
import com.ritense.search.domain.SearchFieldMatchType
import com.ritense.search.domain.SearchFieldV2
import com.ritense.search.repository.SearchFieldV2Repository
import com.ritense.search.service.SearchFieldV2Service
import com.ritense.search.web.rest.dto.SearchFieldV2Dto
import com.ritense.valtimo.changelog.service.ChangelogService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.UUID

class SearchFieldDeployerTest {

    lateinit var testDeployer: SearchFieldDeployer

    lateinit var objectMapper: ObjectMapper
    lateinit var changelogService: ChangelogService
    lateinit var repository: SearchFieldV2Repository
    lateinit var searchFieldService: SearchFieldV2Service

    @BeforeEach
    fun setUp() {
        objectMapper = ObjectMapper().registerKotlinModule()
        changelogService = mock()
        repository = mock()
        searchFieldService = mock()

        testDeployer = TestSearchFieldDeployer(
            objectMapper,
            changelogService,
            repository,
            searchFieldService,
            true
        )
    }

    @Test
    fun `should delete searchfields of same type when clearTables is true`() {
        testDeployer.before()

        verify(repository).deleteAllByOwnerType("some-owner-type")
        verify(changelogService).deleteChangesetsByKey("some-change-set")
    }

    @Test
    fun `should not delete searchfields of same type when clearTables is false`() {
        val deployer = TestSearchFieldDeployer(
            objectMapper,
            changelogService,
            repository,
            searchFieldService,
            false
        )

        deployer.before()

        verify(repository, never()).deleteAllByOwnerType("some-owner-type")
        verify(changelogService, never()).deleteChangesetsByKey("some-change-set")
    }

    @Test
    fun `should return changeset with correct details`() {
        val content = """
            {
                "changesetId": "some-changeset-id",
                "collection": [
                    {
                        "ownerId": "test-case",
                        "searchFields": [
                            {
                                "title": "Voornaam",
                                "key": "voornaam",
                                "path": "doc:voornaam",
                                "dataType": "text",
                                "fieldType": "text_contains",
                                "matchType": "like"
                            }
                        ]
                    }
                ]
            }
        """.trimIndent()

        val changesets = testDeployer.getChangelogDetails("filename", content)

        assertEquals(1, changesets.size)
        val changeset = changesets[0]
        assertEquals("some-changeset-id", changeset.changesetId)
        assertEquals("some-change-set", changeset.key)
        assertTrue(changeset.valueToChecksum is List<*>)
    }

    @Test
    fun `should deploy changeset with new field`() {
        val content = """
            {
                "changesetId": "some-changeset-id",
                "collection": [
                    {
                        "ownerId": "test-case",
                        "searchFields": [
                            {
                                "title": "Voornaam",
                                "key": "voornaam",
                                "path": "doc:voornaam",
                                "dataType": "text",
                                "fieldType": "text_contains",
                                "matchType": "like"
                            }
                        ]
                    }
                ]
            }
        """.trimIndent()

        val changesets = testDeployer.getChangelogDetails("filename", content)
        changesets[0].deploy()

        val dtoCaptor = argumentCaptor<SearchFieldV2Dto>()

        verify(repository).findByOwnerTypeAndOwnerIdAndKeyOrderByOrder("some-owner-type", "test-case", "voornaam")
        verify(searchFieldService).create(dtoCaptor.capture())

        val dto = dtoCaptor.firstValue
        assertEquals("test-case", dto.ownerId)
        assertEquals("some-owner-type", dto.ownerType)
        assertEquals("voornaam", dto.key)
        assertEquals("Voornaam", dto.title)
        assertEquals("doc:voornaam", dto.path)
        assertEquals(0, dto.order)
        assertEquals(DataType.TEXT, dto.dataType)
        assertEquals(FieldType.TEXT_CONTAINS, dto.fieldType)
        assertEquals(SearchFieldMatchType.LIKE, dto.matchType)
    }

    @Test
    fun `should deploy changeset with existing field`() {
        val content = """
            {
                "changesetId": "some-changeset-id",
                "collection": [
                    {
                        "ownerId": "test-case",
                        "searchFields": [
                            {
                                "title": "Voornaam",
                                "key": "voornaam",
                                "path": "doc:voornaam",
                                "dataType": "text",
                                "fieldType": "text_contains",
                                "matchType": "like"
                            }
                        ]
                    }
                ]
            }
        """.trimIndent()

        val existingMock = mock<SearchFieldV2>()
        val id = UUID.randomUUID()
        whenever(existingMock.id).thenReturn(id)
        whenever(existingMock.key).thenReturn("voornaam")
        whenever(repository.findByOwnerTypeAndOwnerIdAndKeyOrderByOrder("some-owner-type", "test-case", "voornaam"))
            .thenReturn(existingMock)

        val changesets = testDeployer.getChangelogDetails("filename", content)
        changesets[0].deploy()

        val dtoCaptor = argumentCaptor<SearchFieldV2Dto>()

        verify(repository).findByOwnerTypeAndOwnerIdAndKeyOrderByOrder("some-owner-type", "test-case", "voornaam")
        verify(searchFieldService).update(eq("test-case"), eq("voornaam"), dtoCaptor.capture())

        val dto = dtoCaptor.firstValue
        assertEquals("test-case", dto.ownerId)
        assertEquals("some-owner-type", dto.ownerType)
        assertEquals("voornaam", dto.key)
        assertEquals("Voornaam", dto.title)
        assertEquals("doc:voornaam", dto.path)
        assertEquals(0, dto.order)
        assertEquals(DataType.TEXT, dto.dataType)
        assertEquals(FieldType.TEXT_CONTAINS, dto.fieldType)
        assertEquals(SearchFieldMatchType.LIKE, dto.matchType)
    }

    class TestSearchFieldDeployer(
        objectMapper: ObjectMapper,
        changelogService: ChangelogService,
        repository: SearchFieldV2Repository,
        searchFieldService: SearchFieldV2Service,
        clearTables: Boolean,
    ) : SearchFieldDeployer(objectMapper, changelogService, repository, searchFieldService, clearTables) {
        override fun getPath(): String {
            return "some-path"
        }

        override fun ownerTypeKey(): String {
            return "some-owner-type"
        }

        override fun changeSetKey(): String {
            return "some-change-set"
        }
    }
}