package com.ritense.document.dashboard

import com.ritense.document.BaseIntegrationTest
import com.ritense.document.domain.impl.JsonDocumentContent
import com.ritense.document.domain.impl.JsonSchemaDocumentDefinition
import com.ritense.document.domain.impl.request.NewDocumentRequest
import com.ritense.document.service.result.CreateDocumentResult
import com.ritense.valtimo.contract.repository.ExpressionOperator
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable

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
        val allByName = documentService.getAllByDocumentDefinitionName(Pageable.unpaged(), documentDefinitionName)
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
                    "/street",
                    ExpressionOperator.EQUAL_TO,
                    street
                )
            )
        )
        val result = documentWidgetDataSource.getCaseCount(properties)
        assertThat(result.value).isEqualTo(expectedCount.toLong())
    }

    private fun createDocument(documentDefinition: JsonSchemaDocumentDefinition, street: String = "Funenpark"): CreateDocumentResult? {
        val content = JsonDocumentContent("""{"street": "$street"}""")
        return documentService.createDocument(
            NewDocumentRequest(
                documentDefinition.id().name(),
                content.asJson()
            )
        )
    }
}