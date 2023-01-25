package com.ritense.search.service

import com.ritense.search.BaseIntegrationTest
import com.ritense.search.domain.DataType
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
        val dbUpdatedSearchListColumn = searchListColumnService.update(updatedSearchListColumn)

        assertThat(dbUpdatedSearchListColumn.title).isNotEqualTo(searchListColumn.title)
        assertThat(dbUpdatedSearchListColumn.title).isEqualTo(updatedSearchListColumn.title)

        val dbLookUpByKey = searchListColumnService.findByKey(searchListColumn.key)
        assertThat(dbLookUpByKey).isNotNull
        assertThat(dbLookUpByKey?.ownerId).isEqualTo(searchListColumn.ownerId)

        val secondSearchListColumn = createSearchListColumn("a new key")

        val searchListColumnList = searchListColumnService.getAll()
        assertThat(searchListColumnList.size).isEqualTo(2)

        searchListColumnService.delete(secondSearchListColumn)
        searchListColumnService.delete(dbUpdatedSearchListColumn)

        val emptyList = searchListColumnService.getAll()
        assertThat(emptyList).isNull()

    }


    private fun createSearchListColumn(key: String? = null): SearchListColumn =
        searchListColumnService.create(
            SearchListColumn(
                ownerId = "I own this",
                key = key ?: "the magic key",
                title = "Title",
                path = "everywhere",
                dataType = DataType.TEXT,
                sortable = false,
                order = 1
            )
        )
}