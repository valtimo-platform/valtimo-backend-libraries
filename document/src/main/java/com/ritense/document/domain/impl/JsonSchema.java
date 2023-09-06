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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.ritense.document.config.validator.UuidValidator;
import com.ritense.document.domain.DocumentContent;
import com.ritense.document.domain.impl.meta.MetaJsonSchemaV7Draft;
import org.everit.json.schema.ReadWriteContext;
import org.everit.json.schema.Schema;
import org.everit.json.schema.Validator;
import org.everit.json.schema.loader.SchemaClient;
import org.everit.json.schema.loader.SchemaLoader;
import org.hibernate.annotations.Type;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.annotation.Transient;
import org.springframework.util.StreamUtils;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import static com.ritense.valtimo.contract.utils.AssertionConcern.assertArgumentNotEmpty;
import static com.ritense.valtimo.contract.utils.AssertionConcern.assertArgumentNotNull;

@Embeddable
public final class JsonSchema {

    @Transient
    private static final String DEFAULT_REFERENCE_PATH_LOCATION = "classpath://config/document/definition/reference/";

    @Transient
    private static final Validator VALIDATOR = Validator.builder()
        .readWriteContext(ReadWriteContext.WRITE)
        .build();

    @Type(type = "com.vladmihalcea.hibernate.type.json.JsonType")
    @Column(name = "json_schema", columnDefinition = "json")
    private String schema;

    JsonSchema() {
    }

    public static JsonSchema fromString(String schema) {
        assertArgumentNotNull(schema, "schema is required");
        return new JsonSchema(schema);
    }

    public static JsonSchema fromResourceUri(URI resourceUri) {
        assertArgumentNotNull(resourceUri, "resourceUri is required");
        try {
            var classPathResource = new ClassPathResource(resourceUri.getPath());
            var rawJson = StreamUtils.copyToString(classPathResource.getInputStream(), StandardCharsets.UTF_8);
            return new JsonSchema(rawJson);
        } catch (IOException ex) {
            throw new RuntimeException(
                String.format("Cannot build schema from resource %s", resourceUri),
                ex
            );
        }
    }

    private JsonSchema(String jsonSchema) {
        assertArgumentNotEmpty(jsonSchema, "jsonSchema is required");
        MetaJsonSchemaV7Draft.getInstance().validate(jsonSchema);
        this.schema = new JSONObject(jsonSchema).toString();
    }

    JsonDocumentContent validateDocument(DocumentContent content) {
        final var jsonObject = new JSONObject(content.asJson().toString()); // Wrapped is needed for lib
        // If there are some properties missing from input which have "default" values in the schema,
        // then they will be set by the validator during validation.
        VALIDATOR.performValidation(getSchema(), jsonObject);
        return new JsonDocumentContent(jsonObject.toString());
    }

    public JsonNode asJson() {
        try {
            return Mapper.INSTANCE.get().readTree(schema);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @JsonIgnore
    public Schema getSchema() {
        final SchemaLoader schemaLoader = getSchemaLoaderBuilder()
            .schemaJson(new JSONObject(new JSONTokener(schema)))
            .build();
        return schemaLoader.load().build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof JsonSchema)) {
            return false;
        }
        JsonSchema that = (JsonSchema) o;
        return getSchema().equals(that.getSchema());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getSchema());
    }

    @Transient
    private static SchemaLoader.SchemaLoaderBuilder getSchemaLoaderBuilder() {
        return SchemaLoader.builder()
            .schemaClient(SchemaClient.classPathAwareClient())
            .resolutionScope(DEFAULT_REFERENCE_PATH_LOCATION)
            .useDefaults(true)
            .draftV7Support()
            .addFormatValidator(new UuidValidator());
    }

}
