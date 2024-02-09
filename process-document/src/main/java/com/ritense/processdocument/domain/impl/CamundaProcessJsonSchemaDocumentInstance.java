/*
 * Copyright 2015-2024 Ritense BV, the Netherlands.
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

import static com.ritense.valtimo.contract.utils.AssertionConcern.assertArgumentLength;
import static com.ritense.valtimo.contract.utils.AssertionConcern.assertArgumentNotEmpty;
import static com.ritense.valtimo.contract.utils.AssertionConcern.assertArgumentNotNull;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ritense.processdocument.domain.ProcessDocumentInstance;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import org.springframework.data.domain.Persistable;

@Entity
@Table(name = "camunda_process_json_schema_document_instance")
public class CamundaProcessJsonSchemaDocumentInstance
    implements ProcessDocumentInstance, Persistable<CamundaProcessJsonSchemaDocumentInstanceId> {

    @EmbeddedId
    private CamundaProcessJsonSchemaDocumentInstanceId processDocumentInstanceId;

    @Column(name = "process_name", columnDefinition = "VARCHAR(255)")
    private String processName;

    @Transient
    public boolean isActive;

    public CamundaProcessJsonSchemaDocumentInstance(
        final CamundaProcessJsonSchemaDocumentInstanceId processDocumentInstanceId,
        final String processName
    ) {
        assertArgumentNotNull(processDocumentInstanceId, "processDocumentInstanceId is required");
        if (processName != null) {
            assertArgumentNotEmpty(processName, "processName cannot be empty");
            assertArgumentLength(processName, 255, "processName max length is 255");
        }
        this.processDocumentInstanceId = processDocumentInstanceId;
        this.processName = processName;
    }

    CamundaProcessJsonSchemaDocumentInstance() {
    }

    @Override
    public CamundaProcessJsonSchemaDocumentInstanceId processDocumentInstanceId() {
        return processDocumentInstanceId;
    }

    @Override
    public String processName() {
        return this.processName;
    }

    @Override
    @JsonIgnore
    public CamundaProcessJsonSchemaDocumentInstanceId getId() {
        return processDocumentInstanceId;
    }

    @Override
    @JsonIgnore
    public boolean isNew() {
        return processDocumentInstanceId.isNew();
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    @JsonGetter
    public boolean isActive() {
        return isActive;
    }
}