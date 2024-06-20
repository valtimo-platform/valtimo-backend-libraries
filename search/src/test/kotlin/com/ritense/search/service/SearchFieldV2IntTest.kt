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

package com.ritense.search.service

import com.ritense.search.BaseIntegrationTest
import com.ritense.search.domain.DataType
import com.ritense.search.domain.FieldType
import com.ritense.search.domain.SearchFieldV2
import com.ritense.search.web.rest.dto.LegacySearchFieldV2Dto
import com.ritense.search.web.rest.dto.SearchFieldV2Dto
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional

@Transactional
internal class SearchFieldV2IntTest : BaseIntegrationTest() {

    @Autowired
    lateinit var searchFieldV2Service: SearchFieldV2Service

    @Test
    fun `CRUD test`() {
        val searchField = createSearchField()
        assertThat(searchField).isNotNull

        val updatedSearchField = searchField.copy(title = "New Title")
        val updatedSearchFieldDto = LegacySearchFieldV2Dto(
            id = updatedSearchField.id,
            ownerId = updatedSearchField.ownerId,
            key = updatedSearchField.key,
            title = updatedSearchField.title,
            path = updatedSearchField.path,
            order = updatedSearchField.order,
            dataType = updatedSearchField.dataType,
            fieldType = updatedSearchField.fieldType,
            matchType = updatedSearchField.matchType,
            dropdownDataProvider = updatedSearchField.dropdownDataProvider
        )
        val dbUpdatedSearchField = searchFieldV2Service.update(
            updatedSearchField.ownerId,
            updatedSearchField.key,
            updatedSearchFieldDto
        )

        assertThat(dbUpdatedSearchField?.title).isEqualTo(updatedSearchField.title)

        val dbLookUpByOwnerId = searchFieldV2Service.findAllByOwnerId(searchField.ownerId)
        assertThat(dbLookUpByOwnerId).isNotNull
        assertThat(dbLookUpByOwnerId?.first()?.path).isEqualTo(searchField.path)

        dbUpdatedSearchField?.ownerId?.let { searchFieldV2Service.delete(it, dbUpdatedSearchField.key) }

        val list = searchFieldV2Service.findAllByOwnerId(searchField.ownerId)

        assertThat(list).isEmpty()
    }


    private fun createSearchField(ownerId: String? = null): SearchFieldV2 =
        searchFieldV2Service.create(
            LegacySearchFieldV2Dto(
                ownerId = ownerId ?: "I own this",
                key = "the magic key",
                title = "Title",
                path = "everywhere",
                order = 1,
                dataType = DataType.TEXT,
                fieldType = FieldType.RANGE,
                matchType = null,
                dropdownDataProvider = null
            )
        )
}