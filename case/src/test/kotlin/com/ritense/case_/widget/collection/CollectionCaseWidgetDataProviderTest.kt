package com.ritense.case_.widget.collection

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.TextNode
import com.ritense.case_.domain.tab.CaseWidgetTab
import com.ritense.case_.domain.tab.CaseWidgetTabWidgetId
import com.ritense.case_.widget.exception.InvalidCollectionException
import com.ritense.case_.widget.exception.InvalidCollectionNodeTypeException
import com.ritense.valtimo.contract.json.MapperSingleton
import com.ritense.valueresolver.ValueResolverService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
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
class CollectionCaseWidgetDataProviderTest(
    @Mock private val valueResolverService: ValueResolverService
) {

    private val caseWidgetDataProvider = CollectionCaseWidgetDataProvider(MapperSingleton.get(), valueResolverService)
    private val objectMapper = MapperSingleton.get()

    @Test
    fun `should resolve data`() {
        val widgetTab = mock<CaseWidgetTab>()
        val widget = testWidget()
        val documentId = UUID.randomUUID()
        val people = people()
        mockCollection(documentId, widget, people)

        val firstPage = caseWidgetDataProvider.getData(documentId, widgetTab, widget, Pageable.ofSize(widget.properties.defaultPageSize))
        JSONAssert.assertEquals("""
            {
              "content": [
                {
                  "title": "John",
                  "fields": {
                    "lastName": "Doe",
                    "real": false,
                    "age": 30,
                    "partnerFirstName": "Jane",
                    "partnerLastName": "Doe",
                    "partnerReal": true,
                    "partnerAge": 25
                  }
                },
                {
                  "title": "John",
                  "fields": {
                    "lastName": "Doe",
                    "real": false,
                    "age": 30,
                    "partnerFirstName": null,
                    "partnerLastName": null,
                    "partnerReal": null,
                    "partnerAge": null
                  }
                }
              ],
              "first": true,
              "last": false,
              "totalPages": 2,
              "totalElements": 3,
              "numberOfElements": 2,
              "size": 2,
              "number": 0,
              "sort": []
            }
        """.trimIndent(), objectMapper.writeValueAsString(firstPage), JSONCompareMode.STRICT_ORDER)
        val secondPage = caseWidgetDataProvider.getData(documentId, widgetTab, widget, Pageable.ofSize(widget.properties.defaultPageSize).withPage(1))
        JSONAssert.assertEquals("""
            {
              "content": [
                {
                  "title": null,
                  "fields": {
                    "lastName": null,
                    "real": null,
                    "age": null,
                    "partnerFirstName": null,
                    "partnerLastName": null,
                    "partnerReal": null,
                    "partnerAge": null
                  }
                }
              ],
              "first": false,
              "last": true,
              "totalPages": 2,
              "totalElements": 3,
              "numberOfElements": 1,
              "size": 2,
              "number": 1,
              "sort": []
            }
        """.trimIndent(), objectMapper.writeValueAsString(secondPage), JSONCompareMode.STRICT_ORDER)
    }

    @Test
    fun `should resolve data when JsonNode collection is resolved`() {
        val widgetTab = mock<CaseWidgetTab>()
        val widget = testWidget()
        val documentId = UUID.randomUUID()
        val collection = objectMapper.valueToTree<JsonNode>(listOf(john()))
        mockCollection(documentId, widget, collection)

        val page = caseWidgetDataProvider.getData(documentId, widgetTab, widget, Pageable.ofSize(widget.properties.defaultPageSize))
        val first = page.content.first()
        assertThat(first.title).isEqualTo("John")
        assertThat(first.fields).containsEntry("lastName", "Doe")
    }

    @Test
    fun `should resolve data when resolved field value is a JsonNode`() {
        val widgetTab = mock<CaseWidgetTab>()
        val widget = testWidget()
        val documentId = UUID.randomUUID()
        val collection = listOf(mapOf(
            "firstName" to TextNode.valueOf("John"),
            "lastName" to TextNode.valueOf("Doe"),
        ))
        mockCollection(documentId, widget, collection)

        val page = caseWidgetDataProvider.getData(documentId, widgetTab, widget, Pageable.ofSize(widget.properties.defaultPageSize))
        val first = page.content.first()
        assertThat(first.title).isEqualTo("John")
        assertThat(first.fields).containsEntry("lastName", "Doe")
    }

    @Test
    fun `should throw error if collection placeholder value is not a collection`() {
        val widgetTab = mock<CaseWidgetTab>()
        val widget = testWidget()
        val documentId = UUID.randomUUID()
        mockCollection(documentId, widget, "justAString")

        assertThrows<InvalidCollectionException> {
            caseWidgetDataProvider.getData(documentId, widgetTab, widget, Pageable.ofSize(widget.properties.defaultPageSize))
        }
    }

    @Test
    fun `should throw error if collection node is not an object`() {
        val widgetTab = mock<CaseWidgetTab>()
        val widget = testWidget()
        val documentId = UUID.randomUUID()
        val collection = people() + listOf("")
        mockCollection(documentId, widget, collection)

        assertThrows<InvalidCollectionNodeTypeException> {
            caseWidgetDataProvider.getData(documentId, widgetTab, widget, Pageable.ofSize(widget.properties.defaultPageSize).withPage(1))
        }
    }

    @Test
    fun `should return empty page when resolved collection is null`() {
        val widgetTab = mock<CaseWidgetTab>()
        val widget = testWidget()
        val documentId = UUID.randomUUID()
        mockCollection(documentId, widget, null)

        val data = caseWidgetDataProvider.getData(documentId, widgetTab, widget, Pageable.ofSize(1))

        assertThat(data.content.size).isZero()
        assertThat(data.number).isEqualTo(0)
        assertThat(data.totalPages).isEqualTo(0)
    }

    @Test
    fun `should return empty page when page number is unavailable`() {
        val widgetTab = mock<CaseWidgetTab>()
        val widget = testWidget()
        val documentId = UUID.randomUUID()
        val collection = people()
        mockCollection(documentId, widget, collection)

        val data = caseWidgetDataProvider.getData(documentId, widgetTab, widget, Pageable.ofSize(widget.properties.defaultPageSize).withPage(2))

        assertThat(data.content.size).isZero()
        assertThat(data.number).isEqualTo(2)
        assertThat(data.totalPages).isEqualTo(2)
    }

    private fun testWidget() = CollectionCaseWidget(
        CaseWidgetTabWidgetId("test"), "Test", 0, 1, true, CollectionWidgetProperties(
            collection = "test:someCollection",
            defaultPageSize = 2,
            title = CollectionWidgetProperties.TitleField("$.firstName"),
            fields = testFields()
        )
    )

    private fun testFields() = listOf(
        CollectionWidgetProperties.Field("lastName", "", "/lastName"),
        CollectionWidgetProperties.Field("real", "", "real"),
        CollectionWidgetProperties.Field("age", "", "$.age"),
        CollectionWidgetProperties.Field("partnerFirstName", "", "$.partner.firstName"),
        CollectionWidgetProperties.Field("partnerLastName", "", "/partner/lastName"),
        CollectionWidgetProperties.Field("partnerReal", "", "partner/real"),
        CollectionWidgetProperties.Field("partnerAge", "", "$.partner.age"),
    )

    private fun mockCollection(documentId: UUID, widget: CollectionCaseWidget, collectionValue: Any?) {
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