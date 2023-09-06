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

@Transactional
class DocumentWidgetDataSourceIntTest @Autowired constructor(
    private val documentWidgetDataSource: DocumentWidgetDataSource
): BaseIntegrationTest() {

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
}