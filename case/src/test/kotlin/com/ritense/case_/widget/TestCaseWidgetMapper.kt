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

package com.ritense.case_.widget

import com.ritense.case_.domain.tab.CaseWidgetTabWidgetId
import com.ritense.case_.domain.tab.TestCaseWidgetTabWidget
import com.ritense.case_.web.rest.dto.TestCaseWidgetTabWidgetDto

class TestCaseWidgetMapper : CaseWidgetMapper<TestCaseWidgetTabWidget, TestCaseWidgetTabWidgetDto> {

    override fun toEntity(dto: TestCaseWidgetTabWidgetDto, index: Int): TestCaseWidgetTabWidget {
        return TestCaseWidgetTabWidget(
            CaseWidgetTabWidgetId(dto.key),
            dto.title,
            index,
            dto.width,
            dto.highContrast,
            dto.properties
        )
    }

    override fun toDto(entity: TestCaseWidgetTabWidget): TestCaseWidgetTabWidgetDto {
        return TestCaseWidgetTabWidgetDto(
            entity.id.key,
            entity.title,
            entity.width,
            entity.highContrast,
            entity.properties
        )
    }
}