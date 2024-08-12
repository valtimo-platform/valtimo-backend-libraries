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

import com.fasterxml.jackson.annotation.JsonCreator
import com.ritense.valtimo.contract.domain.AbstractId
import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinColumns
import jakarta.persistence.ManyToOne
import java.util.Objects

@Embeddable
data class CaseWidgetTabWidgetId(

    @Column(name = "`key`", updatable = false, nullable = false, unique = true)
    val key: String,

    @ManyToOne(targetEntity = CaseWidgetTab::class, fetch = FetchType.LAZY)
    @JoinColumns(
        JoinColumn(name = "case_definition_name", referencedColumnName = "case_definition_name", updatable = false, nullable = false),
        JoinColumn(name = "tab_key", referencedColumnName = "tab_key", updatable = false, nullable = false)
    )
    var caseWidgetTab: CaseWidgetTab? = null
) : AbstractId<CaseWidgetTabWidgetId>() {

    override fun hashCode(): Int {
        return Objects.hash(key)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CaseWidgetTabWidgetId

        if (key != other.key) return false

        return true
    }

    override fun toString(): String {
        return "${caseWidgetTab?.id}:$key"
    }

    companion object {
        @JvmStatic
        @JsonCreator
        fun create(value: String) = CaseWidgetTabWidgetId(value).newIdentity()
    }
}