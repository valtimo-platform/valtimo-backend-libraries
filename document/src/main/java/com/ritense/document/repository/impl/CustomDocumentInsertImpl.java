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

import com.ritense.document.domain.impl.JsonSchemaDocument;
import com.ritense.document.repository.CustomDocumentInsert;
import org.hibernate.query.NativeQuery;
import org.springframework.beans.factory.annotation.Value;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

@Transactional
public class CustomDocumentInsertImpl extends AbstractDbUtil implements CustomDocumentInsert {

    private EntityManager entityManager;

    public CustomDocumentInsertImpl(
        EntityManager entityManager,
        @Value("${valtimo.database:mysql}") String dbType
    ) {
        this.entityManager = entityManager;
        this.dbType = dbType;
    }

    public void insert(JsonSchemaDocument document) {
        var insertQuery = entityManager.createNativeQuery(" " +
            " INSERT INTO   json_schema_document " +
            " (             json_schema_document_id " +
            " ,             json_document_content " +
            " ,             document_definition_name " +
            " ,             document_definition_version " +
            " ,             sequence" +
            " ,             created_on " +
            " ,             created_by " +
            " ,             assignee_id " +
            " ,             assignee_full_name " +
            " ,             document_relations " +
            " ,             related_files" +
            " ,             tenant_id )" +
            " VALUES (      :id" +
            " ,             :content" +
            " ,             :documentDefinitionName" +
            " ,             :documentDefinitionVersion" +
            " ,             :sequence" +
            " ,             :createdOn" +
            " ,             :createdBy" +
            " ,             :assigneeId" +
            " ,             :assigneeFullName" +
            " ,             :documentRelations" +
            " ,             :relatedFiles" +
            " ,             :tenantId ) "
        ).unwrap(NativeQuery.class);

        insertQuery.setParameter("id", document.id().getId());
        insertQuery.setParameter("content", document.content().asJson(), getJsonType());
        insertQuery.setParameter("documentDefinitionName", document.definitionId().name());
        insertQuery.setParameter("documentDefinitionVersion", document.definitionId().version());
        insertQuery.setParameter("sequence", document.sequence());
        insertQuery.setParameter("createdOn", document.createdOn());
        insertQuery.setParameter("createdBy", document.createdBy());
        insertQuery.setParameter("assigneeId", document.assigneeId());
        insertQuery.setParameter("assigneeFullName", document.assigneeFullName());
        insertQuery.setParameter("documentRelations", document.relations(), getJsonType());
        insertQuery.setParameter("relatedFiles", document.relatedFiles(), getJsonType());
        insertQuery.setParameter("tenantId", document.tenantId());
        final var result = insertQuery.executeUpdate();
        assert result == 1;
    }

}