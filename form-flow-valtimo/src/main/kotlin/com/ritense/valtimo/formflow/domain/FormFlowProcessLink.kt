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

package com.ritense.valtimo.formflow.domain

import com.ritense.form.domain.FormDisplayType
import com.ritense.form.domain.FormSizes
import com.ritense.processlink.domain.ActivityTypeWithEventName
import com.ritense.processlink.domain.ProcessLink
import com.ritense.valtimo.formflow.mapper.FormFlowProcessLinkMapper.Companion.PROCESS_LINK_TYPE_FORM_FLOW
import io.hypersistence.utils.hibernate.type.json.JsonType
import jakarta.persistence.Column
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import org.hibernate.annotations.Type
import java.util.UUID

@Entity
@DiscriminatorValue(PROCESS_LINK_TYPE_FORM_FLOW)
class FormFlowProcessLink(
    id: UUID,
    processDefinitionId: String,
    activityId: String,
    activityType: ActivityTypeWithEventName,

    @Type(value = JsonType::class)
    @Column(name = "subtitles", columnDefinition = "JSON", nullable = true)
    val subtitles: List<String>? = null,

    @Column(name = "form_flow_definition_id", nullable = false)
    val formFlowDefinitionId: String,

    @Column(name = "form_display_type")
    @Enumerated(EnumType.STRING)
    val formDisplayType: FormDisplayType = FormDisplayType.modal,

    @Column(name = "form_size")
    @Enumerated(EnumType.STRING)
    val formSize: FormSizes = FormSizes.medium

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
        activityId = activityId
    )

    fun copy(
        id: UUID = this.id,
        processDefinitionId: String = this.processDefinitionId,
        activityId: String = this.activityId,
        activityType: ActivityTypeWithEventName = this.activityType,
        formFlowDefinitionId: String = this.formFlowDefinitionId,
        formDisplayType: FormDisplayType = this.formDisplayType,
        formSize: FormSizes = this.formSize,
        subtitles: List<String>? = this.subtitles,
    ) = FormFlowProcessLink(
        id = id,
        processDefinitionId = processDefinitionId,
        activityId = activityId,
        activityType = activityType,
        formFlowDefinitionId = formFlowDefinitionId,
        formDisplayType = formDisplayType,
        formSize = formSize,
        subtitles = subtitles,
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as FormFlowProcessLink

        if (formFlowDefinitionId != other.formFlowDefinitionId) return false
        if (formDisplayType != other.formDisplayType) return false
        if (formSize != other.formSize) return false
        if (subtitles != other.subtitles) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + formFlowDefinitionId.hashCode()
        result = 31 * result + formDisplayType.hashCode()
        result = 31 * result + formSize.hashCode()
        result = 31 * result + subtitles.hashCode()
        return result
    }
}
