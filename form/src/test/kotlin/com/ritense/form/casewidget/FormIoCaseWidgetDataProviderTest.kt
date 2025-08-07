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

package com.ritense.form.casewidget

import com.ritense.case_.domain.tab.CaseWidgetTab
import com.ritense.case_.domain.tab.CaseWidgetTabWidgetId
import com.ritense.form.domain.FormIoFormDefinition
import com.ritense.form.service.FormDefinitionService
import com.ritense.form.service.PrefillFormService
import com.ritense.valtimo.contract.json.MapperSingleton
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Answers
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
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

        whenever(formDefinitionService.getFormDefinitionByName(eq(formDefinitionName), any())).thenReturn(Optional.of(formDefinition))
        whenever(formService.getPrefilledFormDefinition(formDefinitionId, documentId)).thenReturn(formDefinition)

        whenever(formDefinition.asJson()).thenReturn("""
            {
                "x": true,
                "y": false
            }
            """.trimIndent().toJsonNode())

        val data = dataProvider.getData(
            documentId, mock(defaultAnswer = Answers.RETURNS_DEEP_STUBS), FormIoCaseWidget(
                CaseWidgetTabWidgetId("k"), "t", 0, 4, false, emptyList(), FormIoWidgetProperties(
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
            documentId, mock(defaultAnswer = Answers.RETURNS_DEEP_STUBS), FormIoCaseWidget(
                CaseWidgetTabWidgetId("k"), "t", 0, 4, false, emptyList(), FormIoWidgetProperties(
                    formDefinitionName
                )
            ), Pageable.unpaged()
        )
        assertThat(data).isNull()
    }

    private fun String.toJsonNode() = MapperSingleton.get().readTree(this)
}