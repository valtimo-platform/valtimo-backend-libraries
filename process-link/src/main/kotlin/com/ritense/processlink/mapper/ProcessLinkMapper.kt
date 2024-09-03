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

package com.ritense.processlink.mapper

import com.ritense.exporter.request.ExportRequest
import com.ritense.processlink.autodeployment.ProcessLinkDeployDto
import com.ritense.processlink.domain.ProcessLink
import com.ritense.processlink.web.rest.dto.ProcessLinkCreateRequestDto
import com.ritense.processlink.web.rest.dto.ProcessLinkExportResponseDto
import com.ritense.processlink.web.rest.dto.ProcessLinkResponseDto
import com.ritense.processlink.web.rest.dto.ProcessLinkUpdateRequestDto
import java.util.UUID

interface ProcessLinkMapper {
    fun supportsProcessLinkType(processLinkType: String): Boolean
    fun toProcessLinkResponseDto(processLink: ProcessLink): ProcessLinkResponseDto
    fun toProcessLinkCreateRequestDto(deployDto: ProcessLinkDeployDto): ProcessLinkCreateRequestDto
    fun toProcessLinkUpdateRequestDto(
        deployDto: ProcessLinkDeployDto,
        existingProcessLinkId: UUID
    ): ProcessLinkUpdateRequestDto

    fun toProcessLinkExportResponseDto(processLink: ProcessLink): ProcessLinkExportResponseDto
    fun toNewProcessLink(createRequestDto: ProcessLinkCreateRequestDto): ProcessLink
    fun toUpdatedProcessLink(
        processLinkToUpdate: ProcessLink,
        updateRequestDto: ProcessLinkUpdateRequestDto
    ): ProcessLink

    /**
     * Used by the export service.
     * Should return export requests the provided processLink depends on.
     * @param processLink The processLink to create related export requests for
     */
    fun createRelatedExportRequests(processLink: ProcessLink): Set<ExportRequest> = setOf()

    fun getImporterType(): String? = null
}
