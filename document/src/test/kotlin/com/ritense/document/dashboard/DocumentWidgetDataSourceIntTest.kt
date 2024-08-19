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

package com.ritense.document.dashboard

import com.ritense.authorization.AuthorizationContext.Companion.runWithoutAuthorization
import com.ritense.document.BaseIntegrationTest
import com.ritense.document.domain.impl.JsonDocumentContent
import com.ritense.document.domain.impl.JsonSchemaDocumentDefinition
import com.ritense.document.domain.impl.request.NewDocumentRequest
import com.ritense.document.service.result.CreateDocumentResult
import com.ritense.valtimo.contract.Constants
import com.ritense.valtimo.contract.repository.ExpressionOperator
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable
import org.springframework.transaction.annotation.Transactional
import java.util.Collections

@Transactional
class DocumentWidgetDataSourceIntTest @Autowired constructor(
    private val documentWidgetDataSource: DocumentWidgetDataSource
) : BaseIntegrationTest() {

    @Test
    fun `should count by documentDefinitionName`() {
        val definition = definition()

        val expectedCount = 3
        repeat(expectedCount) {
            createDocument(definition)
        }

        val documentDefinitionName = definition.id().name()
        val result = documentWidgetDataSource.getCaseCount(DocumentCountDataSourceProperties(documentDefinitionName))
        assertThat(result.value).isGreaterThanOrEqualTo(expectedCount.toLong())

        //There might be remnants of other tests (should not be), so were checking against the documentService as well.
        val allByName = runWithoutAuthorization { documentService.getAllByDocumentDefinitionName(Pageable.unpaged(), documentDefinitionName) }
        assertThat(result.value).isEqualTo(allByName.totalElements)
    }

    @Test
    fun `should count by documentDefinitionName and criteria`() {
        val definition = definition()

        val street = "Sesame Street"
        val expectedCount = 3
        repeat(expectedCount) {
            createDocument(definition, street)
        }
        createDocument(definition)

        val documentDefinitionName = definition.id().name()

        val properties = DocumentCountDataSourceProperties(
            documentDefinitionName,
            listOf(
                QueryCondition(
                    "case:createdBy",
                    ExpressionOperator.EQUAL_TO,
                    Constants.SYSTEM_ACCOUNT
                ),
                QueryCondition(
                    "doc:street",
                    ExpressionOperator.EQUAL_TO,
                    street
                ),
                QueryCondition(
                    "housenumber",
                    ExpressionOperator.EQUAL_TO,
                    1
                )
            )
        )
        val result = documentWidgetDataSource.getCaseCount(properties)
        assertThat(result.value).isEqualTo(expectedCount.toLong())
    }

    @Test
    fun `should count by documentDefinitionName and null criteria`() {
        documentRepository.deleteAll()
        val definition = definition()

        val street = "Sesame Street"
        repeat(3) {
            createDocument(definition, street)
        }
        createDocument(definition)

        val documentDefinitionName = definition.id().name()

        val properties = DocumentCountDataSourceProperties(
            documentDefinitionName,
            null
        )
        val result = documentWidgetDataSource.getCaseCount(properties)
        assertThat(result.value).isEqualTo(4)
    }

    @Test
    fun `should group by documentDefinitionName, use criteria and resolve enum`() {
        documentRepository.deleteAll()
        val definition = definition()

        val street1 = "Sesame Street"

        repeat(2) {
            createDocument(definition, street1)
        }

        val street2 = "Main Street"

        repeat(3) {
            createDocument(definition, street2)
        }

        val street3 = "3rd Street"

        repeat(5) {
            createDocument(definition, street3)
        }

        val documentDefinitionName = definition.id().name()

        val openSesame = "Open sesame"

        val properties = DocumentGroupByDataSourceProperties(
            documentDefinitionName,
            path = "doc:street",
            listOf(
                QueryCondition(
                    "doc:street",
                    ExpressionOperator.NOT_EQUAL_TO,
                    street3
                )
            ),
            mapOf(street1 to openSesame)
        )

        val result = documentWidgetDataSource.getCaseGroupBy(properties)
        val resultValues = result.values.sortedBy { it.label }

        assertThat(resultValues.size).isEqualTo(2)
        assertThat(resultValues[1].value).isEqualTo(2)
        assertThat(resultValues[1].label).isEqualTo(openSesame)
        assertThat(resultValues[0].value).isEqualTo(3)
        assertThat(resultValues[0].label).isEqualTo(street2)
    }

    @Test
    fun `should resolve multiple case counts for a documentDefinitionName`() {
        documentRepository.deleteAll()
        val definition = definition()

        val street1 = "Sesame Street"

        repeat(2) {
            createDocument(definition, street1)
        }

        val street2 = "Main Street"

        repeat(3) {
            createDocument(definition, street2)
        }

        val street3 = "3rd Street"

        repeat(5) {
            createDocument(definition, street3)
        }

        val documentDefinitionName = definition.id().name()

        val livingOnSesameStreet = "Living on Sesame Street"
        val livingOnMainStreet = "Living on Main Street"
        val livingOn3rdStreet = "Living on 3rd Street"

        val properties = DocumentCountsDataSourceProperties(
            documentDefinitionName,
            queryItems = listOf(
                DocumentCountsQueryItem(
                    livingOnSesameStreet,
                    listOf(QueryCondition(
                        "doc:street",
                        ExpressionOperator.EQUAL_TO,
                        street1
                    ))

                ),
                DocumentCountsQueryItem(
                    livingOnMainStreet,
                    listOf(QueryCondition(
                        "doc:street",
                        ExpressionOperator.EQUAL_TO,
                        street2
                    ))

                ),
                DocumentCountsQueryItem(
                    livingOn3rdStreet,
                    listOf(QueryCondition(
                        "doc:street",
                        ExpressionOperator.EQUAL_TO,
                        street3
                    ))

                )
            )

        )

        val result = documentWidgetDataSource.getCaseCounts(properties)

        assertThat(result.values.size).isEqualTo(3)
        assertThat(result.values[0].label).isEqualTo(livingOnSesameStreet)
        assertThat(result.values[0].value).isEqualTo(2)
        assertThat(result.values[1].label).isEqualTo(livingOnMainStreet)
        assertThat(result.values[1].value).isEqualTo(3)
        assertThat(result.values[2].label).isEqualTo(livingOn3rdStreet)
        assertThat(result.values[2].value).isEqualTo(5)
    }

    @Test
    fun `should support by local date time in criteria`() {
        documentRepository.deleteAll()

        val definition = definition()

        val street = "Sesame Street"

        createDocument(definition, street)

        val documentDefinitionName = definition.id().name()

        val properties = DocumentCountDataSourceProperties(
            documentDefinitionName,
            listOf(
                QueryCondition(
                    "case:createdOn",
                    ExpressionOperator.GREATER_THAN,
                    "\${localDateTimeNow.plusMinutes(1)}"
                ),
                QueryCondition(
                    "doc:street",
                    ExpressionOperator.EQUAL_TO,
                    street
                ),
            )
        )

        val properties2 = DocumentCountDataSourceProperties(
            documentDefinitionName,
            listOf(
                QueryCondition(
                    "case:createdOn",
                    ExpressionOperator.GREATER_THAN,
                    "\${localDateTimeNow.minusMinutes(1)}"
                ),
                QueryCondition(
                    "doc:street",
                    ExpressionOperator.EQUAL_TO,
                    street
                ),
            )
        )

        val result1 = documentWidgetDataSource.getCaseCount(properties)
        val result2 = documentWidgetDataSource.getCaseCount(properties2)

        assertThat(result1.value).isEqualTo(0)
        assertThat(result1.total).isEqualTo(1)
        assertThat(result2.value).isEqualTo(1)
        assertThat(result2.total).isEqualTo(1)
    }

    @Test
    fun `should filter out null values when using group by`() {
        documentRepository.deleteAll()
        val definition = definition()

        val street1 = "Sesame Street"

        repeat(2) {
            createDocument(definition, street1)
        }

        val street2 = "Back street"


        repeat(4) {
            createDocument(definition, street2)
        }

        repeat(3) {
            createDocumentWithNullValue(definition)
        }

        val documentDefinitionName = definition.id().name()

        val properties = DocumentGroupByDataSourceProperties(
            documentDefinitionName,
            path = "doc:street",
            null,
            null
        )

        val result = documentWidgetDataSource.getCaseGroupBy(properties)

        assertThat(result.values.size).isEqualTo(2)
    }

    private fun createDocument(documentDefinition: JsonSchemaDocumentDefinition, street: String = "Funenpark"): CreateDocumentResult? {
        val content = JsonDocumentContent("""{"street": "$street", "housenumber": 1}""")
        return runWithoutAuthorization {
            documentService.createDocument(
                NewDocumentRequest(
                    documentDefinition.id().name(),
                    content.asJson()
                )
            )
        }
    }

    private fun createDocumentWithNullValue(documentDefinition: JsonSchemaDocumentDefinition): CreateDocumentResult? {
        val content = JsonDocumentContent("""{"street": null, "housenumber": 1}""")
        return runWithoutAuthorization {
            documentService.createDocument(
                NewDocumentRequest(
                    documentDefinition.id().name(),
                    content.asJson()
                )
            )
        }
    }
}