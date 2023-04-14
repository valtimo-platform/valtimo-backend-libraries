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

package com.ritense.form.domain

import com.ritense.form.mapper.FormProcessLinkMapper.Companion.PROCESS_LINK_TYPE_FORM
import com.ritense.processlink.domain.ActivityTypeWithEventName
import com.ritense.processlink.domain.ProcessLink
import java.util.UUID
import javax.persistence.Column
import javax.persistence.DiscriminatorValue
import javax.persistence.Entity

@Entity
@DiscriminatorValue(PROCESS_LINK_TYPE_FORM)
data class FormProcessLink(
    override val id: UUID,

    override val processDefinitionId: String,

    override val activityId: String,

    override val activityType: ActivityTypeWithEventName,

    @Column(name = "form_definition_id")
    val formDefinitionId: UUID

) : ProcessLink(
    id,
    processDefinitionId,
    activityId,
    activityType,
    PROCESS_LINK_TYPE_FORM,
) {

    override fun copy(
        id: UUID,
        processDefinitionId: String,
    ) = copy(
        id = id,
        processDefinitionId = processDefinitionId,
        activityId = activityId,
        activityType = activityType,
        formDefinitionId = formDefinitionId
    )
}
