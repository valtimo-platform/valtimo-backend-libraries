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

package com.ritense.dashboard.domain

import com.fasterxml.jackson.databind.node.ObjectNode
import org.hibernate.annotations.Type
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table

@Entity
@Table(name = "dashboard_widget_configuration")
data class WidgetConfiguration(

    @Id
    @Column(name = "`key`", updatable = false, nullable = false, unique = true)
    val key: String,

    @Column(name = "title", nullable = false)
    val title: String,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "dashboard_key")
    val dashboard: Dashboard,

    @Column(name = "data_source_key", nullable = false)
    val dataSourceKey: String,

    @Type(type = "com.vladmihalcea.hibernate.type.json.JsonType")
    @Column(name = "data_source_properties", columnDefinition = "JSON")
    val dataSourceProperties: ObjectNode,

    @Type(type = "com.vladmihalcea.hibernate.type.json.JsonType")
    @Column(name = "display_type_properties", columnDefinition = "JSON")
    val displayTypeProperties: ObjectNode,

    @Column(name = "display_type", nullable = false)
    val displayType: String,

    @Column(name = "sort_order", nullable = false)
    val order: Int
) {
    override fun toString(): String {
        return "WidgetConfiguration(" +
            "key='$key', " +
            "title='$title', " +
            "dataSourceKey='$dataSourceKey', " +
            "dataSourceProperties=$dataSourceProperties, " +
            "displayTypeProperties=$displayTypeProperties, " +
            "displayType='$displayType', " +
            "order=$order)"
    }
}
