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
import com.ritense.document.repository.CustomDocumentInsert;
import com.vladmihalcea.hibernate.type.json.JsonStringType;
import org.hibernate.query.NativeQuery;

import javax.persistence.EntityManager;

public class CustomDocumentInsertImpl implements CustomDocumentInsert {

    private EntityManager entityManager;

    public CustomDocumentInsertImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public void insertForTenant(Document document, String tenantId) {
        var insertQuery = entityManager.createNativeQuery(" " +
            " INSERT INTO   json_schema_document doc " +
            " (             doc.json_schema_document_id " +
            " ,             doc.json_document_content " +
            " ,             doc.document_definition_name " +
            " ,             doc.document_definition_version " +
            " ,             doc.created_on " +
            " ,             doc.created_by " +
            " ,             doc.assignee_id " +
            " ,             doc.assignee_full_name " +
            " ,             doc.document_relations " +
            " ,             doc.related_files" +
            " ,             doc.tenant_id )" +
            " VALUES (      :id" +
            " ,             :content" +
            " ,             :documentDefinitionName" +
            " ,             :documentDefinitionVersion" +
            " ,             :createdOn" +
            " ,             :createdBy" +
            " ,             :assigneeId" +
            " ,             :assigneeFullName" +
            " ,             :documentRelations" +
            " ,             :relatedFiles" +
            " ,             :tenantId "
            ).unwrap(NativeQuery.class);
        insertQuery.setParameter("id", document.id().getId());
        insertQuery.setParameter("content", document.content().asJson(), JsonStringType.INSTANCE);
        insertQuery.setParameter("documentDefinitionName", document.definitionId().name());
        insertQuery.setParameter("documentDefinitionVersion", document.definitionId().version());
        insertQuery.setParameter("createdOn", document.createdOn());
        insertQuery.setParameter("createdBy", document.createdBy());
        insertQuery.setParameter("assigneeId", document.assigneeId());
        insertQuery.setParameter("assigneeFullName", document.assigneeFullName());
        insertQuery.setParameter("documentRelations", document.relations(), JsonStringType.INSTANCE);
        insertQuery.setParameter("relatedFiles", document.relatedFiles(), JsonStringType.INSTANCE);
        insertQuery.setParameter("tenantId", document.tenantId());
        insertQuery.executeUpdate();
    }

}