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

package com.ritense.document.service.impl;

import com.ritense.authorization.AuthorizationService;
import com.ritense.authorization.request.EntityAuthorizationRequest;
import com.ritense.document.domain.impl.JsonSchemaDocumentDefinition;
import com.ritense.document.service.DocumentService;
import com.ritense.document.service.UndeployDocumentDefinitionService;
import com.ritense.document.service.result.UndeployDocumentDefinitionResult;
import com.ritense.document.service.result.UndeployDocumentDefinitionResultFailed;
import com.ritense.document.service.result.UndeployDocumentDefinitionResultSucceeded;
import com.ritense.document.service.result.error.DocumentDefinitionError;
import com.ritense.valtimo.contract.event.UndeployDocumentDefinitionEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import static com.ritense.authorization.AuthorizationContext.runWithoutAuthorization;
import static com.ritense.document.service.JsonSchemaDocumentDefinitionActionProvider.DELETE;

public class UndeployJsonSchemaDocumentDefinitionService implements UndeployDocumentDefinitionService {

    private final JsonSchemaDocumentDefinitionService documentDefinitionService;
    private final DocumentService documentService;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final AuthorizationService authorizationService;

    public UndeployJsonSchemaDocumentDefinitionService(
        JsonSchemaDocumentDefinitionService documentDefinitionService,
        DocumentService documentService,
        ApplicationEventPublisher applicationEventPublisher,
        AuthorizationService authorizationService
    ) {
        this.documentDefinitionService = documentDefinitionService;
        this.documentService = documentService;
        this.applicationEventPublisher = applicationEventPublisher;
        this.authorizationService = authorizationService;
    }

    @Override
    @Transactional
    public UndeployDocumentDefinitionResult undeploy(String documentDefinitionName) {
        try {
            Optional<JsonSchemaDocumentDefinition> documentDefinition = documentDefinitionService.findLatestByName(
                documentDefinitionName);
            return documentDefinition.map(definition -> {
                authorizationService.requirePermission(new EntityAuthorizationRequest<>(
                    JsonSchemaDocumentDefinition.class,
                    DELETE,
                    definition
                ));

                if (definition.isReadOnly()) {
                    return new UndeployDocumentDefinitionResultFailed(List.of(
                        () -> "The document definition is marked read-only and therefore not deletable")
                    );
                }
                runWithoutAuthorization(() -> {
                    documentService.removeDocuments(documentDefinitionName);
                    return null;
                });
                documentDefinitionService.removeDocumentDefinition(documentDefinitionName);
                applicationEventPublisher.publishEvent(new UndeployDocumentDefinitionEvent(documentDefinitionName));
                return new UndeployDocumentDefinitionResultSucceeded(documentDefinitionName);
            }).orElseGet(() -> new UndeployDocumentDefinitionResultFailed(List.of(
                () -> "The document definition was not found")
            ));
        } catch (Exception ex) {
            DocumentDefinitionError error = ex::getMessage;
            return new UndeployDocumentDefinitionResultFailed(List.of(error));
        }
    }
}
