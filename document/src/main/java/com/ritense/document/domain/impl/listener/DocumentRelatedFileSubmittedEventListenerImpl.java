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

package com.ritense.document.domain.impl.listener;

import static com.ritense.authorization.AuthorizationContext.runWithoutAuthorization;

import com.ritense.document.domain.impl.JsonSchemaDocumentId;
import com.ritense.document.domain.impl.JsonSchemaRelatedFile;
import com.ritense.document.service.DocumentService;
import com.ritense.resource.service.ResourceService;
import com.ritense.valtimo.contract.document.event.DocumentRelatedFileSubmittedEvent;
import com.ritense.valtimo.contract.listener.DocumentRelatedFileEventListener;
import com.ritense.valtimo.contract.utils.SecurityUtils;
import java.util.Optional;

public class DocumentRelatedFileSubmittedEventListenerImpl implements DocumentRelatedFileEventListener {

    private final DocumentService documentService;
    private final Optional<ResourceService> resourceServiceOpt;

    public DocumentRelatedFileSubmittedEventListenerImpl(DocumentService documentService, Optional<ResourceService> resourceServiceOpt) {
        this.documentService = documentService;
        this.resourceServiceOpt = resourceServiceOpt;
    }

    @Override
    public void handle(DocumentRelatedFileSubmittedEvent event) {
        resourceServiceOpt.ifPresent(resourceService -> {
            var resource = resourceService.getResource(event.getResourceId());
            runWithoutAuthorization(() -> {
                documentService.assignRelatedFile(
                    JsonSchemaDocumentId.existingId(event.getDocumentId()),
                    JsonSchemaRelatedFile.from(resource).withCreatedBy(SecurityUtils.getCurrentUserLogin())
                );
                return null;
            });
        });
    }
}
