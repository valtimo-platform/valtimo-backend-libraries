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
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.FetchType
import javax.persistence.Id
import javax.persistence.ManyToOne
import javax.persistence.Table

@Entity
@Table(name = "search_field_configuration")
class SearchFieldConfiguration(

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    val id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    val searchConfiguration: SearchConfiguration,

    @Column(name = "field_key", nullable = false, updatable = false)
    val key: String,

    @Column(name = "path", nullable = true, length = 255, updatable = true)
    val path: String,

    @Column(name = "data_type", nullable = false, length = 32, updatable = false)
    @Enumerated(EnumType.STRING)
    val dataType: DataType,

    @Column(name = "field_type", nullable = false, length = 32, updatable = false)
    @Enumerated(EnumType.STRING)
    val fieldType: FieldType,

    @Column(name = "match_type", nullable = false, length = 32, updatable = false)
    @Enumerated(EnumType.STRING)
    val matchType: MatchType,
)
