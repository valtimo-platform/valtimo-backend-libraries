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

package com.ritense.form.processlink.dto

import com.fasterxml.jackson.annotation.JsonTypeName
import com.ritense.form.mapper.FormProcessLinkMapper.Companion.PROCESS_LINK_TYPE_FORM
import com.ritense.form.domain.FormSizes
import com.ritense.form.domain.FormDisplayType
import com.ritense.processlink.autodeployment.ProcessLinkDeployDto
import com.ritense.processlink.domain.ActivityTypeWithEventName

@JsonTypeName(PROCESS_LINK_TYPE_FORM)
data class FormProcessLinkDeployDto(
    override val processDefinitionId: String,
    override val activityId: String,
    override val activityType: ActivityTypeWithEventName,
    val formDefinitionName: String,
    val viewModelEnabled: Boolean = false,
    val formDisplayType: FormDisplayType = FormDisplayType.modal,
    val formSize: FormSizes = FormSizes.medium,
) : ProcessLinkDeployDto {
    override val processLinkType: String
        get() = PROCESS_LINK_TYPE_FORM
}
