/*
 * Copyright 2015-2025 Ritense BV, the Netherlands.
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

package com.ritense.widget.interactivetable

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.TextNode
import com.ritense.valtimo.contract.json.MapperSingleton
import com.ritense.valueresolver.ValueResolverPropertyKey.Companion.ID
import com.ritense.valueresolver.ValueResolverPropertyKey.Companion.IKO_DATA_AGGREGATE_KEY
import com.ritense.valueresolver.ValueResolverPropertyKey.Companion.PAGEABLE
import com.ritense.valueresolver.ValueResolverPropertyKey.Companion.TAB_KEY
import com.ritense.valueresolver.ValueResolverPropertyKey.Companion.WIDGET_KEY
import com.ritense.valueresolver.ValueResolverService
import com.ritense.widget.exception.InvalidCollectionException
import com.ritense.widget.exception.InvalidCollectionNodeTypeException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class InteractiveTableWidgetDataProviderTest(
    @Mock private val valueResolverService: ValueResolverService
) {

    private val widgetDataProvider = InteractiveTableWidgetDataProvider(MapperSingleton.get(), valueResolverService)
    private val objectMapper = MapperSingleton.get()

    @Test
    fun `should resolve data`() {
        val widget = testWidget()
        val properties = mapOf(
            ID to "id",
            IKO_DATA_AGGREGATE_KEY to "ikoDataAggregateKey",
            TAB_KEY to "tabKey",
            WIDGET_KEY to "widgetKey",
            PAGEABLE to Pageable.ofSize(widget.properties.defaultPageSize),
        )
        val people = people()
        mockCollection(widget, people)

        val firstPage = widgetDataProvider.getData(widget, properties)
        JSONAssert.assertEquals(
            """
            {
              "resolved": {},
              "data": {
                "content": [
                  {
                    "firstName": "John",
                    "lastName": "Doe",
                    "real": false,
                    "age": 30,
                    "partnerFirstName": "Jane",
                    "partnerLastName": "Doe",
                    "partnerReal": true,
                    "partnerAge": 25
                  },
                  {
                    "firstName": "John",
                    "lastName": "Doe",
                    "real": false,
                    "age": 30,
                    "partnerFirstName": null,
                    "partnerLastName": null,
                    "partnerReal": null,
                    "partnerAge": null
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
            }
        """.trimIndent(), objectMapper.writeValueAsString(firstPage), JSONCompareMode.STRICT_ORDER
        )
        val propertiesSecondPage = HashMap(properties)
        propertiesSecondPage[PAGEABLE] = Pageable.ofSize(widget.properties.defaultPageSize).withPage(1)
        val secondPage = widgetDataProvider.getData(widget, propertiesSecondPage)
        JSONAssert.assertEquals(
            """
            {
              "resolved": {},
              "data": {
                "content": [
                  {
                    "firstName": null,
                    "lastName": null,
                    "real": null,
                    "age": null,
                    "partnerFirstName": null,
                    "partnerLastName": null,
                    "partnerReal": null,
                    "partnerAge": null
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
            }
        """.trimIndent(), objectMapper.writeValueAsString(secondPage), JSONCompareMode.STRICT_ORDER
        )
    }

    @Test
    fun `should resolve data when JsonNode collection is resolved`() {
        val widget = testWidget()
        val properties = mapOf(
            ID to "id",
            IKO_DATA_AGGREGATE_KEY to "ikoDataAggregateKey",
            TAB_KEY to "tabKey",
            WIDGET_KEY to "widgetKey",
            PAGEABLE to Pageable.ofSize(widget.properties.defaultPageSize),
        )
        val collection = objectMapper.valueToTree<JsonNode>(listOf(john()))
        mockCollection(widget, collection)

        val page = widgetDataProvider.getData(widget, properties)["data"] as Page<Map<String, Any?>>
        assertThat(page.content.first()).containsEntry("firstName", "John")
    }

    @Test
    fun `should resolve data when resolved field value is a JsonNode`() {
        val widget = testWidget()
        val properties = mapOf(
            ID to "id",
            IKO_DATA_AGGREGATE_KEY to "ikoDataAggregateKey",
            TAB_KEY to "tabKey",
            WIDGET_KEY to "widgetKey",
            PAGEABLE to Pageable.ofSize(widget.properties.defaultPageSize),
        )
        val collection = listOf(
            mapOf(
                "firstName" to TextNode.valueOf("John"),
            )
        )
        mockCollection(widget, collection)

        val page = widgetDataProvider.getData(widget, properties)["data"] as Page<Map<String, Any?>>
        assertThat(page.content.first()).containsEntry("firstName", "John")
    }

    @Test
    fun `should throw error if collection placeholder value is not a collection`() {
        val widget = testWidget()
        val properties = mapOf(
            ID to "id",
            IKO_DATA_AGGREGATE_KEY to "ikoDataAggregateKey",
            TAB_KEY to "tabKey",
            WIDGET_KEY to "widgetKey",
            PAGEABLE to Pageable.ofSize(widget.properties.defaultPageSize),
        )
        mockCollection(widget, "justAString")

        assertThrows<InvalidCollectionException> {
            widgetDataProvider.getData(widget, properties)
        }
    }

    @Test
    fun `should throw error if collection node is not an object`() {
        val widget = testWidget()
        val properties = mapOf(
            ID to "id",
            IKO_DATA_AGGREGATE_KEY to "ikoDataAggregateKey",
            TAB_KEY to "tabKey",
            WIDGET_KEY to "widgetKey",
            PAGEABLE to Pageable.ofSize(widget.properties.defaultPageSize).withPage(1),
        )
        val collection = people() + listOf("")
        mockCollection(widget, collection)

        assertThrows<InvalidCollectionNodeTypeException> {
            widgetDataProvider.getData(widget, properties)
        }
    }

    @Test
    fun `should return empty page when resolved collection is null`() {
        val widget = testWidget()
        val properties = mapOf(
            ID to "id",
            IKO_DATA_AGGREGATE_KEY to "ikoDataAggregateKey",
            TAB_KEY to "tabKey",
            WIDGET_KEY to "widgetKey",
            PAGEABLE to Pageable.ofSize(1),
        )
        mockCollection(widget, null)

        val data = widgetDataProvider.getData(widget, properties)["data"] as Page<Map<String, Any?>>

        assertThat(data.content.size).isZero()
        assertThat(data.number).isEqualTo(0)
        assertThat(data.totalPages).isEqualTo(0)
    }

    @Test
    fun `should return empty page when page number is unavailable`() {
        val widget = testWidget()
        val properties = mapOf(
            ID to "id",
            IKO_DATA_AGGREGATE_KEY to "ikoDataAggregateKey",
            TAB_KEY to "tabKey",
            WIDGET_KEY to "widgetKey",
            PAGEABLE to Pageable.ofSize(widget.properties.defaultPageSize).withPage(2),
        )
        val collection = people()
        mockCollection(widget, collection)

        val data = widgetDataProvider.getData(widget, properties)["data"] as Page<Map<String, Any?>>

        assertThat(data.content.size).isZero()
        assertThat(data.number).isEqualTo(2)
        assertThat(data.totalPages).isEqualTo(2)
    }

    private fun testWidget() = InteractiveTableWidget(
        id = UUID.fromString("3ab43f1a-0154-4658-82b8-41527def0ae2"),
        key = "key",
        title = "Test",
        order = 0,
        width = 1,
        highContrast = true,
        actions = emptyList(),
        properties = InteractiveTableWidgetProperties(
            collection = "test:someCollection",
            defaultPageSize = 2,
            columns = testColumns()
        )
    )

    private fun testColumns() = listOf(
        InteractiveTableWidgetProperties.Column("firstName", "", "$.firstName"),
        InteractiveTableWidgetProperties.Column("lastName", "", "/lastName"),
        InteractiveTableWidgetProperties.Column("real", "", "real"),
        InteractiveTableWidgetProperties.Column("age", "", "$.age"),
        InteractiveTableWidgetProperties.Column("partnerFirstName", "", "$.partner.firstName"),
        InteractiveTableWidgetProperties.Column("partnerLastName", "", "/partner/lastName"),
        InteractiveTableWidgetProperties.Column("partnerReal", "", "partner/real"),
        InteractiveTableWidgetProperties.Column("partnerAge", "", "$.partner.age"),
    )

    private fun mockCollection(widget: InteractiveTableWidget, collectionValue: Any?) {
        whenever(
            valueResolverService.resolveValues(
                any<Map<String, Any>>(),
                eq(listOf(widget.properties.collection))
            )
        ).thenReturn(
            mapOf(widget.properties.collection to collectionValue)
        )
    }

    private fun people() = listOf(
        john(partner = jane()),
        john(partner = Person()),
        Person()
    )

    private fun john(
        firstName: String? = "John",
        lastName: String? = "Doe",
        real: Boolean? = false,
        age: Int? = 30,
        partner: Person? = null
    ): Person {
        return Person(firstName, lastName, real, age, partner)
    }

    private fun jane(
        firstName: String? = "Jane",
        lastName: String? = "Doe",
        real: Boolean? = true,
        age: Int? = 25,
        partner: Person? = null
    ): Person {
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