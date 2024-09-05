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

package com.ritense.zakenapi.service

import com.ritense.temporaryresource.domain.ResourceStorageMetadata
import com.ritense.temporaryresource.domain.ResourceStorageMetadataId
import com.ritense.temporaryresource.domain.StorageMetadataKeys
import com.ritense.temporaryresource.repository.ResourceStorageMetadataRepository
import com.ritense.zakenapi.event.ResourceStorageDocumentMetadataAvailableEvent
import org.springframework.context.event.EventListener

class DocumentMetadataAvailableEventListener(
    private val repository: ResourceStorageMetadataRepository
) {

    @EventListener(ResourceStorageDocumentMetadataAvailableEvent::class)
    fun storeResourceMetadata(event: ResourceStorageDocumentMetadataAvailableEvent) {
        if (event.documentId.isNotEmpty()) {
            repository.save(
                ResourceStorageMetadata(
                    ResourceStorageMetadataId(event.resourceId, StorageMetadataKeys.DOCUMENT_ID),
                    event.documentId
                )
            )
        }

        if (event.downloadUrl.isNotEmpty()) {
            repository.save(
                ResourceStorageMetadata(
                    ResourceStorageMetadataId(event.resourceId, StorageMetadataKeys.DOWNLOAD_URL),
                    event.downloadUrl
                )
            )
        }
    }
}