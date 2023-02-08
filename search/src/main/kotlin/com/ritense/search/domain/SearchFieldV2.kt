/*
 * Copyright 2015-2022 Ritense BV, the Netherlands.
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

package com.ritense.search.domain

import java.util.UUID
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "search_field_v2")
data class SearchFieldV2(
    @Id
    @Column(name = "id")
    val id: UUID = UUID.randomUUID(),
    @Column(name = "owner_id")
    val ownerId: String,
    @Column(name = "column_key")
    val key: String,
    @Column(name = "title")
    val title: String?,
    @Column(name = "path")
    val path: String,
    @Column(name = "column_order")
    val order: Int,
    @Column(name = "data_type")
    val dataType: DataType,
    @Column(name = "field_type")
    val fieldType: FieldType
)