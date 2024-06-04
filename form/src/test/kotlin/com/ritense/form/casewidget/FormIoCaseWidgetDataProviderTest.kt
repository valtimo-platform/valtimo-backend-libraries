package com.ritense.form.casewidget

import com.ritense.form.domain.FormIoFormDefinition
import com.ritense.form.service.FormDefinitionService
import com.ritense.form.service.PrefillFormService
import com.ritense.valtimo.contract.json.MapperSingleton
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.data.domain.Pageable
import java.util.Optional
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class FormIoCaseWidgetDataProviderTest(
    @Mock val formDefinitionService: FormDefinitionService,
    @Mock val formService: PrefillFormService
) {

    private val dataProvider = FormIoCaseWidgetDataProvider(formDefinitionService, formService)

    @Test
    fun `should return a prefilled form definition`() {
        val documentId = UUID.randomUUID()
        val formDefinitionName = "myForm"

        val formDefinition: FormIoFormDefinition = mock()
        val formDefinitionId = UUID.randomUUID()
        whenever(formDefinition.id).thenReturn(formDefinitionId)

        whenever(formDefinitionService.getFormDefinitionByName(formDefinitionName)).thenReturn(Optional.of(formDefinition))
        whenever(formService.getPrefilledFormDefinition(formDefinitionId, documentId)).thenReturn(formDefinition)

        whenever(formDefinition.asJson()).thenReturn("""
            {
                "x": true,
                "y": false
            }
            """.trimIndent().toJsonNode())

        val data = dataProvider.getData(
            documentId, mock(), FormIoCaseWidget(
                "k", "t", 0, 4, false, FormIoWidgetProperties(
                    formDefinitionName
                )
            ), Pageable.unpaged()
        )!!
        assertThat(data.at("/x").booleanValue()).isEqualTo(true)
        assertThat(data.at("/y").booleanValue()).isEqualTo(false)
    }

    @Test
    fun `should return null when form definition cannot be found`() {
        val documentId = UUID.randomUUID()
        val formDefinitionName = "myForm"

        whenever(formDefinitionService.getFormDefinitionByName(formDefinitionName)).thenReturn(Optional.empty())

        val data = dataProvider.getData(
            documentId, mock(), FormIoCaseWidget(
                "k", "t", 0, 4, false, FormIoWidgetProperties(
                    formDefinitionName
                )
            ), Pageable.unpaged()
        )
        assertThat(data).isNull()
    }

    private fun String.toJsonNode() = MapperSingleton.get().readTree(this)
}