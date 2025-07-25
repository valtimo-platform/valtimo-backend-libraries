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

import com.ritense.valtimo.contract.annotation.AllOpen
import com.ritense.widget.domain.DeprecatedStartProcessWidgetAction
import com.ritense.widget.domain.Widget
import com.ritense.widget.domain.WidgetTopRightCorner
import io.hypersistence.utils.hibernate.type.json.JsonType
import jakarta.persistence.Column
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity
import org.hibernate.annotations.Type
import java.util.UUID

@AllOpen
@Entity
@DiscriminatorValue("custom")
class CustomWidget(
    id: UUID = UUID.randomUUID(),
    key: String,
    title: String,
    order: Int,
    width: Int,
    highContrast: Boolean,
    actions: List<DeprecatedStartProcessWidgetAction> = emptyList(),
    topRightCorner: WidgetTopRightCorner? = null,

    @Type(value = JsonType::class)
    @Column(name = "properties", nullable = false)
    val properties: CustomWidgetProperties
) : Widget(
    id, key, title, order, width, highContrast, actions, topRightCorner
) {
    override fun copy(
        id: UUID,
        key: String,
        title: String,
        order: Int,
        width: Int,
        highContrast: Boolean,
        actions: List<DeprecatedStartProcessWidgetAction>,
        topRightCorner: WidgetTopRightCorner?,
    ) = CustomWidget(
        id = id,
        key = key,
        title = title,
        order = order,
        width = width,
        highContrast = highContrast,
        actions = actions,
        topRightCorner = topRightCorner,
        properties = properties,
    )

    override fun toDto() = CustomWidgetDto(
        key = this.key,
        title = this.title,
        width = this.width,
        highContrast = this.highContrast,
        actions = this.actions,
        topRightCorner = this.topRightCorner,
        properties = this.properties,
    )
}