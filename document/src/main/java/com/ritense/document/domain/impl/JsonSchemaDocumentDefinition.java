/*
 * Copyright 2015-2020 Ritense BV, the Netherlands.
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

package com.ritense.document.domain.impl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.ritense.document.domain.DocumentContent;
import com.ritense.document.domain.DocumentDefinition;
import com.ritense.document.domain.impl.event.JsonSchemaDocumentDefinitionDeployedEvent;
import com.ritense.document.domain.validation.DocumentContentValidationError;
import com.ritense.document.domain.validation.DocumentContentValidationResult;
import com.ritense.document.exception.DocumentDefinitionNameMismatchException;
import org.everit.json.schema.ValidationException;
import org.springframework.data.domain.AbstractAggregateRoot;
import org.springframework.data.domain.Persistable;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import static com.ritense.valtimo.contract.utils.AssertionConcern.assertArgumentNotNull;

@Entity
@Table(name = "json_schema_document_definition")
public class JsonSchemaDocumentDefinition extends AbstractAggregateRoot<JsonSchemaDocumentDefinition>
    implements DocumentDefinition, Persistable<JsonSchemaDocumentDefinitionId> {

    @EmbeddedId
    private JsonSchemaDocumentDefinitionId id;

    @Embedded
    private JsonSchema schema;

    @Column(name = "created_on", columnDefinition = "DATETIME", nullable = false)
    private LocalDateTime createdOn;

    @Column(name = "read_only", columnDefinition = "BIT")
    private Boolean readOnly = false;

    public JsonSchemaDocumentDefinition(
        final JsonSchemaDocumentDefinitionId id,
        final JsonSchema schema
    ) {
        assertArgumentNotNull(id, "id is required");
        assertArgumentNotNull(schema, "schema is required");
        assertMatchingSchemaIds(id, schema);
        this.id = id;
        this.schema = schema;
        this.createdOn = LocalDateTime.now();
        registerEvent(
            new JsonSchemaDocumentDefinitionDeployedEvent(
                new JsonSchemaDocumentDefinition(this)
            )
        );
    }

    private JsonSchemaDocumentDefinition(JsonSchemaDocumentDefinition another) {
        id = another.id();
        schema = another.schema;
        createdOn = another.createdOn();
    }

    JsonSchemaDocumentDefinition() {
    }

    @Override
    public JsonSchemaDocumentDefinitionId id() {
        return id;
    }

    @Override
    public LocalDateTime createdOn() {
        return createdOn;
    }

    @Override
    public JsonNode schema() {
        return schema.asJson();
    }

    public JsonSchema getSchema() {
        return schema;
    }

    public void markReadOnly() {
        this.readOnly = true;
    }

    @JsonProperty
    public boolean isReadOnly() {
        return readOnly;
    }

    @Override
    public DocumentContentValidationResult validate(DocumentContent content) {
        assertArgumentNotNull(content, "content is required");

        List<DocumentContentValidationError> errors = new ArrayList<>();
        try {
            content = schema.validateDocument(content);
        } catch (ValidationException e) {
            errors = e.getAllMessages()
                .stream()
                .map(DocumentContentValidationErrorImpl::new)
                .collect(Collectors.toList());
        }
        return new DocumentContentValidationResultImpl(errors, content);
    }

    private void assertMatchingSchemaIds(JsonSchemaDocumentDefinitionId id, JsonSchema schema) {
        if (!(id.name() + ".schema").equals(schema.getSchema().getId())) {
            throw new DocumentDefinitionNameMismatchException(id, schema);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof JsonSchemaDocumentDefinition)) {
            return false;
        }

        JsonSchemaDocumentDefinition that = (JsonSchemaDocumentDefinition) o;

        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    @JsonIgnore
    public JsonSchemaDocumentDefinitionId getId() {
        return id;
    }

    @Override
    @JsonIgnore
    public boolean isNew() {
        return id.isNew();
    }

    ///////////////////////////////////////////////////
    //// DocumentContentValidation IMPLEMENTATIONS ////
    ///////////////////////////////////////////////////

    public static class DocumentContentValidationErrorImpl implements DocumentContentValidationError {

        private final String message;

        public DocumentContentValidationErrorImpl(String message) {
            this.message = message;
        }

        @Override
        public String asString() {
            return message;
        }

    }

    public static class DocumentContentValidationResultImpl implements DocumentContentValidationResult {

        private final List<DocumentContentValidationError> validationErrors;
        private final DocumentContent content;

        public DocumentContentValidationResultImpl(List<DocumentContentValidationError> validationErrors, DocumentContent content) {
            this.validationErrors = validationErrors;
            this.content = content;
        }

        @Override
        public List<DocumentContentValidationError> validationErrors() {
            return Collections.unmodifiableList(validationErrors);
        }

        @Override
        public boolean passedValidation() {
            return validationErrors.isEmpty();
        }

        @Override
        public DocumentContent content() {
            return content;
        }

    }

}