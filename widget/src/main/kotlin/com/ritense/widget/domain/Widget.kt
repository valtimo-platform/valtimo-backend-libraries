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

package com.ritense.widget.domain

import com.ritense.valtimo.contract.annotation.AllOpen
import com.ritense.widget.web.rest.dto.WidgetDto
import io.hypersistence.utils.hibernate.type.json.JsonType
import jakarta.persistence.Column
import jakarta.persistence.DiscriminatorColumn
import jakarta.persistence.DiscriminatorType
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Inheritance
import jakarta.persistence.InheritanceType
import jakarta.persistence.Table
import org.hibernate.annotations.Type
import java.util.Objects
import java.util.UUID

@AllOpen
@Entity
@Table(name = "widget")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(
    name = "widget_type",
    discriminatorType = DiscriminatorType.STRING
)
abstract class Widget(
    @Id
    @Column(name = "id")
    val id: UUID = UUID.randomUUID(),

    @Column(name = "widget_key", nullable = false)
    val key: String,

    @Column(name = "title", nullable = false)
    val title: String,

    @Column(name = "widget_order", nullable = false)
    val order: Int,

    @Column(name = "width", nullable = false)
    val width: Int,

    @Column(name = "high_contrast", nullable = false)
    val highContrast: Boolean,

    @Type(value = JsonType::class)
    @Column(name = "actions", nullable = false)
    val actions: List<WidgetAction> = emptyList(),
) {

    init {
        require(title == null || title.isNotBlank()) { "title was blank!" }
        require(order >= 0) { "order was < 0" }
    }

    abstract fun copy(
        id: UUID = this.id,
        key: String = this.key,
        title: String = this.title,
        order: Int = this.order,
        width: Int = this.width,
        highContrast: Boolean = this.highContrast,
        actions: List<WidgetAction> = this.actions,
    ): Widget

    abstract fun toDto(): WidgetDto

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Widget) return false

        if (id != other.id) return false
        if (key != other.key) return false
        if (title != other.title) return false
        if (order != other.order) return false
        if (width != other.width) return false
        if (highContrast != other.highContrast) return false
        if (actions != other.actions) return false

        return true
    }

    override fun hashCode(): Int {
        return Objects.hash(
            id,
            key,
            title,
            order,
            width,
            highContrast,
            actions,
        )
    }

    override fun toString(): String {
        return "Widget(id='$id', key='$key', title='$title', order=$order, width=$width, highContrast=$highContrast)"
    }
}