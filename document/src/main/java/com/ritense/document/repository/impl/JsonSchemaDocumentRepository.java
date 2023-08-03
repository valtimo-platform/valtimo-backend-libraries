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

import com.ritense.document.domain.impl.*;
import com.ritense.document.domain.impl.relation.JsonSchemaDocumentRelation;
import com.ritense.document.repository.DocumentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Set;

@Repository
public interface JsonSchemaDocumentRepository extends DocumentRepository<JsonSchemaDocument> {

    Page<JsonSchemaDocument> findAllByDocumentDefinitionIdName(Pageable pageable, String definitionName);

    @Query(" SELECT  doc " +
        "    FROM    JsonSchemaDocument doc " +
        "    WHERE   (:definitionName IS NULL OR doc.documentDefinitionId.name = :definitionName)" +
        "    AND     (:searchCriteria IS NULL OR JSON_SEARCH(LOWER(doc.content), 'all', LOWER(CONCAT('%', :searchCriteria, '%'))) IS NOT NULL)" +
        "    AND     (:sequence IS NULL OR doc.sequence = :sequence)" +
        "    AND     (:createdBy IS NULL OR doc.createdBy = :createdBy)")
    Page<JsonSchemaDocument> searchByCriteria(
        @Param("definitionName") String definitionName,
        @Param("searchCriteria") String searchCriteria,
        @Param("sequence") Long sequence,
        @Param("createdBy") String createdBy,
        Pageable pageable
    );

    Long countByDocumentDefinitionIdNameAndAssigneeId(String definitionName, String assigneeId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("" +
        " INSERT INTO JsonSchemaDocument doc ( doc.id " +
        " ,      doc.content " +
        " ,      doc.documentDefinitionId " +
        " ,      doc.createdOn " +
        " ,      doc.createdBy " +
        " ,      doc.assigneeId " +
        " ,      doc.assigneeFullName " +
        " ,      doc.documentRelations " +
        " ,      doc.relatedFiles" +
        " ,      doc.tenantId )" +
        " VALUES ( :id " +
        " ,      :id" +
        " ,      :content" +
        " ,      :documentDefinitionId" +
        " ,      :createdOn" +
        " ,      :createdBy" +
        " ,      :assigneeId" +
        " ,      :assigneeFullName" +
        " ,      :documentRelations" +
        " ,      :relatedFiles" +
        " ,      :tenantId) "
    )
    JsonSchemaDocument insertDocument(
        @Param("id") JsonSchemaDocumentId jsonSchemaDocumentId,
        @Param("content") JsonDocumentContent content,
        @Param("documentDefinitionId") JsonSchemaDocumentDefinitionId documentDefinitionId,
        @Param("createdOn") LocalDateTime createdOn,
        @Param("createdBy") String createdBy,
        @Param("assigneeId") String assigneeId,
        @Param("assigneeFullName") String assigneeFullName,
        @Param("documentRelations") Set<JsonSchemaDocumentRelation> documentRelations,
        @Param("relatedFiles") Set<JsonSchemaRelatedFile> relatedFiles,
        @Param("tenantId") String tenantId
    );

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("" +
        " UPDATE JsonSchemaDocument doc " +
        " SET    doc.content = :content " +
        " ,      doc.documentDefinitionId = :documentDefinitionId " +
        " ,      doc.modifiedOn = :modifiedOn " +
        " ,      doc.assigneeId = :assigneeId " +
        " ,      doc.assigneeFullName = :assigneeFullName " +
        " ,      doc.documentRelations = :documentRelations " +
        " ,      doc.relatedFiles = :relatedFiles " +
        " WHERE  doc.id = :id " +
        " AND    doc.tenantId = :tenantId"
    )
    JsonSchemaDocument updateDocument(
        @Param("id") JsonSchemaDocumentId jsonSchemaDocumentId,
        @Param("content") JsonDocumentContent content,
        @Param("documentDefinitionId") JsonSchemaDocumentDefinitionId documentDefinitionId,
        @Param("modifiedOn") LocalDateTime modifiedOn,
        @Param("assigneeId") String assigneeId,
        @Param("assigneeFullName") String assigneeFullName,
        @Param("documentRelations") Set<JsonSchemaDocumentRelation> documentRelations,
        @Param("relatedFiles") Set<JsonSchemaRelatedFile> relatedFiles,
        @Param("tenantId") String tenantId
    );
}
