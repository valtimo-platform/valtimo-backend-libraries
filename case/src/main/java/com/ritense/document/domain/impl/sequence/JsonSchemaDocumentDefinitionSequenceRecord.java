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

package com.ritense.document.domain.impl.sequence;

import static com.ritense.valtimo.contract.utils.AssertionConcern.assertArgumentNotNull;

import com.ritense.document.domain.sequence.DocumentDefinitionSequenceRecord;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.springframework.data.domain.Persistable;

@Entity
@Table(name = "json_schema_document_definition_sequence_record")
public class JsonSchemaDocumentDefinitionSequenceRecord
    implements DocumentDefinitionSequenceRecord, Persistable<String> {

    @Id
    @Column(name = "document_definition_name", length = 50, columnDefinition = "VARCHAR(50)", nullable = false, updatable = false)
    private String name;

    @Column(name = "sequence_value", columnDefinition = "BIGINT")
    private long sequence;

    public JsonSchemaDocumentDefinitionSequenceRecord(String jsonSchemaDocumentDefinitionName) {
        assertArgumentNotNull(jsonSchemaDocumentDefinitionName, "jsonSchemaDocumentDefinitionName is required");
        this.name = jsonSchemaDocumentDefinitionName;
        this.sequence = 1L;
    }

    private JsonSchemaDocumentDefinitionSequenceRecord() {
    }

    @Override
    public void increment() {
        this.sequence++;
    }

    @Override
    public long sequence() {
        return sequence;
    }

    @Override
    public String getId() {
        return name;
    }

    @Override
    public boolean isNew() {
        return true;
    }
}