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

package com.ritense.form;

import com.ritense.document.domain.impl.JsonSchema;
import com.ritense.document.domain.impl.JsonSchemaDocumentDefinition;
import com.ritense.document.domain.impl.JsonSchemaDocumentDefinitionId;
import com.ritense.form.domain.FormIoFormDefinition;
import com.ritense.form.domain.FormSpringContextHelper;
import com.ritense.valtimo.contract.form.FormFieldDataResolver;
import org.apache.commons.io.IOUtils;
import org.springframework.context.ApplicationContext;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public abstract class BaseTest {

    protected static final String DEFAULT_FORM_DEFINITION_NAME = "Form1";

    protected static void mockSpringContextHelper() {
        var applicationContext = mock(ApplicationContext.class);
        var formFieldDataResolver = mock(FormFieldDataResolver.class);
        when(formFieldDataResolver.supports(eq("oz"))).thenReturn(true);
        when(applicationContext.getBeansOfType(FormFieldDataResolver.class)).thenReturn(Map.of("Test", formFieldDataResolver));
        FormSpringContextHelper.setContext(applicationContext);
    }

    protected FormIoFormDefinition formDefinition() {
        return new FormIoFormDefinition(UUID.randomUUID(), DEFAULT_FORM_DEFINITION_NAME, "{}", false);
    }

    protected FormIoFormDefinition formDefinition(UUID id, String formName) {
        return new FormIoFormDefinition(id, formName, "{}", false);
    }

    protected FormIoFormDefinition formDefinitionOf(String formDefinitionId) throws IOException {
        var s = IOUtils.toString(
            Thread.currentThread().getContextClassLoader().getResourceAsStream("config/form/" + formDefinitionId + ".json"),
            StandardCharsets.UTF_8
        );
        return new FormIoFormDefinition(UUID.randomUUID(), "form-example", s, false);
    }

    protected JsonSchemaDocumentDefinition definition() {
        final JsonSchemaDocumentDefinitionId jsonSchemaDocumentDefinitionId = JsonSchemaDocumentDefinitionId.newId("person");
        final JsonSchema jsonSchema = JsonSchema.fromResourceUri(path(jsonSchemaDocumentDefinitionId.name()));
        return new JsonSchemaDocumentDefinition(jsonSchemaDocumentDefinitionId, jsonSchema);
    }

    public URI path(String name) {
        return URI.create(String.format("config/document/definition/%s.json", name + ".schema"));
    }
}