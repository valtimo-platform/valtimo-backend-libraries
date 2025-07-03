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

import com.ritense.document.domain.impl.JsonSchemaDocument
import com.ritense.logging.withLoggingContext
import com.ritense.zakenapi.event.ResourceStorageDocumentMetadataAvailableEvent
import org.operaton.bpm.engine.delegate.DelegateExecution
import org.springframework.context.ApplicationEventPublisher

class UploadProcessDelegate(
    private val eventPublisher: ApplicationEventPublisher
) {

    fun publishFileUploadedEvent(execution: DelegateExecution) {
        withLoggingContext(JsonSchemaDocument::class, execution.processBusinessKey) {
            val event = ResourceStorageDocumentMetadataAvailableEvent(
                this,
                resourceId = execution.getVariable("resourceId") as String,
                documentId = execution.getVariable("documentId") as? String ?: "",
                documentUrl = execution.getVariable("documentUrl") as? String ?: "",
                downloadUrl = execution.getVariable("downloadUrl") as? String ?: "",
            )
            eventPublisher.publishEvent(event)
        }
    }
}