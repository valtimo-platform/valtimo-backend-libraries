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

import com.ritense.document.domain.Document;
import com.ritense.document.domain.impl.JsonSchemaDocumentId;
import com.ritense.document.domain.impl.snapshot.JsonSchemaDocumentSnapshot;
import com.ritense.document.domain.snapshot.DocumentSnapshot;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface DocumentSnapshotService {

    Optional<JsonSchemaDocumentSnapshot> findById(DocumentSnapshot.Id id);

    Page<? extends DocumentSnapshot> getDocumentSnapshots(
        String definitionName,
        JsonSchemaDocumentId documentId,
        LocalDateTime fromDateTime,
        LocalDateTime toDateTime,
        Pageable pageable
    );

    void makeSnapshot(Document.Id documentId, LocalDateTime createdOn, String createdBy);

}