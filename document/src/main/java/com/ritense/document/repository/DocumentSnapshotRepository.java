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

package com.ritense.document.repository;

import com.ritense.document.domain.impl.JsonSchemaDocumentId;
import com.ritense.document.domain.snapshot.DocumentSnapshot;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;
import java.time.LocalDateTime;
import java.util.List;

@NoRepositoryBean
public interface DocumentSnapshotRepository<T extends DocumentSnapshot> extends JpaRepository<T, DocumentSnapshot.Id> {

    Page<T> getDocumentSnapshots(
        String definitionName,
        JsonSchemaDocumentId documentId,
        LocalDateTime fromDateTime,
        LocalDateTime toDateTime,
//        List<String> roles,
        Pageable pageable
    );

    void deleteAllByDefinitionName(String definitionName);
}