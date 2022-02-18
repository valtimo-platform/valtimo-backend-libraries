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

package com.ritense.document.web.rest.impl;

import com.ritense.document.domain.impl.JsonSchemaDocumentId;
import com.ritense.document.domain.impl.snapshot.JsonSchemaDocumentSnapshotId;
import com.ritense.document.domain.snapshot.DocumentSnapshot;
import com.ritense.document.service.DocumentDefinitionService;
import com.ritense.document.service.DocumentSnapshotService;
import com.ritense.document.web.rest.DocumentSnapshotResource;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/document-snapshot", produces = MediaType.APPLICATION_JSON_VALUE)
public class JsonSchemaDocumentSnapshotResource implements DocumentSnapshotResource {

    private final DocumentSnapshotService documentSnapshotService;
    private final DocumentDefinitionService documentDefinitionService;

    @Override
    @GetMapping(value = "/{id}")
    public ResponseEntity<? extends DocumentSnapshot> getDocumentSnapshot(@PathVariable(name = "id") UUID snapshotId) {
        return documentSnapshotService.findById(JsonSchemaDocumentSnapshotId.existingId(snapshotId))
            .filter(it -> hasAccessToDefinitionName(it.document().definitionId().name()))
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @Override
    @GetMapping
    public ResponseEntity<Page<? extends DocumentSnapshot>> getDocumentSnapshots(
        @RequestParam(value = "definitionName", required = false) String definitionName,
        @RequestParam(value = "documentId", required = false) UUID documentId,
        @RequestParam(value = "fromDateTime", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDateTime,
        @RequestParam(value = "toDateTime", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDateTime,
        Pageable pageable
    ) {
        //TODO: How are we going to filter these by role?
        return ResponseEntity.ok(
            documentSnapshotService.getDocumentSnapshots(
                definitionName,
                documentId == null ? null : JsonSchemaDocumentId.existingId(documentId),
                fromDateTime,
                toDateTime,
                pageable
            )
        );
    }

    private boolean hasAccessToDefinitionName(String definitionName) {
        return documentDefinitionService.currentUserCanAccessDocumentDefinition(
            definitionName
        );
    }
}