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

package com.ritense.document.domain.impl.meta;

import com.ritense.document.exception.SchemaValidationException;
import org.everit.json.schema.Schema;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.JSONTokener;
import java.io.InputStream;

public class MetaJsonSchemaV7Draft {

    private static final String DRAFT_V7_RESOURCE_NAME = "meta-schema/draftv7.json";

    private static MetaJsonSchemaV7Draft metaJsonSchemaV7Draft;

    private static Schema schema;

    private MetaJsonSchemaV7Draft() {
        final var jsonObject = new JSONObject(new JSONTokener(getResourceAsStream(DRAFT_V7_RESOURCE_NAME)));
        schema = SchemaLoader.load(jsonObject);
    }

    public void validate(String jsonSchema) {
        final JSONObject subject = new JSONObject(new JSONTokener(jsonSchema));
        if (subject.toString().isEmpty()) {
            throw new SchemaValidationException("Validating empty schema");
        }
        schema.validate(subject);
    }

    public static MetaJsonSchemaV7Draft getInstance() {
        if (metaJsonSchemaV7Draft == null) {
            metaJsonSchemaV7Draft = new MetaJsonSchemaV7Draft();
        }
        return metaJsonSchemaV7Draft;
    }

    private static InputStream getResourceAsStream(String resource) {
        return Thread.currentThread().getContextClassLoader().getResourceAsStream(resource);
    }

}