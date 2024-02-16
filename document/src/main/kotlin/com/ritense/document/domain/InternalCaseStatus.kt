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

package com.ritense.document.domain

import jakarta.persistence.Column
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Table

@Entity
@Table(name = "internal_case_status")
data class InternalCaseStatus(
    @EmbeddedId
    val id: InternalCaseStatusId,
    @Column(name = "status_title")
    val title: String,
    @Column(name = "visible_in_case_list_by_default")
    val visibleInCaseListByDefault: Boolean,
    @Column(name = "internal_case_status_order")
    val order: Int,
    @Column(name = "internal_case_status_color")
    @Enumerated(EnumType.STRING)
    val color: InternalCaseStatusColor,
) {
    init {
        require(title.isNotBlank()) { "title was blank!" }
        require(order >= 0) { "order was < 0" }
    }
}
