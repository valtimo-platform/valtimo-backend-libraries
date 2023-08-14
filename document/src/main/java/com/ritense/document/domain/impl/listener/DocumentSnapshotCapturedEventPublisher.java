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

import com.ritense.document.domain.impl.event.JsonSchemaDocumentCreatedEvent;
import com.ritense.document.domain.impl.event.JsonSchemaDocumentModifiedEvent;
import com.ritense.document.domain.impl.event.JsonSchemaDocumentSnapshotCapturedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;

public class DocumentSnapshotCapturedEventPublisher {

    private static final Logger logger = LoggerFactory.getLogger(DocumentSnapshotCapturedEventPublisher.class);
    private final ApplicationEventPublisher applicationEventPublisher;

    public DocumentSnapshotCapturedEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    // TODO this is event is bogus mis use and should either be command or removed.
    @EventListener(JsonSchemaDocumentCreatedEvent.class)
    public void handleDocumentCreatedEvent(JsonSchemaDocumentCreatedEvent event) {
        logger.debug("{} - handle - jsonSchemaDocumentCreatedEvent - {}", Thread.currentThread().getName(), event.documentId());
        applicationEventPublisher.publishEvent(
            new JsonSchemaDocumentSnapshotCapturedEvent(
                event.documentId(),
                event.getOccurredOn(),
                event.getUser()
            )
        );
    }

    @EventListener(JsonSchemaDocumentModifiedEvent.class)
    public void handleDocumentModifiedEvent(JsonSchemaDocumentModifiedEvent event) {
        logger.debug("{} - handle - JsonSchemaDocumentModifiedEvent - {}", Thread.currentThread().getName(), event.documentId());
        applicationEventPublisher.publishEvent(
            new JsonSchemaDocumentSnapshotCapturedEvent(
                event.documentId(),
                event.getOccurredOn(),
                event.getUser()
            )
        );
    }

}