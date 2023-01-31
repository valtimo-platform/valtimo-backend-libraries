/*
 * Copyright 2015-2022 Ritense BV, the Netherlands.
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
import com.ritense.search.domain.SearchField
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional

@Transactional
internal class SearchFieldIntTest : BaseIntegrationTest() {

    @Autowired
    lateinit var searchFieldService: SearchFieldService

    @Test
    fun `CRUD test`() {
        val searchField = createSearchField()
        assertThat(searchField).isNotNull

        val updatedSearchField = searchField.copy(title = "New Title")
        val dbUpdatedSearchField = searchFieldService.update(
            updatedSearchField.ownerId,
            updatedSearchField.key,
            updatedSearchField
        )

        assertThat(dbUpdatedSearchField.title).isEqualTo(updatedSearchField.title)

        val dbLookUpByOwnerId = searchFieldService.findByOwnerId(searchField.ownerId)
        assertThat(dbLookUpByOwnerId).isNotNull
        assertThat(dbLookUpByOwnerId?.path).isEqualTo(searchField.path)

        searchFieldService.delete(dbUpdatedSearchField.ownerId, dbUpdatedSearchField.key)

    }


    private fun createSearchField(ownerId: String? = null): SearchField =
        searchFieldService.create(
            SearchField(
                ownerId = ownerId ?: "I own this",
                key = "the magic key",
                title = "Title",
                path = "everywhere",
                order = 1,
                dataType = DataType.TEXT,
                fieldType = FieldType.RANGE
            )
        )
}