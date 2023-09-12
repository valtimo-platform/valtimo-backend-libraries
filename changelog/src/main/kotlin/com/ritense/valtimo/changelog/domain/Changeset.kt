/*
 *  Copyright 2015-2023 Ritense BV, the Netherlands.
 *
 *  Licensed under EUPL, Version 1.2 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" basis,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.ritense.valtimo.changelog.domain

import java.time.Instant
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "valtimo_changelog")
data class Changeset(

    @Id
    @Column(name = "id", nullable = false, updatable = false, unique = true)
    val id: String,

    @Column(name = "`key`")
    val key: String?,

    @Column(name = "filename", nullable = false, updatable = false)
    val filename: String,

    @Column(name = "date_executed", nullable = false, updatable = false)
    val dateExecuted: Instant,

    @Column(name = "order_executed", nullable = false, updatable = false, unique = true)
    val orderExecuted: Int,

    @Column(name = "md5sum", nullable = false, updatable = false, unique = true)
    val md5sum: String,
)
