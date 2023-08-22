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

package com.ritense.document.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.ritense.document.domain.Document;
import com.ritense.document.domain.RelatedFile;
import com.ritense.document.domain.impl.request.ModifyDocumentRequest;
import com.ritense.document.domain.impl.request.NewDocumentRequest;
import com.ritense.document.domain.relation.DocumentRelation;
import com.ritense.document.service.result.CreateDocumentResult;
import com.ritense.document.service.result.ModifyDocumentResult;
import com.ritense.valtimo.contract.authentication.NamedUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface DocumentService {

    Page<? extends Document> getAll(Pageable pageable);

    Page<? extends Document> getAllByDocumentDefinitionName(Pageable pageable, String definitionName);

    Optional<? extends Document> findBy(Document.Id documentId, String tenantId);

    Document get(String documentId, String tenantId);

    CreateDocumentResult createDocument(NewDocumentRequest newDocumentRequest);

    void modifyDocument(Document document, JsonNode jsonNode, String tenantId);

    ModifyDocumentResult modifyDocument(ModifyDocumentRequest modifyDocumentRequest);

    void assignDocumentRelation(Document.Id documentId, DocumentRelation documentRelation, String tenantId);

    void assignRelatedFile(Document.Id documentId, RelatedFile relatedFile, String tenantId);

    void assignResource(Document.Id documentId, UUID resourceId, String tenantId);

    void assignResource(Document.Id documentId, UUID resourceId, Map<String, Object> metadata, String tenantId);

    void removeRelatedFile(Document.Id documentId, UUID fileId, String tenantId);

    void removeDocuments(String documentDefinitionName);

    boolean currentUserCanAccessDocument(Document.Id documentId, String tenantId);

    void assignUserToDocument(UUID documentId, String assigneeId, String tenantId);

    void unassignUserFromDocument(UUID documentId, String tenantId);

    Set<String> getDocumentRoles(Document.Id documentId, String tenantId);

    List<NamedUser> getCandidateUsers(Document.Id documentId, String tenantId);
}
