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

package com.ritense.case_.widget.mapper

import com.ritense.case_.domain.tab.CaseWidgetTabWidget
import com.ritense.case_.domain.tab.TestCaseWidgetTabWidget
import com.ritense.case_.rest.dto.CaseWidgetTabWidgetDto
import com.ritense.case_.web.rest.dto.TestCaseWidgetTabWidgetDto
import com.ritense.case_.widget.CaseWidgetMapper

class TestCaseWidgetMapper: CaseWidgetMapper {
    override fun toEntity(dto: CaseWidgetTabWidgetDto, index: Int): CaseWidgetTabWidget? {
        return if (dto is TestCaseWidgetTabWidgetDto) {
            TestCaseWidgetTabWidget(
                dto.key,
                dto.title,
                index,
                dto.width,
                dto.highContrast
            )
        } else {
            null
        }
    }

    override fun toDto(entity: CaseWidgetTabWidget): CaseWidgetTabWidgetDto? {
        return if (entity is TestCaseWidgetTabWidget) {
            TestCaseWidgetTabWidgetDto(
                entity.key,
                entity.title,
                entity.width,
                entity.highContrast
            )
        } else {
            null
        }

    }
}