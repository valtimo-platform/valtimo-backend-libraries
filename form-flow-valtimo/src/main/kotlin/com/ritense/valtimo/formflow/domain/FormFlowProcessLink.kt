/*
 * Copyright 2015-2023 Ritense BV, the Netherlands.
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

package com.ritense.valtimo.formflow.domain

import com.ritense.processlink.domain.ActivityTypeWithEventName
import com.ritense.processlink.domain.ProcessLink
import com.ritense.valtimo.formflow.mapper.FormFlowProcessLinkMapper.Companion.PROCESS_LINK_TYPE_FORM_FLOW
import java.util.UUID
import jakarta.persistence.Column
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity

@Entity
@DiscriminatorValue(PROCESS_LINK_TYPE_FORM_FLOW)
data class FormFlowProcessLink(
    override val id: UUID,

    override val processDefinitionId: String,

    override val activityId: String,

    override val activityType: ActivityTypeWithEventName,

    @Column(name = "form_flow_definition_id", nullable = false)
    val formFlowDefinitionId: String

) : ProcessLink(
    id,
    processDefinitionId,
    activityId,
    activityType,
    PROCESS_LINK_TYPE_FORM_FLOW,
) {

    override fun copy(
        id: UUID,
        processDefinitionId: String,
    ) = copy(
        id = id,
        processDefinitionId = processDefinitionId,
        activityId = activityId,
        activityType = activityType,
        formFlowDefinitionId = this.formFlowDefinitionId
    )
}
