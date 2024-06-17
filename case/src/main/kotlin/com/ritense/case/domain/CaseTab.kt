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

import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "case_tab")
data class CaseTab(

    @EmbeddedId
    val id: CaseTabId,

    val name: String?,

    val tabOrder: Int,

    @Enumerated(EnumType.STRING)
    val type: CaseTabType,

    val contentKey: String,

    val createdOn: LocalDateTime? = LocalDateTime.now(),

    val createdBy: String? = null,
    val showTasks: Boolean = false
) {
    constructor(
        id: CaseTabId,
        name: String?,
        tabOrder: Int,
        type: CaseTabType,
        contentKey: String,
    ) : this(id, name, tabOrder, type, contentKey, LocalDateTime.now(), null, false)

    init {
        require(name == null || name.isNotBlank()) { "name was blank!" }
        require(tabOrder >= 0) { "tabOrder was < 0" }
        require(contentKey.isNotBlank()) { "contentKey was blank!" }
    }
}