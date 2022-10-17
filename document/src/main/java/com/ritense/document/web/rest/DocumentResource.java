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

package com.ritense.document.web.rest;

import com.ritense.document.domain.Document;
import com.ritense.document.domain.impl.request.ModifyDocumentRequest;
import com.ritense.document.domain.impl.request.NewDocumentRequest;
import com.ritense.document.domain.impl.request.UpdateAssigneeRequest;
import com.ritense.document.service.result.CreateDocumentResult;
import com.ritense.document.service.result.ModifyDocumentResult;
import com.ritense.valtimo.contract.authentication.NamedUser;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

public interface DocumentResource {

    ResponseEntity<? extends Document> getDocument(UUID id);

    ResponseEntity<CreateDocumentResult> createNewDocument(NewDocumentRequest request);

    ResponseEntity<ModifyDocumentResult> modifyDocumentContent(ModifyDocumentRequest request);

    ResponseEntity<Void> assignResource(UUID documentId, UUID resourceId);

    ResponseEntity<Void> removeRelatedFile(UUID documentId, UUID resourceId);

    ResponseEntity<Void> assignHandlerToDocument(UUID documentId, UpdateAssigneeRequest request);

    ResponseEntity<List<NamedUser>> getCandidateUsers(UUID documentId);

}
