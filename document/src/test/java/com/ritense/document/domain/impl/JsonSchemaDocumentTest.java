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

package com.ritense.document.domain.impl;

import com.ritense.document.BaseTest;
import com.ritense.document.domain.DocumentVersion;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class JsonSchemaDocumentTest extends BaseTest {

    @Test
    public void shouldNotCreateDocumentWithEmptyContent() {
        assertThrows(IllegalArgumentException.class, () -> new JsonDocumentContent(""));
    }

    @Test
    public void shouldBeEqualObjects() {
        final var contentOne = new JsonDocumentContent("{\"firstName\": \"Jan\"}");
        final var contentTwo = new JsonDocumentContent(contentOne);
        assertThat(contentOne).isEqualTo(contentTwo);
    }

    @Test
    public void shouldCreateDocumentWithEmptyJsonContent() {
        final var content = new JsonDocumentContent("{}");
        final var createResult = createDocument(definitionOf("person"), content);

        assertThat(createResult.errors()).isEmpty();
        assertThat(createResult.resultingDocument()).isPresent();
    }

    @Test
    public void shouldReturnNoAdditionalPropertyAllowedValidationError() {
        final var content = new JsonDocumentContent("{\"id\": \"123-123\"}");
        final var createResult = createDocument(definitionOf("additional-property-example"), content);

        assertThat(createResult.errors()).hasSize(1);
        assertThat(createResult.errors()).extracting("message").contains("#: extraneous key [id] is not permitted");
        assertThat(createResult.resultingDocument()).isNotPresent();
    }

    @Test
    public void shouldCreateDocumentWithMatchingPropertyOnly() {
        final var content = new JsonDocumentContent("{\"firstname\": \"aName\"}");
        final var createResult = createDocument(definitionOf("additional-property-example"), content);

        assertThat(createResult.errors()).isEmpty();
        assertThat(createResult.resultingDocument()).isPresent();
    }

    @Test
    public void shouldReturnNoAdditionalPropertyAllowedValidationErrorWithCombinedSchema() {
        final var content = new JsonDocumentContent("" +
            "{\"address\": " +
            "{ \"additionalProp\": \"somevalue\", " +
            "\"streetName\": \"Straatnaam\", " +
            "\"number\": \"1F\", " +
            "\"city\": \"Amsterdam\", " +
            "\"province\": \"Noord-holland\"} }"
        );
        final var createResult = createDocument(definitionOf("combined-schema-additional-property-example"), content);

        assertThat(createResult.errors()).hasSize(1);
        assertThat(createResult.errors()).extracting("message").contains("#/address: extraneous key [additionalProp] is not permitted");
        assertThat(createResult.resultingDocument()).isNotPresent();
    }

    @Test
    public void shouldCreateDocumentWithMatchingPropertiesOnlyWithCombinedSchema() {
        final var content = new JsonDocumentContent("{\"address\": {\"streetName\": \"Straatnaam\", \"number\": \"1F\", \"city\": \"Amsterdam\", \"province\": \"Noord-holland\"} }");
        final var createResult = createDocument(definitionOf("combined-schema-additional-property-example"), content);

        assertThat(createResult.errors()).isEmpty();
        assertThat(createResult.resultingDocument()).isPresent();
    }

    @Test
    public void shouldReturnReadOnlyValidationError() {
        final var content = new JsonDocumentContent("{\"firstName\": \"changed\"}");
        final var createResult = createDocument(definitionOf("readonly-example"), content);

        assertThat(createResult.errors()).hasSize(1);
        assertThat(createResult.errors()).extracting("message").contains("#/firstName: value is read-only");
        assertThat(createResult.resultingDocument()).isNotPresent();
    }

    @Test
    public void shouldReturnUuidValidationError() {
        final var content = new JsonDocumentContent("{\"id\": \"123-123\"}");
        final var createResult = createDocument(definitionOf("uuid-example"), content);

        assertThat(createResult.errors()).hasSize(1);
        assertThat(createResult.errors()).extracting("message").contains("#/id: invalid uuid [123-123]");
        assertThat(createResult.resultingDocument()).isNotPresent();
    }

    @Test
    public void shouldReturnValidUuidDocument() {
        final var content = new JsonDocumentContent("{\"id\": \"" + UUID.randomUUID() + "\"}");
        final var createResult = createDocument(definitionOf("uuid-example"), content);

        assertThat(createResult.errors()).isEmpty();
        assertThat(createResult.resultingDocument()).isPresent();
    }

    @Test
    public void shouldReturnDocumentWithDefaultValue() {
        final var content = new JsonDocumentContent("{\"firstName\": \"Jan\"}");

        final var createResult = createDocument(definitionOf("defaults-example"), content);

        assertThat(createResult.errors()).isEmpty();
        assertThat(createResult.resultingDocument()).isPresent();

        final var document = createResult.resultingDocument().orElseThrow();
        assertThat(document.content().asJson().toString()).contains("18");
    }

    @Test
    public void shouldReturnMaxLengthValidationError() {
        final var content = new JsonDocumentContent("{\"firstName\": \"Joeasdasdsadsadasdasdasdasdasdasdasd\"}");
        final var createResult = createDocument(definitionOf("person"), content);

        assertThat(createResult.errors()).hasSize(1);
        assertThat(createResult.resultingDocument()).isNotPresent();
    }

    @Test
    public void shouldReturnDocumentWithIntegerValue() {
        final var content = new JsonDocumentContent("{\"age\": 40 }");
        final var createResult = createDocument(definitionOf("person"), content);

        assertThat(createResult.errors()).isEmpty();
        assertThat(createResult.resultingDocument()).isPresent();

        final var jsonSchemaDocument = createResult.resultingDocument().orElseThrow();
        assertThat(jsonSchemaDocument.content().asJson().get("age").asInt()).isEqualTo(40);
    }

    @Test
    public void shouldReturnDocumentWithDateValue() {
        final var content = new JsonDocumentContent("{\"birthday\": \"1982-01-01\" }");
        final var createResult = createDocument(definitionOf("person"), content);

        assertThat(createResult.errors()).isEmpty();
        assertThat(createResult.resultingDocument()).isPresent();

        final var jsonSchemaDocument = createResult.resultingDocument().orElseThrow();
        assertThat(jsonSchemaDocument.content().asJson().get("birthday").asText()).isEqualTo("1982-01-01");
    }

    @Test
    public void shouldReturnDocumentWithBooleanValue() {
        final var content = new JsonDocumentContent("{\"is-cool\": true }");
        final var createResult = createDocument(definitionOf("person"), content);

        assertThat(createResult.errors()).isEmpty();
        assertThat(createResult.resultingDocument()).isPresent();

        final var jsonSchemaDocument = createResult.resultingDocument().orElseThrow();
        assertThat(jsonSchemaDocument.content().asJson().get("is-cool").asBoolean()).isTrue();
    }

    @Test
    public void shouldCreateDocument() {
        final var content = new JsonDocumentContent("{\"firstName\": \"John\"}");
        final var createResult = createDocument(definitionOf("person"), content);

        assertThat(createResult.errors()).hasSize(0);
        assertThat(createResult.resultingDocument()).isPresent();
    }

    @Test
    public void shouldModifyDocument() {
        var definition = definitionOf("person");

        final var content = new JsonDocumentContent("{\"firstName\": \"John\"}");
        final var createResult = createDocument(definition, content);

        final var document = createResult.resultingDocument().orElseThrow();

        final var contentModified = new JsonDocumentContent("{\"firstName\": \"Johnny\"}");
        final var result = document.applyModifiedContent(contentModified, definition, document.version());

        final var modifiedDocument = result.resultingDocument().orElseThrow();

        assertThat(result.errors()).hasSize(0);
        assertThat(modifiedDocument.content().asJson().toString()).contains("Johnny");
    }

    @Test
    public void shouldModifyDocumentPartial() {
        var definition = definitionOf("person");

        final var content = new JsonDocumentContent("{\"firstName\": \"John\", \"lastName\": \"Doe\"}");
        final var createResult = createDocument(definition, content);

        final var document = createResult.resultingDocument().orElseThrow();

        final var partialContentChange = new JsonDocumentContent("{\"firstName\": \"Johnny\"}");
        final var contentModified = JsonDocumentContent.build(content.asJson(), partialContentChange.asJson(), null);
        final var result = document.applyModifiedContent(contentModified, definition, document.version());

        final var modifiedDocument = result.resultingDocument().orElseThrow();

        assertThat(result.errors()).hasSize(0);
        assertThat(modifiedDocument.content().asJson().toString()).contains("Johnny");
        assertThat(modifiedDocument.content().asJson().toString()).contains("Doe");
    }

    @Test
    public void shouldNotModifyDocumentWhenVersionDiverged() {
        var definition = definitionOf("person");

        final var content = new JsonDocumentContent("{\"firstName\": \"John\"}");
        final var createResult = createDocument(definitionOf("person"), content);

        final var document = createResult.resultingDocument().orElseThrow();

        var contentModified = new JsonDocumentContent("{\"firstName\": \"Johnny\"}");
        DocumentVersion version = document.version();
        JsonSchemaDocument.ModifyDocumentResultImpl result = document.applyModifiedContent(contentModified, definition, version);

        contentModified = new JsonDocumentContent("{\"firstName\": \"Henk\"}");
        result = result.resultingDocument().orElseThrow().applyModifiedContent(contentModified, definition, version);

        assertThat(result.errors()).hasSize(1);
        assertThat(result.resultingDocument()).isEmpty();
    }

    @Test
    public void shouldAddArrayItem() {
        var definition = definitionOf("person");
        final var content = new JsonDocumentContent("{\"files\": [{\"id\" : \"1\"}]}");
        final var createResult = createDocument(definitionOf("array-example"), content);

        final var document = createResult.resultingDocument().orElseThrow();

        var modifiedContent = new JsonDocumentContent("{\"files\": [{\"id\" : \"1\"}, {\"id\" : \"2\"}]}");
        var jsonDocumentContent = JsonDocumentContent.build(
            document.content().asJson(),
            modifiedContent.asJson(),
            null
        );

        final var result = document.applyModifiedContent(jsonDocumentContent, definition, document.version());

        final var modifiedDocument = result.resultingDocument().orElseThrow();

        assertThat(result.errors()).hasSize(0);
        assertThat(modifiedDocument.content().asJson().toString()).contains("1");
        assertThat(modifiedDocument.content().asJson().toString()).contains("2");
    }

    @Test
    public void shouldEmptyArrayItems() {
        var definition = definitionOf("array-example");
        final var content = new JsonDocumentContent("{\"files\": [{\"id\" : \"1\"}, {\"id\" : \"2\"}, {\"id\" : \"3\"}]}");
        final var createResult = createDocument(definition, content);

        final var document = createResult.resultingDocument().orElseThrow();

        final var contentModified = new JsonDocumentContent("{\"files\": [{\"id\" : \"1\"}, {\"id\" : \"\"}, {\"id\" : \"\"}]}");
        final var result = document.applyModifiedContent(contentModified, definition, document.version());

        final var modifiedDocument = result.resultingDocument().orElseThrow();

        assertThat(result.errors()).hasSize(0);
        assertThat(modifiedDocument.content().asJson().toString()).contains("1");
        assertThat(modifiedDocument.content().asJson().toString()).doesNotContain("2");
        assertThat(modifiedDocument.content().asJson().toString()).doesNotContain("3");
    }

    @Test
    public void shouldEmptyAllArrayRefItems() {
        var definition = definitionOf("array-example");
        final var content = new JsonDocumentContent("{\"files\" : [{\"id\" : \"1\"}, {\"id\" : \"2\"}, {\"id\" : \"3\"}]}");
        final var createResult = createDocument(definition, content);

        final var document = createResult.resultingDocument().orElseThrow();

        final var contentModified = new JsonDocumentContent("{\"files\" : [{\"id\" : \"\"}, {\"id\" : \"\"}, {\"id\" : \"\"}]}");
        final var result = document.applyModifiedContent(contentModified, definition, document.version());

        final var modifiedDocument = result.resultingDocument().orElseThrow();

        assertThat(result.errors()).hasSize(0);
        assertThat(modifiedDocument.content().asJson().toString()).doesNotContain("1");
        assertThat(modifiedDocument.content().asJson().toString()).doesNotContain("2");
        assertThat(modifiedDocument.content().asJson().toString()).doesNotContain("3");
    }

    @Test
    public void shouldNotAllowAdditionalItemInArray() {
        var definition = definitionOf("array-example");
        final var content = new JsonDocumentContent("{\"files\" : [{\"id\" : \"1\"}, {\"id\" : \"2\"}, {\"id\" : \"3\"}]}");
        final var createResult = createDocument(definition, content);

        final var document = createResult.resultingDocument().orElseThrow();

        final var contentModified = new JsonDocumentContent("{\"files\" : [{\"id\" : \"1\"}, {\"id\" : \"2\"}, {\"id\" : \"3\"}, \"aRandom\"]}");
        final var result = document.applyModifiedContent(contentModified, definition, document.version());

        assertThat(result.errors()).hasSize(1);
    }

    @Test
    public void shouldReturnNextEnumValueConditionally() {
        final var content = new JsonDocumentContent("{\"status\": \"1\"}");
        final var createResult = createDocument(definitionOf("conditional-example"), content);

        assertThat(createResult.errors()).isEmpty();
        assertThat(createResult.resultingDocument()).isPresent();
        assertThat(createResult.resultingDocument().orElseThrow().content().asJson().toString()).contains("\"next\":\"2\"");
    }

    @Test
    public void shouldNotAllowAdditionalItemInReferencedArray() {
        var definition = definitionOf("referenced-array");
        final var content = new JsonDocumentContent("{\"addresses\" : [{\"streetName\" : \"Funenpark\"}]}");
        final var createResult = createDocument(definition, content);

        final var document = createResult.resultingDocument().orElseThrow();

        final var contentModified = new JsonDocumentContent("{\"addresses\" : [{\"streetName2\" : \"Funenpark 1F\"}]}");
        final var result = document.applyModifiedContent(contentModified, definition, document.version());

        assertThat(result.errors()).hasSize(1);
    }

}