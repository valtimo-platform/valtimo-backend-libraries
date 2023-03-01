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

package com.ritense.document.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.ritense.document.BaseTest;
import com.ritense.document.domain.event.DocumentFieldChangedEvent;
import com.ritense.document.domain.impl.JsonDocumentContent;
import com.ritense.document.domain.impl.JsonSchemaDocumentDefinition;
import com.ritense.document.domain.impl.JsonSchemaDocumentFieldChangedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import static org.assertj.core.api.Assertions.assertThat;

public class JsonSchemaJsonDifferenceServiceTest extends BaseTest {

    private JsonSchemaDocumentDefinition definition;

    @BeforeEach
    public void setUp() {
        definition = definitionOf("person");
    }

    @Test
    public void shouldNotHaveRemoveDifferences() {
        final var existingJsonContent = new JsonDocumentContent("{\"firstName\": \"John\", \"age\": 18}");
        final var existingDocument = createDocument(definition, existingJsonContent).resultingDocument().orElseThrow();

        final var proposedJsonContent = new JsonDocumentContent("{\"firstName\": \"Richard\", \"age\": 19}}");
        final var proposedDocument = createDocument(definition, proposedJsonContent).resultingDocument().orElseThrow();

        final JsonNode diff = existingDocument.content().diff(proposedDocument.content());
        final List<JsonSchemaDocumentFieldChangedEvent> operations = StreamSupport.stream(diff.spliterator(), false)
            .map(JsonSchemaDocumentFieldChangedEvent::fromJsonNode).collect(Collectors.toList());

        assertThat(operations).isNotNull();
        assertThat(operations).hasSize(2);
        assertThat(operations)
            .extracting(DocumentFieldChangedEvent::type)
            .contains("replace")
            .doesNotContain("add", "remove", "copy", "move");
    }

    @Test
    public void shouldHaveJohnWithAge18Differences() {
        final var existingJsonContent = new JsonDocumentContent("{\"firstName\": \"Richard\"}");
        final var existingDocument = createDocument(definition, existingJsonContent).resultingDocument().orElseThrow();

        final var proposedJsonContent = new JsonDocumentContent("{\"firstName\": \"John\", \"age\": 18}");
        final var proposedDocument = createDocument(definition, proposedJsonContent).resultingDocument().orElseThrow();

        final JsonNode diff = existingDocument.content().diff(proposedDocument.content());
        final List<JsonSchemaDocumentFieldChangedEvent> operations = StreamSupport.stream(diff.spliterator(), false)
            .map(JsonSchemaDocumentFieldChangedEvent::fromJsonNode).collect(Collectors.toList());

        assertThat(operations).isNotNull();
        assertThat(operations)
            .extracting(DocumentFieldChangedEvent::type)
            .contains("replace", "add")
            .doesNotContain("remove", "copy", "move");
    }

    @Test
    public void shouldNotHaveDifferences() {
        final var content = new JsonDocumentContent("{\"firstName\": \"John\"}");
        final var existingDocument = createDocument(definition, content).resultingDocument().orElseThrow();
        final var proposedDocument = createDocument(definition, content).resultingDocument().orElseThrow();

        final JsonNode diff = existingDocument.content().diff(proposedDocument.content());
        final List<JsonSchemaDocumentFieldChangedEvent> operations = StreamSupport.stream(diff.spliterator(), false)
            .map(JsonSchemaDocumentFieldChangedEvent::fromJsonNode).collect(Collectors.toList());

        assertThat(operations).isNotNull();
        assertThat(operations.stream()).isEmpty();
    }

    @Test
    public void shouldHaveReplaceItemValueDifference() {
        final var content = new JsonDocumentContent("{\"items\": [\"John\"] }");
        final var existingDocument = createDocument(definition, content).resultingDocument().orElseThrow();

        final var content2 = new JsonDocumentContent("{\"items\": [\"Tom\"] }");
        final var proposedDocument = createDocument(definition, content2).resultingDocument().orElseThrow();

        final JsonNode diff = existingDocument.content().diff(proposedDocument.content());
        final List<JsonSchemaDocumentFieldChangedEvent> operations = StreamSupport.stream(diff.spliterator(), false)
            .map(JsonSchemaDocumentFieldChangedEvent::fromJsonNode).collect(Collectors.toList());

        assertThat(operations).isNotNull();
        assertThat(operations)
            .extracting(DocumentFieldChangedEvent::type)
            .contains("replace")
            .doesNotContain("remove", "copy", "move", "add");
    }

}