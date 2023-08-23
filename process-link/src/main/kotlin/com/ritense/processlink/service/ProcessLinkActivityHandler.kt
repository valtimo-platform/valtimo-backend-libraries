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
package com.ritense.processlink.service

import com.ritense.processlink.domain.ProcessLink
import com.ritense.processlink.web.rest.dto.ProcessLinkActivityResult
import org.camunda.bpm.engine.task.Task
import java.util.UUID

interface ProcessLinkActivityHandler<T> {
    fun supports(processLink: ProcessLink): Boolean
    fun openTask(task: Task, processLink: ProcessLink): ProcessLinkActivityResult<T>
    fun getStartEventObject(
        processDefinitionId: String,
        documentId: UUID?,
        documentDefinitionName: String?,
        processLink: ProcessLink,
        tenantId: String
    ): ProcessLinkActivityResult<T>
}