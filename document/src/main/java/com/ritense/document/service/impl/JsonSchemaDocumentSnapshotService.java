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

import com.ritense.authorization.Action;
import com.ritense.authorization.AuthorizationService;
import com.ritense.authorization.request.EntityAuthorizationRequest;
import com.ritense.document.domain.Document;
import com.ritense.document.domain.impl.JsonSchemaDocumentId;
import com.ritense.document.domain.impl.snapshot.JsonSchemaDocumentSnapshot;
import com.ritense.document.domain.snapshot.DocumentSnapshot;
import com.ritense.document.exception.DocumentNotFoundException;
import com.ritense.document.repository.DocumentSnapshotRepository;
import com.ritense.document.service.DocumentSnapshotService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Nullable;
import java.time.LocalDateTime;
import java.util.Optional;
import static com.ritense.document.repository.impl.specification.JsonSchemaDocumentSnapshotSpecificationHelper.bySearch;
import static com.ritense.document.service.JsonSchemaDocumentSnapshotActionProvider.VIEW;
import static com.ritense.document.service.JsonSchemaDocumentSnapshotActionProvider.VIEW_LIST;

public class JsonSchemaDocumentSnapshotService implements DocumentSnapshotService {

    private final DocumentSnapshotRepository<JsonSchemaDocumentSnapshot> documentSnapshotRepository;
    private final JsonSchemaDocumentService documentService;
    private final JsonSchemaDocumentDefinitionService documentDefinitionService;
    private final AuthorizationService authorizationService;

    public JsonSchemaDocumentSnapshotService(DocumentSnapshotRepository<JsonSchemaDocumentSnapshot> documentSnapshotRepository, JsonSchemaDocumentService documentService, JsonSchemaDocumentDefinitionService documentDefinitionService, AuthorizationService authorizationService) {
        this.documentSnapshotRepository = documentSnapshotRepository;
        this.documentService = documentService;
        this.documentDefinitionService = documentDefinitionService;
        this.authorizationService = authorizationService;
    }

    @Override
    public Optional<JsonSchemaDocumentSnapshot> findById(DocumentSnapshot.Id id) {
        final var snapshot = documentSnapshotRepository.findById(id).orElse(null);
        if(snapshot != null) {
            authorizationService
                .requirePermission(
                    new EntityAuthorizationRequest<>(
                        JsonSchemaDocumentSnapshot.class,
                        VIEW,
                        snapshot
                    )
                );
        }

        return Optional.ofNullable(snapshot);
    }

    @Override
    public Page<JsonSchemaDocumentSnapshot> getDocumentSnapshots(
        @Nullable String definitionName,
        @Nullable JsonSchemaDocumentId documentId,
        @Nullable LocalDateTime fromDateTime,
        @Nullable LocalDateTime toDateTime,
        Pageable pageable
    ) {

        var spec = (Specification<JsonSchemaDocumentSnapshot>) authorizationService.getAuthorizationSpecification(
            new EntityAuthorizationRequest<>(
                JsonSchemaDocumentSnapshot.class,
                VIEW_LIST
            ), null
        ).and(
            bySearch(
                definitionName,
                documentId,
                fromDateTime,
                toDateTime
            )
        );

        return documentSnapshotRepository.findAll(
            spec,
            pageable
        );
    }

    @Transactional
    @Override
    public void makeSnapshot(Document.Id documentId, LocalDateTime createdOn, String createdBy) {
        denyAuthorization();

        var document = documentService.findBy(documentId)
            .orElseThrow(() -> new DocumentNotFoundException("Document not found with id " + documentId));

        var documentDefinition = documentDefinitionService.findBy(document.definitionId())
            .orElseThrow();

        documentSnapshotRepository.saveAndFlush(new JsonSchemaDocumentSnapshot(document, createdOn, createdBy, documentDefinition));
    }

    @Transactional
    @Override
    public void deleteSnapshotsBy(String documentDefinitionName) {
        denyAuthorization();

        documentSnapshotRepository.deleteAllByDefinitionName(documentDefinitionName);
    }

    private void denyAuthorization() {
        authorizationService.requirePermission(
            new EntityAuthorizationRequest<>(
                JsonSchemaDocumentSnapshot.class,
                Action.deny()
            )
        );
    }

}