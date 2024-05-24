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
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import org.springframework.data.domain.Pageable
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class TableCaseWidgetDataProviderTest(
    @Mock private val valueResolverService: ValueResolverService
) {

    private val caseWidgetDataProvider = TableCaseWidgetDataProvider(MapperSingleton.get(), valueResolverService)
    private val objectMapper = MapperSingleton.get()

    @Test
    fun `should resolve data`() {
        val widgetTab = mock<CaseWidgetTab>()
        val widget = TableCaseWidget(
            "test", "Test", 0, 1, true, TableWidgetProperties(
                collection = "test:someCollection",
                defaultPageSize = 2,
                columns = listOf(
                    TableWidgetProperties.Column("firstName", "", "$.firstName"),
                    TableWidgetProperties.Column("lastName", "", "/lastName"),
                    TableWidgetProperties.Column("real", "", "real"),
                    TableWidgetProperties.Column("age", "", "$.age"),
                    TableWidgetProperties.Column("partnerFirstName", "", "$.partner.firstName"),
                    TableWidgetProperties.Column("partnerLastName", "", "/partner/lastName"),
                    TableWidgetProperties.Column("partnerReal", "", "partner/real"),
                    TableWidgetProperties.Column("partnerAge", "", "$.partner.age"),
                )
            )
        )
        val documentId = UUID.randomUUID()
        val people = people()
        mockCollection(documentId, widget, people)

        val data = caseWidgetDataProvider.getData(documentId, widgetTab, widget, Pageable.ofSize(widget.properties.defaultPageSize))
        assertThat(data).isNotNull
        val jsonData = objectMapper.writeValueAsString(data)
        JSONAssert.assertEquals("""

        """.trimIndent(), jsonData, JSONCompareMode.STRICT)
    }

    private fun mockCollection(documentId: UUID, widget: TableCaseWidget, collectionValue: Any?) {
        whenever(valueResolverService.resolveValues(documentId.toString(), listOf(widget.properties.collection))).thenReturn(
            mapOf(widget.properties.collection to collectionValue)
        )
    }

    private fun people() = listOf(
        john(partner = jane()),
        john(partner = Person()),
        Person()
    )

    private fun john(firstName: String? = "John", lastName: String? = "Doe", real: Boolean? = false, age: Int? = 30, partner: Person? = null): Person {
        return Person(firstName, lastName, real, age, partner)
    }

    private fun jane(firstName: String? = "Jane", lastName: String? = "Doe", real: Boolean? = true, age: Int? = 25, partner: Person? = null): Person {
        return Person(firstName, lastName, real, age, partner)
    }

    private data class Person(
        val firstName: String? = null,
        val lastName: String? = null,
        val real: Boolean? = null,
        val age: Int? = null,
        val partner: Person? = null
    )
}