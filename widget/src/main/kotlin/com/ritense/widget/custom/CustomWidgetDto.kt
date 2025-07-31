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

package com.ritense.widget.custom

import com.fasterxml.jackson.annotation.JsonTypeName
import com.ritense.widget.domain.WidgetAction
import com.ritense.widget.domain.Widget
import com.ritense.widget.web.rest.dto.WidgetDto
import jakarta.validation.Valid
import java.util.UUID

@JsonTypeName("custom")
data class CustomWidgetDto(
    override val key: String,
    override val title: String,
    override val width: Int,
    override val highContrast: Boolean,
    override val actions: List<WidgetAction> = emptyList(),
    @field:Valid val properties: CustomWidgetProperties
) : WidgetDto {
    override fun toEntity(id: UUID, order: Int): Widget = CustomWidget(
        id = id,
        key = key,
        title = title,
        width = width,
        order = order,
        highContrast = highContrast,
        actions = actions,
        properties = properties,
    )
}