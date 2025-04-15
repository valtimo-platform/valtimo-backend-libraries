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

package com.ritense.case_.domain.tab

import com.fasterxml.jackson.annotation.JsonProperty
import com.google.common.base.Objects
import com.ritense.case_.rest.dto.CaseWidgetAction
import com.ritense.valtimo.contract.annotation.AllOpen
import io.hypersistence.utils.hibernate.type.json.JsonType
import jakarta.persistence.Column
import jakarta.persistence.DiscriminatorColumn
import jakarta.persistence.DiscriminatorType
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.Inheritance
import jakarta.persistence.InheritanceType
import jakarta.persistence.Table
import org.hibernate.annotations.Type

@AllOpen
@Entity
@Table(name = "case_widget_tab_widget")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(
    name = "case_widget_type",
    discriminatorType = DiscriminatorType.STRING
)
abstract class CaseWidgetTabWidget(

    @EmbeddedId
    @JsonProperty("key")
    val id: CaseWidgetTabWidgetId,

    @Column(name = "title", nullable = false)
    val title: String,

    @Column(name = "sort_order", nullable = false)
    val order: Int,

    @Column(name = "width", nullable = false)
    val width: Int,

    @Column(name = "high_contrast", nullable = false)
    val highContrast: Boolean,

    @Type(value = JsonType::class)
    @Column(name = "actions", nullable = false)
    val actions: List<CaseWidgetAction> = emptyList(),
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CaseWidgetTabWidget) return false

        if (id != other.id) return false
        if (title != other.title) return false
        if (order != other.order) return false
        if (width != other.width) return false
        if (highContrast != other.highContrast) return false
        if (actions != other.actions) return false

        return true
    }

    override fun hashCode(): Int {
        return Objects.hashCode(
            title,
            order,
            width,
            highContrast,
            actions,
        )
    }

    override fun toString(): String {
        return "CaseWidgetTabWidget(id='$id', title='$title', order=$order, width=$width, highContrast=$highContrast)"
    }
}
