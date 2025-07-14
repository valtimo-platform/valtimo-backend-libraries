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

package com.ritense.iko.domain

import io.hypersistence.utils.hibernate.type.json.JsonType
import jakarta.persistence.Column
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.Table
import org.hibernate.annotations.Type

@Entity
@Table(name = "iko_data_request")
class IkoDataRequest(

    @EmbeddedId
    val id: IkoDataRequestId,

    @Column(name = "title", nullable = false)
    val title: String,

    @Column(name = "iko_data_request_order", nullable = false)
    val order: Int,

    @Type(value = JsonType::class)
    @Column(name = "properties", nullable = false)
    val properties: Map<String, Any?>,
)