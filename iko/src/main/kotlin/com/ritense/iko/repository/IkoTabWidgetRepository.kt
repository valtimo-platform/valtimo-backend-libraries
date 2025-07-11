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

package com.ritense.iko.repository

import com.ritense.iko.domain.IkoTabWidget
import com.ritense.iko.domain.IkoTabWidgetId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface IkoTabWidgetRepository : JpaRepository<IkoTabWidget, IkoTabWidgetId>, JpaSpecificationExecutor<IkoTabWidget> {
    fun findAllByIdTabId(tabId: UUID): List<IkoTabWidget>
    fun findByIdTabIdAndWidgetKey(id: UUID, widgetKey: String): IkoTabWidget?
    fun deleteByIdTabIdAndWidgetKey(id: UUID, widgetKey: String)
}