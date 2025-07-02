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

package com.ritense.tab.importer

import com.ritense.tab.domain.Tab

data class TabDto(
    val key: String,
    val title: String?,
    val type: String,
) {
    fun toEntity(ownerType: String, ownerId: String, order: Int) = Tab(
        ownerType = ownerType,
        ownerId = ownerId,
        key = this.key,
        title = this.title,
        type = this.type,
        order = order
    )

    companion object {
        fun from(entity: Tab) = TabDto(
            key = entity.key,
            title = entity.title,
            type = entity.type,
        )
    }
}
