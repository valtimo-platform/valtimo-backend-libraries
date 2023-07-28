/*
 * Copyright 2015-2023 Ritense BV, the Netherlands.
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

package com.ritense.dashboard

import com.ritense.dashboard.domain.DefaultDisplayTypes
import com.ritense.dashboard.datasource.WidgetDataSource
import com.ritense.dashboard.datasource.dto.DashboardWidgetListDto
import com.ritense.dashboard.datasource.dto.DashboardWidgetSingleDto

class TestDataSource {

    @WidgetDataSource(
        key = "test-key-multi",
        title = "Test title multi",
        displayTypes = [DefaultDisplayTypes.NUMBER]
    )
    fun testDashboardWidgetListDto(): DashboardWidgetListDto {
        return DashboardWidgetListDto(emptyList(), 0)
    }

    @WidgetDataSource(
        key = "test-key-single",
        title = "Test title single",
        displayTypes = [DefaultDisplayTypes.NUMBER, "custom"]
    )
    fun dashboardWidgetSingleDto() = DashboardWidgetSingleDto(1, 0)

    fun testNonAnnotatedMethod(): String {
        return "test"
    }

}