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

package com.ritense.processlink.uicomponent.service

import com.ritense.processlink.domain.ProcessLink
import com.ritense.processlink.service.ProcessLinkActivityHandler
import com.ritense.processlink.uicomponent.domain.UIComponentActivityResultProperties
import com.ritense.processlink.uicomponent.domain.UIComponentProcessLink
import com.ritense.processlink.uicomponent.domain.UIComponentProcessLink.Companion.TYPE_UI_COMPONENT
import com.ritense.processlink.web.rest.dto.ProcessLinkActivityResult
import com.ritense.valtimo.operaton.domain.OperatonTask
import java.util.UUID

class UIComponentProcessLinkActivityHandler : ProcessLinkActivityHandler<UIComponentActivityResultProperties> {

    override fun supports(processLink: ProcessLink): Boolean {
        return processLink is UIComponentProcessLink
    }

    override fun openTask(
        task: OperatonTask,
        processLink: ProcessLink
    ): ProcessLinkActivityResult<UIComponentActivityResultProperties> {
        processLink as UIComponentProcessLink
        return ProcessLinkActivityResult(
            processLink.id,
            TYPE_UI_COMPONENT,
            UIComponentActivityResultProperties(processLink.componentKey)
        )
    }

    override fun getStartEventObject(
        processDefinitionId: String,
        documentId: UUID?,
        documentDefinitionName: String?,
        processLink: ProcessLink
    ): ProcessLinkActivityResult<UIComponentActivityResultProperties> {
        processLink as UIComponentProcessLink
        return ProcessLinkActivityResult(
            processLink.id,
            TYPE_UI_COMPONENT,
            UIComponentActivityResultProperties(processLink.componentKey)
        )
    }
}