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

import com.ritense.document.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class JsonSchemaDocumentRelatedFilesTest extends BaseTest {

    private JsonSchemaDocumentDefinition definition;

    @BeforeEach
    public void setUp() {
        definition = definitionOf("person");
    }

    @Test
    public void shouldAddRelatedFileToRelatedFiles() {
        final var content = new JsonDocumentContent("{\"firstName\": \"John\"}");
        final var createResult = createDocument(definition, content);

        final JsonSchemaDocument document = createResult.resultingDocument().orElseThrow();

        assertThat(document.relatedFiles()).isEmpty();
        document.addRelatedFile(relatedFile());
        assertThat(document.relatedFiles()).hasSize(1);
    }

    @Test
    public void shouldRemoveRelatedFile() {
        final var content = new JsonDocumentContent("{\"firstName\": \"John\"}");
        final var createResult = createDocument(definition, content);

        final JsonSchemaDocument document = createResult.resultingDocument().orElseThrow();

        assertThat(document.relatedFiles()).isEmpty();

        final JsonSchemaRelatedFile relatedFile = relatedFile();

        document.addRelatedFile(relatedFile);
        assertThat(document.relatedFiles()).hasSize(1);
        document.removeRelatedFileBy(relatedFile.getFileId());
        assertThat(document.relatedFiles()).hasSize(0);
    }

}