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
import com.ritense.document.repository.CustomDocumentUpdate;
import com.vladmihalcea.hibernate.type.json.JsonStringType;
import org.hibernate.query.NativeQuery;

import javax.persistence.EntityManager;

public class CustomDocumentUpdateImpl implements CustomDocumentUpdate {

    private EntityManager entityManager;

    public CustomDocumentUpdateImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public void updateByTenant(Document document, String tenantId) {
        var updateQuery = entityManager.createNativeQuery(" " +
            " UPDATE JsonSchemaDocument doc " +
            " SET    doc.content = :content " +
            " ,      doc.document_definition_name = :documentDefinitionName " +
            " ,      doc.document_definition_version = :documentDefinitionVersion " +
            " ,      doc.modifiedOn = :modifiedOn " +
            " ,      doc.assigneeId = :assigneeId " +
            " ,      doc.assigneeFullName = :assigneeFullName " +
            " ,      doc.documentRelations = :documentRelations " +
            " ,      doc.relatedFiles = :relatedFiles " +
            " WHERE  doc.id = :id " +
            " AND    doc.tenantId = :tenantId"
        ).unwrap(NativeQuery.class);
        updateQuery.setParameter("content", document.content().asJson(), JsonStringType.INSTANCE);
        updateQuery.setParameter("documentDefinitionName", document.definitionId().name());
        updateQuery.setParameter("documentDefinitionVersion", document.definitionId().version());
        updateQuery.setParameter("modifiedOn", document.modifiedOn());
        updateQuery.setParameter("assigneeId", document.assigneeId());
        updateQuery.setParameter("assigneeFullName", document.assigneeFullName());
        updateQuery.setParameter("documentRelations", document.relations(), JsonStringType.INSTANCE);
        updateQuery.setParameter("relatedFiles", document.relatedFiles(), JsonStringType.INSTANCE);
        updateQuery.setParameter("id", document.id().getId());
        updateQuery.setParameter("tenantId", document.tenantId());
        updateQuery.executeUpdate();
    }

}