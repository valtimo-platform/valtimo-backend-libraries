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

package com.ritense.processlink.uicomponent.domain

import com.ritense.processlink.domain.ActivityTypeWithEventName
import com.ritense.processlink.domain.ProcessLink
import jakarta.persistence.Column
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity
import java.util.UUID

@Entity
@DiscriminatorValue(UIComponentProcessLink.TYPE_UI_COMPONENT)
class UIComponentProcessLink(
    id: UUID,
    processDefinitionId: String,
    activityId: String,
    activityType: ActivityTypeWithEventName,
    @Column(name = "component_key")
    val componentKey: String
) : ProcessLink(
    id,
    processDefinitionId,
    activityId,
    activityType,
    TYPE_UI_COMPONENT
) {

    override fun copy(id: UUID, processDefinitionId: String) =
        copy(
            id = id,
            processDefinitionId = processDefinitionId,
            activityId = activityId
        )

    fun copy(
        id: UUID = this.id,
        processDefinitionId: String = this.processDefinitionId,
        activityId: String = this.activityId,
        activityType: ActivityTypeWithEventName = this.activityType,
        componentKey: String = this.componentKey
    ) = UIComponentProcessLink(
        id = id,
        processDefinitionId = processDefinitionId,
        activityId = activityId,
        activityType = activityType,
        componentKey = componentKey,
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as UIComponentProcessLink

        return componentKey == other.componentKey
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + componentKey.hashCode()
        return result
    }

    companion object {
        const val TYPE_UI_COMPONENT = "ui-component"
    }
}