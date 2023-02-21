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

package com.ritense.document.domain.impl.listener;

import com.ritense.document.domain.impl.event.JsonSchemaDocumentSnapshotCapturedEvent;
import com.ritense.document.service.DocumentSnapshotService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;

public class DocumentSnapshotCapturedEventListener {

    private static final Logger logger = LoggerFactory.getLogger(DocumentSnapshotCapturedEventListener.class);
    private final DocumentSnapshotService documentSnapshotService;

    public DocumentSnapshotCapturedEventListener(DocumentSnapshotService documentSnapshotService) {
        this.documentSnapshotService = documentSnapshotService;
    }

    @EventListener(JsonSchemaDocumentSnapshotCapturedEvent.class)
    public void handleDocumentCreatedEvent(JsonSchemaDocumentSnapshotCapturedEvent event) {
        logger.debug("{} - handle - JsonSchemaDocumentSnapshotEvent - {}", Thread.currentThread().getName(), event.documentId());
        documentSnapshotService.makeSnapshot(event.documentId(), event.createdOn(), event.createdBy());
    }

}