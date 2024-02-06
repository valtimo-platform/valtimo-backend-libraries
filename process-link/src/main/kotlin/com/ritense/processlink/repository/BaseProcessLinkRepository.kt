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

package com.ritense.processlink.repository

import com.ritense.processlink.domain.ActivityTypeWithEventName
import com.ritense.processlink.domain.ProcessLink
import java.util.UUID
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor

interface BaseProcessLinkRepository<T : ProcessLink> : JpaRepository<T, UUID>, JpaSpecificationExecutor<T> {
    fun findByProcessDefinitionId(processDefinitionId: String): List<T>
    fun findByProcessDefinitionIdAndActivityId(processDefinitionId: String, activityId: String): List<T>
    fun findByActivityIdAndActivityTypeAndProcessLinkType(
        activityId: String,
        activityType: ActivityTypeWithEventName,
        processLinkType: String
    ): List<T>

    fun findByProcessDefinitionIdAndActivityIdAndActivityType(
        processDefinitionId: String,
        activityId: String,
        activityType: ActivityTypeWithEventName
    ): List<T>

    fun findByProcessDefinitionIdAndActivityType(processDefinitionId: String, activityType: ActivityTypeWithEventName): T?
}
