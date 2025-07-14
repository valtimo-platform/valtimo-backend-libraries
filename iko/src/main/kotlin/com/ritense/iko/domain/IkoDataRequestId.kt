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

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.persistence.FetchType.EAGER
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne

@Embeddable
class IkoDataRequestId(

    @Column(name = "key", updatable = false, nullable = false)
    val key: String,

    @ManyToOne(fetch = EAGER)
    @JoinColumn(name = "iko_data_aggregate_key", referencedColumnName = "key")
    val ikoDataAggregate: IkoDataAggregate,
)