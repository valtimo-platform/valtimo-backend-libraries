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
import com.fasterxml.jackson.databind.JsonNode;
import com.ritense.document.config.validator.UuidValidator;
import com.ritense.document.domain.DocumentContent;
import com.ritense.document.domain.impl.meta.MetaJsonSchemaV7Draft;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.everit.json.schema.ReadWriteContext;
import org.everit.json.schema.Schema;
import org.everit.json.schema.Validator;
import org.everit.json.schema.loader.SchemaClient;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.data.annotation.Transient;
import org.springframework.util.StreamUtils;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import static com.ritense.valtimo.contract.utils.AssertionConcern.assertArgumentNotEmpty;
import static com.ritense.valtimo.contract.utils.AssertionConcern.assertArgumentNotNull;

@Embeddable
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public final class JsonSchema {

    @Transient
    private static final transient String DEFAULT_REFERENCE_PATH_LOCATION = "classpath://config/document/definition/reference/";

    @Transient
    private static final transient Validator VALIDATOR = Validator.builder()
        .readWriteContext(ReadWriteContext.WRITE)
        .build();

    @Transient
    private static final transient SchemaLoader.SchemaLoaderBuilder SCHEMA_LOADER_BUILDER = SchemaLoader.builder()
        .schemaClient(SchemaClient.classPathAwareClient())
        .resolutionScope(DEFAULT_REFERENCE_PATH_LOCATION)
        .useDefaults(true)
        .draftV7Support()
        .addFormatValidator(new UuidValidator());

    @Column(name = "json_schema", columnDefinition = "json")
    private String schema;

    public static JsonSchema fromString(String schema) {
        assertArgumentNotNull(schema, "schema is required");
        return new JsonSchema(schema);
    }

    public static JsonSchema fromResource(URI resource) {
        assertArgumentNotNull(resource, "resource is required");
        try {
            final String rawJson = getResourceAsString(resource.toString());
            return new JsonSchema(rawJson);
        } catch (IOException ex) {
            throw new RuntimeException("Cannot build schema from resource", ex);
        }
    }

    public static JsonSchema fromStream(InputStream stream) {
        assertArgumentNotNull(stream, "stream is required");
        try {
            final String rawJson = StreamUtils.copyToString(stream, StandardCharsets.UTF_8);
            return new JsonSchema(rawJson);
        } catch (IOException ex) {
            throw new RuntimeException("Cannot build schema from stream", ex);
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
        final SchemaLoader schemaLoader = SCHEMA_LOADER_BUILDER
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

    private static String getResourceAsString(String resource) throws IOException {
        return IOUtils.toString(getResourceAsStream(resource));
    }

    private static InputStream getResourceAsStream(String resource) {
        return Thread.currentThread().getContextClassLoader().getResourceAsStream(resource);
    }

}