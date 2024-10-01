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

package com.ritense.case_.domain.tab

import com.ritense.case.domain.CaseTabId
import jakarta.persistence.CascadeType.ALL
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.FetchType.EAGER
import jakarta.persistence.OneToMany
import jakarta.persistence.OrderBy
import jakarta.persistence.Table


@Entity
@Table(name = "case_widget_tab")
data class CaseWidgetTab(
    @EmbeddedId
    val id: CaseTabId,

    @OneToMany(mappedBy = "id.caseWidgetTab", fetch = EAGER, cascade = [ALL], orphanRemoval = true)
    @OrderBy("order ASC")

    val widgets: List<CaseWidgetTabWidget> = listOf(),
) {
    init {
        widgets.forEach { widget -> widget.id.caseWidgetTab = this }
    }
}
