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

import com.ritense.document.domain.DocumentDefinition;
import com.ritense.document.domain.impl.JsonSchemaDocumentDefinitionId;
import com.ritense.document.domain.sequence.DocumentDefinitionSequenceRecord;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import org.springframework.data.domain.Persistable;

@Entity
@Table(name = "json_schema_document_definition_sequence_record")
public class JsonSchemaDocumentDefinitionSequenceRecord
    implements DocumentDefinitionSequenceRecord, Persistable<JsonSchemaDocumentDefinitionId> {

    @EmbeddedId
    private JsonSchemaDocumentDefinitionId id;

    @Column(name = "sequence_value", columnDefinition = "BIGINT")
    private long sequence;

    @Version
    @Column(name = "sequence_version", columnDefinition = "BIGINT", nullable = false)
    private long version;

    public JsonSchemaDocumentDefinitionSequenceRecord(JsonSchemaDocumentDefinitionId id) {
        assertArgumentNotNull(id, "id is required");
        this.id = id;
        this.sequence = 1L;
    }

    private JsonSchemaDocumentDefinitionSequenceRecord() {
    }

    @Override
    public void increment() {
        this.sequence++;
    }

    @Override
    public DocumentDefinition.Id definitionId() {
        return id;
    }

    @Override
    public long sequence() {
        return sequence;
    }

    @Override
    public JsonSchemaDocumentDefinitionId getId() {
        return id;
    }

    @Override
    public boolean isNew() {
        return id.isNew();
    }

}