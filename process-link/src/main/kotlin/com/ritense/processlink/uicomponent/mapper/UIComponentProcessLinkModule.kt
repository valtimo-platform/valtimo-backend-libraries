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

package com.ritense.processlink.uicomponent.mapper

import com.fasterxml.jackson.databind.module.SimpleModule
import com.ritense.processlink.uicomponent.dto.UIComponentProcessLinkCreateRequestDto
import com.ritense.processlink.uicomponent.dto.UIComponentProcessLinkDeployDto
import com.ritense.processlink.uicomponent.dto.UIComponentProcessLinkExportResponseDto
import com.ritense.processlink.uicomponent.dto.UIComponentProcessLinkResponseDto
import com.ritense.processlink.uicomponent.dto.UIComponentProcessLinkUpdateRequestDto

class UIComponentProcessLinkModule: SimpleModule(MODULE_NAME) {

    override fun setupModule(context: SetupContext) {
        super.setupModule(context)

        context.registerSubtypes(
            UIComponentProcessLinkCreateRequestDto::class.java,
            UIComponentProcessLinkResponseDto::class.java,
            UIComponentProcessLinkDeployDto::class.java,
            UIComponentProcessLinkExportResponseDto::class.java,
            UIComponentProcessLinkUpdateRequestDto::class.java
        )
    }

    companion object {
        const val MODULE_NAME = "UIComponentProcessLinkModule"
    }
}