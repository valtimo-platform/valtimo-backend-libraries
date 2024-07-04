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

package com.ritense.processlink.domain

import jakarta.persistence.Column
import jakarta.persistence.DiscriminatorColumn
import jakarta.persistence.DiscriminatorType.STRING
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Inheritance
import jakarta.persistence.InheritanceType.SINGLE_TABLE
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "process_link")
@Inheritance(strategy = SINGLE_TABLE)
@DiscriminatorColumn(name = "process_link_type", discriminatorType = STRING)
abstract class ProcessLink(

    @Id
    @Column(name = "id")
    open val id: UUID,

    @Column(name = "process_definition_id")
    open val processDefinitionId: String,

    @Column(name = "activity_id")
    open val activityId: String,

    @Column(name = "activity_type")
    @Enumerated(EnumType.STRING)
    open val activityType: ActivityTypeWithEventName,

    @Column(name = "process_link_type", insertable = false, updatable = false)
    open val processLinkType: String,
) {
    abstract fun copy(
        id: UUID = this.id,
        processDefinitionId: String = this.processDefinitionId,
    ) : ProcessLink

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ProcessLink

        if (id != other.id) return false
        if (processDefinitionId != other.processDefinitionId) return false
        if (activityId != other.activityId) return false
        if (activityType != other.activityType) return false
        if (processLinkType != other.processLinkType) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + processDefinitionId.hashCode()
        result = 31 * result + activityId.hashCode()
        result = 31 * result + activityType.hashCode()
        result = 31 * result + processLinkType.hashCode()
        return result
    }
}
