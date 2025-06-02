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

package com.ritense.case_.deployment

import com.ritense.BaseIntegrationTest
import com.ritense.authorization.AuthorizationContext.Companion.runWithoutAuthorization
import com.ritense.case_.repository.CaseWidgetTabRepository
import com.ritense.case_.service.CaseWidgetTabService
import com.ritense.case_.widget.fields.FieldsCaseWidgetDto
import com.ritense.valtimo.contract.case_.CaseDefinitionId
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional

@Transactional
class CaseWidgetTabDeployerIntTest @Autowired constructor(
    private val caseWidgetTabService: CaseWidgetTabService,
    private val caseWidgetTabRepository: CaseWidgetTabRepository
) : BaseIntegrationTest() {

    @Test
    fun `should auto deploy cases`() {
        val caseDefinitionId = CaseDefinitionId.of("some-other-case-type", "1.1.1")
        val tabKey = "widget-tab"

        val widgetTab = runWithoutAuthorization {
            caseWidgetTabService.getWidgetTab(caseDefinitionId, tabKey)
        }

        assertThat(widgetTab).isNotNull
        assertThat(widgetTab!!.widgets).hasSize(2)
        assertThat(widgetTab.widgets[0].key).isEqualTo("test-widget-2")
        assertThat(widgetTab.widgets[1].key).isEqualTo("test-widget-1")
        assertThat(widgetTab.widgets[0].title).isEqualTo("Widget 2")
        assertThat(widgetTab.widgets[1].title).isEqualTo("Widget 1")
        assertThat(widgetTab.widgets[0].width).isEqualTo(2)
        assertThat(widgetTab.widgets[1].width).isEqualTo(1)
        assertThat(widgetTab.widgets[0].highContrast).isTrue()
        assertThat(widgetTab.widgets[1].highContrast).isFalse()
        assertThat((widgetTab.widgets[0] as FieldsCaseWidgetDto).properties.columns).hasSize(1)
        assertThat((widgetTab.widgets[1] as FieldsCaseWidgetDto).properties.columns).hasSize(2)
    }
}