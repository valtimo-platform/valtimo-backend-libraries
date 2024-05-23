package com.ritense.case_.widget.table

import com.ritense.case_.domain.tab.CaseWidgetTab
import com.ritense.valtimo.contract.json.MapperSingleton
import com.ritense.valueresolver.ValueResolverService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.data.domain.Pageable
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class TableCaseWidgetDataProviderTest(
    @Mock private val valueResolverService: ValueResolverService
) {

    private val caseWidgetDataProvider = TableCaseWidgetDataProvider(MapperSingleton.get(), valueResolverService)


    @Test
    fun `should resolve data`() {
        val widgetTab = mock<CaseWidgetTab>()
        val widget = TableCaseWidget(
            "test", "Test", 0, 1, true, TableWidgetProperties(
                collection = "test:someCollection",
                defaultPageSize = 5,
                columns = listOf(
                    TableWidgetProperties.Column(
                        "firstName", "Firstname", "$.firstName"
                    ),
                    TableWidgetProperties.Column(
                        "lastName", "Lastname", "/lastName"
                    ),
                    TableWidgetProperties.Column(
                        "partner", "Partner", "partner"
                    )
                )
            )
        )
        val documentId = UUID.randomUUID()
        whenever(valueResolverService.resolveValues(documentId.toString(), listOf(widget.properties.collection))).thenReturn(
            mapOf(
                widget.properties.collection to listOf(
                    mapOf(
                        "firstName" to "John",
                        "lastName" to "Doe",
//                        "partner" to "Jane Doe",
                    )
                )
            )
        )

        val data = caseWidgetDataProvider.getData(documentId, widgetTab, widget, Pageable.ofSize(widget.properties.defaultPageSize))
        assertThat(data).isNotNull
    }
}