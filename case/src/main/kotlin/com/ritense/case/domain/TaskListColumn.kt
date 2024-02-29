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

package com.ritense.case.domain

import io.hypersistence.utils.hibernate.type.json.JsonType
import jakarta.persistence.Column
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Table
import org.hibernate.annotations.Type

@Entity
@Table(name = "task_list_column")
data class TaskListColumn(
    @EmbeddedId
    val id: CaseListColumnId,
    @Column(name = "title")
    val title: String?,
    @Column(name = "path")
    val path: String,
    @Type(value = JsonType::class)
    @Column(name = "display_type", columnDefinition = "JSON")
    val displayType: DisplayType,
    @Column(name = "sortable")
    val sortable: Boolean,
    @Column(name = "default_sort")
    @Enumerated(EnumType.STRING)
    val defaultSort: ColumnDefaultSort?,
    @Column(name = "column_order")
    val order: Int
)
