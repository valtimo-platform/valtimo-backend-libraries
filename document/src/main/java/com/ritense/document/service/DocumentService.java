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

package com.ritense.document.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.ritense.document.domain.Document;
import com.ritense.document.domain.RelatedFile;
import com.ritense.document.domain.impl.request.ModifyDocumentRequest;
import com.ritense.document.domain.impl.request.NewDocumentRequest;
import com.ritense.document.domain.relation.DocumentRelation;
import com.ritense.document.service.result.CreateDocumentResult;
import com.ritense.document.service.result.ModifyDocumentResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;
import java.util.UUID;

public interface DocumentService {

    Page<? extends Document> getAll(Pageable pageable);

    Page<? extends Document> getAllByDocumentDefinitionName(Pageable pageable, String definitionName);

    Optional<? extends Document> findBy(Document.Id documentId);

    Document get(String documentId);

    CreateDocumentResult createDocument(NewDocumentRequest newDocumentRequest);

    void modifyDocument(Document document, JsonNode jsonNode);

    ModifyDocumentResult modifyDocument(ModifyDocumentRequest modifyDocumentRequest);

    void assignDocumentRelation(Document.Id documentId, DocumentRelation documentRelation);

    void assignRelatedFile(Document.Id documentId, RelatedFile relatedFile);

    void assignResource(Document.Id documentId, UUID resourceId);

    void removeRelatedFile(Document.Id documentId, UUID fileId);

    void removeDocuments(String documentDefinitionName);

}