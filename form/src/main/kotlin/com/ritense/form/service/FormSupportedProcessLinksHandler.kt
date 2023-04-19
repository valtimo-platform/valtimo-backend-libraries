/*
 * Copyright 2015-2023 Ritense BV, the Netherlands.
 *
 *  Licensed under EUPL, Version 1.2 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" basis,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.ritense.form.service

import com.ritense.processlink.domain.ActivityTypeWithEventName
import com.ritense.processlink.domain.ProcessLinkType
import com.ritense.processlink.domain.SupportedProcessLinkTypeHandler

class FormSupportedProcessLinksHandler(
    val formDefinitionService: FormDefinitionService
): SupportedProcessLinkTypeHandler {

    private val supportedActivityTypes = listOf(
        ActivityTypeWithEventName.USER_TASK_CREATE,
        ActivityTypeWithEventName.START_EVENT_START
    )

    override fun getProcessLinkType(activityType: String): ProcessLinkType? {
        if (supportedActivityTypes.contains(ActivityTypeWithEventName.fromValue(activityType))) {
            return ProcessLinkType("form", isEnabled(activityType))
        }
        return null
    }

    private fun isEnabled(activityType: String): Boolean {
        return formDefinitionService.countAllForms() > 0
    }

}