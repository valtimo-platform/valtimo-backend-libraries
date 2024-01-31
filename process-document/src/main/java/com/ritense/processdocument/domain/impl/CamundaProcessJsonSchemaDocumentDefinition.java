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

package com.ritense.processdocument.domain.impl;

import static com.ritense.valtimo.contract.utils.AssertionConcern.assertArgumentNotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ritense.processdocument.domain.ProcessDocumentDefinition;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.Objects;
import org.hibernate.annotations.Formula;
import org.springframework.data.domain.Persistable;

@Entity
@Table(name = "camunda_process_json_schema_document_definition")
public class CamundaProcessJsonSchemaDocumentDefinition
    implements ProcessDocumentDefinition, Persistable<CamundaProcessJsonSchemaDocumentDefinitionId> {

    @EmbeddedId
    private CamundaProcessJsonSchemaDocumentDefinitionId processDocumentDefinitionId;

    @Column(name = "can_initialize_document", columnDefinition = "BOOLEAN")
    private boolean canInitializeDocument = false;

    @Column(name = "startableByUser", columnDefinition = "BOOLEAN")
    private boolean startableByUser;

    @Formula("( " +
        " SELECT   act_re_procdef.name_ " +
        " FROM     act_re_procdef " +
        " WHERE    act_re_procdef.key_ = camunda_process_definition_key" +
        " ORDER BY act_re_procdef.version_ DESC" +
        " LIMIT    1)")
    private String processName;

    @Formula("( " +
        " SELECT   act_re_procdef.id_ " +
        " FROM     act_re_procdef " +
        " WHERE    act_re_procdef.key_ = camunda_process_definition_key" +
        " ORDER BY act_re_procdef.version_ DESC" +
        " LIMIT    1)")
    private String latestVersionId;

    public CamundaProcessJsonSchemaDocumentDefinition(
        final CamundaProcessJsonSchemaDocumentDefinitionId processDocumentDefinitionId,
        boolean canInitializeDocument,
        boolean startableByUser
    ) {
        assertArgumentNotNull(processDocumentDefinitionId, "processDocumentDefinitionId is required");
        this.processDocumentDefinitionId = processDocumentDefinitionId;
        this.canInitializeDocument = canInitializeDocument;
        this.startableByUser = startableByUser;
    }

    CamundaProcessJsonSchemaDocumentDefinition() {
    }

    @Override
    public CamundaProcessJsonSchemaDocumentDefinitionId processDocumentDefinitionId() {
        return processDocumentDefinitionId;
    }

    @Override
    public boolean canInitializeDocument() {
        return this.canInitializeDocument;
    }

    @Override
    public boolean startableByUser() {
        return this.startableByUser;
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }

    @Override
    public String processName() {
        return processName;
    }

    @JsonProperty("latestVersionId")
    public String getLatestVersionId() {
        return latestVersionId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CamundaProcessJsonSchemaDocumentDefinition)) {
            return false;
        }
        CamundaProcessJsonSchemaDocumentDefinition that = (CamundaProcessJsonSchemaDocumentDefinition) o;
        return processDocumentDefinitionId.equals(that.processDocumentDefinitionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(processDocumentDefinitionId);
    }

    @Override
    @JsonIgnore
    public CamundaProcessJsonSchemaDocumentDefinitionId getId() {
        return processDocumentDefinitionId;
    }

    @Override
    @JsonIgnore
    public boolean isNew() {
        return processDocumentDefinitionId.isNew();
    }

}