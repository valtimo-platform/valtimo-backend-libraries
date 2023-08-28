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

package com.ritense.document.repository.impl;

import com.ritense.document.domain.impl.JsonSchemaDocumentId;
import com.ritense.document.domain.impl.snapshot.JsonSchemaDocumentSnapshot;
import com.ritense.document.repository.DocumentSnapshotRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;

@NoRepositoryBean
public interface MysqlJsonSchemaDocumentSnapshotRepository extends DocumentSnapshotRepository<JsonSchemaDocumentSnapshot> {

    @Query("" +
        "SELECT  distinct ds " +
        "FROM    JsonSchemaDocumentSnapshot ds " +
        "INNER JOIN JsonSchemaDocumentDefinitionRole ddRole ON ddRole.id.documentDefinitionName = ds.document.documentDefinitionId.name AND ddRole.id.role in :roles " +
        "WHERE   (:definitionName IS NULL OR ds.document.documentDefinitionId.name = :definitionName) " +
        "AND     (:documentId IS NULL OR ds.document.id = :documentId) " +
        "AND     (:fromDateTime IS NULL OR ds.createdOn >= :fromDateTime) " +
        "AND     (:toDateTime IS NULL OR ds.createdOn < :toDateTime) ")
    Page<JsonSchemaDocumentSnapshot> getDocumentSnapshots(
        @Param("definitionName") String definitionName,
        @Param("documentId") JsonSchemaDocumentId documentId,
        @Param("fromDateTime") LocalDateTime fromDateTime,
        @Param("toDateTime") LocalDateTime toDateTime,
        @Param("roles") List<String> roles,
        Pageable pageable
    );

    @Modifying
    @Query("" +
        "   DELETE " +
        "   FROM    JsonSchemaDocumentSnapshot ds " +
        "   WHERE   ds.document.documentDefinitionId.name = :definitionName")
    void deleteAllByDefinitionName(@Param("definitionName") String definitionName);

}