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

        assertThat(dbUpdatedSearchListColumn.title).isEqualTo(updatedSearchListColumn.title)

        val dbLookUpByOwnerId = searchListColumnService.findByOwnerId(searchListColumn.ownerId)
        assertThat(dbLookUpByOwnerId).isNotNull
        assertThat(dbLookUpByOwnerId?.path).isEqualTo(searchListColumn.path)

        searchListColumnService.delete(dbUpdatedSearchListColumn.ownerId, dbUpdatedSearchListColumn.key)

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