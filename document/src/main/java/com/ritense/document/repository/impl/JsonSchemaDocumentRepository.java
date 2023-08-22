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

import com.ritense.document.domain.Document;
import com.ritense.document.domain.impl.JsonSchemaDocument;
import com.ritense.document.repository.DocumentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JsonSchemaDocumentRepository extends DocumentRepository<JsonSchemaDocument>,
    JpaSpecificationExecutor<JsonSchemaDocument> {

    Optional<JsonSchemaDocument> findByIdAndTenantId(
        Document.Id documentId,
        String tenantId
    );

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

}