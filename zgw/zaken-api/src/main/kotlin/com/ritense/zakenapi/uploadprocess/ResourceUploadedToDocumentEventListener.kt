/*
 * Copyright 2015-2022 Ritense BV, the Netherlands.
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

package com.ritense.zakenapi.uploadprocess

import com.ritense.resource.domain.MetadataType
import com.ritense.resource.domain.TemporaryResourceUploadedEvent
import com.ritense.resource.service.TemporaryResourceStorageService
import mu.KotlinLogging
import org.springframework.context.event.EventListener

class ResourceUploadedToDocumentEventListener(
    private val resourceService: TemporaryResourceStorageService,
    private val uploadProcessService: UploadProcessService,
) {

    @EventListener(TemporaryResourceUploadedEvent::class)
    fun handle(event: TemporaryResourceUploadedEvent) {
        logger.debug { "Handling TemporaryResourceUploadedEvent with resourceId: ${event.resourceId}" }

        val metadata = resourceService.getResourceMetadata(event.resourceId)
        val caseId = metadata[MetadataType.DOCUMENT_ID.key] as String?

        if (caseId != null) {
            logger.debug { "Uploading resource to document: ${event.resourceId}" }
            uploadProcessService.startUploadResourceProcess(caseId, event.resourceId)
        }
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}
