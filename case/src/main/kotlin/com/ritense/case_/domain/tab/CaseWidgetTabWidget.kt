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

import jakarta.persistence.Column
import jakarta.persistence.DiscriminatorColumn
import jakarta.persistence.DiscriminatorType
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Inheritance
import jakarta.persistence.InheritanceType
import jakarta.persistence.Table

@Entity
@Table(name = "case_widget_tab_widget")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(
    name = "case_widget_type",
    discriminatorType = DiscriminatorType.STRING
)
abstract class CaseWidgetTabWidget(
    @Id
    @Column(name = "`key`", updatable = false, nullable = false, unique = true)
    val key: String,

    @Column(name = "title", nullable = false)
    val title: String,

    @Column(name = "sort_order", nullable = false)
    val order: Int,

    @Column(name = "width", nullable = false)
    val width: Int,

    @Column(name = "high_contrast", nullable = false)
    val highContrast: Boolean,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CaseWidgetTabWidget) return false

        if (key != other.key) return false
        if (title != other.title) return false
        if (order != other.order) return false
        if (width != other.width) return false
        if (highContrast != other.highContrast) return false

        return true
    }

    override fun hashCode(): Int {
        var result = key.hashCode()
        result = 31 * result + title.hashCode()
        result = 31 * result + order
        result = 31 * result + width
        result = 31 * result + highContrast.hashCode()
        return result
    }

    override fun toString(): String {
        return "CaseWidgetTabWidget(key='$key', title='$title', order=$order, width=$width, highContrast=$highContrast)"
    }
}
