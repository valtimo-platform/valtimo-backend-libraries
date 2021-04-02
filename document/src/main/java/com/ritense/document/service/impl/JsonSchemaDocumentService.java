/*
 * Copyright 2015-2020 Ritense BV, the Netherlands.
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

import com.ritense.document.domain.Document;
import com.ritense.document.domain.RelatedFile;
import com.ritense.document.domain.impl.JsonDocumentContent;
import com.ritense.document.domain.impl.JsonSchemaDocument;
import com.ritense.document.domain.impl.JsonSchemaDocumentDefinition;
import com.ritense.document.domain.impl.JsonSchemaDocumentId;
import com.ritense.document.domain.impl.JsonSchemaDocumentVersion;
import com.ritense.document.domain.impl.JsonSchemaRelatedFile;
import com.ritense.document.domain.impl.relation.JsonSchemaDocumentRelation;
import com.ritense.document.domain.impl.request.DocumentRelationRequest;
import com.ritense.document.domain.impl.request.ModifyDocumentRequest;
import com.ritense.document.domain.impl.request.NewDocumentRequest;
import com.ritense.document.domain.relation.DocumentRelation;
import com.ritense.document.exception.DocumentNotFoundException;
import com.ritense.document.exception.UnknownDocumentDefinitionException;
import com.ritense.document.repository.DocumentRepository;
import com.ritense.document.service.DocumentService;
import com.ritense.resource.service.ResourceService;
import com.ritense.valtimo.contract.resource.Resource;
import com.ritense.valtimo.contract.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public class JsonSchemaDocumentService implements DocumentService {

    private final DocumentRepository documentRepository;
    private final JsonSchemaDocumentDefinitionService documentDefinitionService;
    private final JsonSchemaDocumentDefinitionSequenceGeneratorService documentSequenceGeneratorService;
    private final ResourceService resourceService;

    @Override
    public Optional<JsonSchemaDocument> findBy(Document.Id documentId) {
        return documentRepository.findById(documentId);
    }

    @Override
    public Page<JsonSchemaDocument> getAllByDocumentDefinitionName(Pageable pageable, String definitionName) {
        return documentRepository.findAllByDocumentDefinitionIdName(pageable, definitionName);
    }

    @Override
    public Page<JsonSchemaDocument> getAll(Pageable pageable) {
        return documentRepository.findAll(pageable);
    }

    @Override
    @Transactional
    public JsonSchemaDocument.CreateDocumentResultImpl createDocument(
        NewDocumentRequest newDocumentRequest
    ) {
        final JsonSchemaDocumentDefinition definition = documentDefinitionService
            .findLatestByName(newDocumentRequest.documentDefinitionName())
            .orElseThrow(() -> new UnknownDocumentDefinitionException(newDocumentRequest.documentDefinitionName()));
        final var content = JsonDocumentContent.build(newDocumentRequest.content());
        final var user = SecurityUtils.getCurrentUserLogin();

        final var result = JsonSchemaDocument.create(
            definition,
            content,
            user,
            documentSequenceGeneratorService,
            JsonSchemaDocumentRelation.from(newDocumentRequest.documentRelation())
        );
        result.resultingDocument().ifPresent(documentRepository::saveAndFlush);
        return result;
    }

    @Override
    @Transactional
    public synchronized JsonSchemaDocument.ModifyDocumentResultImpl modifyDocument(
        ModifyDocumentRequest request
    ) {
        final var documentId = JsonSchemaDocumentId.existingId(UUID.fromString(request.documentId()));
        final var version = JsonSchemaDocumentVersion.from(request.versionBasedOn());
        final var document = findBy(documentId)
            .orElseThrow(() -> new DocumentNotFoundException("Document not found with id " + request.documentId()));

        final var modifiedContent = JsonDocumentContent.build(
            document.content().asJson(),
            request.content(),
            request.jsonPatch()
        );
        var documentDefinition = documentDefinitionService.findBy(document.definitionId()).orElseThrow();
        final var result = document.applyModifiedContent(
            modifiedContent,
            documentDefinition,
            version
        );

        result.resultingDocument().ifPresent(documentRepository::saveAndFlush);
        return result;
    }

    @Override
    @Transactional
    public void assignDocumentRelation(
        Document.Id documentId,
        DocumentRelation documentRelation
    ) {
        final JsonSchemaDocumentRelation jsonSchemaDocumentRelation = JsonSchemaDocumentRelation.from(
            new DocumentRelationRequest(
                UUID.fromString(documentRelation.id().toString()),
                documentRelation.relationType()
            )
        );
        findBy(documentId)
            .ifPresent(document -> documentRepository.save(document.addRelatedDocument(jsonSchemaDocumentRelation)));
    }

    @Override
    @Transactional
    public void assignRelatedFile(
        final Document.Id documentId,
        final RelatedFile relatedFile
    ) {
        JsonSchemaDocument document = getDocumentBy(documentId);
        document.addRelatedFile(JsonSchemaRelatedFile.from(relatedFile));
        documentRepository.save(document);
    }

    @Override
    @Transactional
    public void assignResource(Document.Id documentId, UUID resourceId) {
        JsonSchemaDocument document = getDocumentBy(documentId);
        final Resource resource = resourceService.getResource(resourceId);
        document.addRelatedFile(JsonSchemaRelatedFile.from(resource).withCreatedBy(SecurityUtils.getCurrentUserLogin()));
        documentRepository.save(document);
    }

    @Override
    @Transactional
    public void removeRelatedFile(Document.Id documentId, UUID fileId) {
        JsonSchemaDocument document = getDocumentBy(documentId);
        document.removeRelatedFileBy(fileId);
        documentRepository.save(document);
    }

    public JsonSchemaDocument getDocumentBy(Document.Id documentId) {
        return findBy(documentId)
            .orElseThrow(() -> new DocumentNotFoundException("Unable to find document with ID " + documentId));
    }

    @Override
    public void removeDocuments(String documentDefinitionName) {
        List<JsonSchemaDocument> documents = getAllByDocumentDefinitionName(Pageable.unpaged(), documentDefinitionName).toList();
        if (!documents.isEmpty()) {
            documents.forEach(JsonSchemaDocument::removeAllRelatedFiles);
            documentRepository.saveAll(documents);
            documentRepository.deleteAll(documents);
            documentSequenceGeneratorService.deleteSequenceRecordBy(documentDefinitionName);
        }
    }
}