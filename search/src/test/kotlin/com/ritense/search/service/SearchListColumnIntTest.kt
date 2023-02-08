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
import com.ritense.search.domain.DisplayType
import com.ritense.search.domain.EmptyDisplayTypeParameter
import com.ritense.search.domain.SearchListColumn
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional

@Transactional
internal class SearchListColumnIntTest : BaseIntegrationTest() {

    @Autowired
    lateinit var searchListColumnService: SearchListColumnService

    @Test
    fun `CRUD test`() {
        val searchListColumn = createSearchListColumn()
        assertThat(searchListColumn).isNotNull

        val updatedSearchListColumn = searchListColumn.copy(title = "New Title")
        val dbUpdatedSearchListColumn = searchListColumnService.update(
            updatedSearchListColumn.ownerId,
            updatedSearchListColumn.key,
            updatedSearchListColumn
        )

        assertThat(dbUpdatedSearchListColumn?.title).isEqualTo(updatedSearchListColumn.title)

        val dbLookUpByOwnerId = searchListColumnService.findByOwnerId(searchListColumn.ownerId)
        assertThat(dbLookUpByOwnerId).isNotNull
        assertThat(dbLookUpByOwnerId?.first()?.path).isEqualTo(searchListColumn.path)

        dbUpdatedSearchListColumn?.ownerId?.let { searchListColumnService.delete(it, dbUpdatedSearchListColumn.key) }

        val list = searchListColumnService.findByOwnerId(searchListColumn.ownerId)

        assertThat(list).isEmpty()
    }


    private fun createSearchListColumn(ownerId: String? = null): SearchListColumn =
         searchListColumnService.create(
            SearchListColumn(
                ownerId = ownerId ?: "I own this",
                key = "the magic key",
                title = "Title",
                path = "everywhere",
                displayType = DisplayType("type", EmptyDisplayTypeParameter()),
                sortable = false,
                order = 1
            )
        )
}