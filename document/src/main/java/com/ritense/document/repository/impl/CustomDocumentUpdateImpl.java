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
import com.ritense.document.repository.CustomDocumentUpdate;
import org.hibernate.query.NativeQuery;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

@Transactional
public class CustomDocumentUpdateImpl extends AbstractDbUtil implements CustomDocumentUpdate {

    private EntityManager entityManager;

    public CustomDocumentUpdateImpl(
        EntityManager entityManager,
        @Value("${valtimo.database:mysql}") String dbType,
        ApplicationEventPublisher applicationEventPublisher
    ) {
        this.entityManager = entityManager;
        this.dbType = dbType;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public void updateByTenant(JsonSchemaDocument document, String tenantId) {
        var updateQuery = entityManager.createNativeQuery(" " +
            " UPDATE json_schema_document " +
            " SET    json_document_content = :content " +
            " ,      document_definition_name = :documentDefinitionName " +
            " ,      document_definition_version = :documentDefinitionVersion " +
            " ,      modified_on = :modifiedOn " +
            " ,      assignee_id = :assigneeId " +
            " ,      assignee_full_name = :assigneeFullName " +
            " ,      document_relations = :documentRelations " +
            " ,      related_files = :relatedFiles " +
            " WHERE  json_schema_document_id = :id " +
            " AND    tenant_id = :tenantId"
        ).unwrap(NativeQuery.class);
        updateQuery.setParameter("content", document.content().asJson(), getJsonType());
        updateQuery.setParameter("documentDefinitionName", document.definitionId().name());
        updateQuery.setParameter("documentDefinitionVersion", document.definitionId().version());
        updateQuery.setParameter("modifiedOn", document.modifiedOn().orElseThrow());
        updateQuery.setParameter("assigneeId", document.assigneeId());
        updateQuery.setParameter("assigneeFullName", document.assigneeFullName());
        updateQuery.setParameter("documentRelations", document.relations(), getJsonType());
        updateQuery.setParameter("relatedFiles", document.relatedFiles(), getJsonType());
        updateQuery.setParameter("id", document.id().getId());
        updateQuery.setParameter("tenantId", document.tenantId());
        final var result = updateQuery.executeUpdate();
        assert result == 1;
        publishEvents(document);
    }

}