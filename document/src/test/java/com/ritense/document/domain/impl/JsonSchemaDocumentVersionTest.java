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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class JsonSchemaDocumentVersionTest extends BaseTest {

    private JsonSchemaDocumentDefinition definition;

    @BeforeEach
    public void setUp() {
        definition = definitionOf("person");
    }

    @Test
    public void shouldHaveEqualDynamicAndPrecomputedVersions() {
        var content = new JsonDocumentContent("{\"firstName\": \"John\"}");
        var document = createDocument(definition, content).resultingDocument().orElseThrow();

        JsonSchemaDocumentVersion version = document.version();
        JsonSchemaDocumentVersion versionPrecomputed = JsonSchemaDocumentVersion.from(version.toString());

        assertEquals(version.toString(), versionPrecomputed.toString());
        assertEquals(version, versionPrecomputed);
    }

    @Test
    public void versionChangesAfterDocumentModification() {
        var content = new JsonDocumentContent("{\"firstName\": \"John\"}");
        var document = createDocument(definition, content).resultingDocument().orElseThrow();
        var origVersion = document.version();

        JsonDocumentContent updatedContent = new JsonDocumentContent("{\"firstName\": \"Paul\"}");
        document.applyModifiedContent(updatedContent, document.version()); // keep in mind: document is mutable (immutable objects pattern not applicable due to JPA)

        assertNotEquals(origVersion, document.version());
    }

    @Test
    public void shouldHaveEqualsVersionInCaseOfPropertyReorder() {
        var contentA = new JsonDocumentContent("{\"obj_1\":{\"obj_2\":{\"q_1\":\"a\",\"a_1\":\"a\",\"a\":[3, 2, 1]}}}");
        var contentB = new JsonDocumentContent("{\"obj_1\":{\"obj_2\":{\"a_1\":\"a\",\"q_1\":\"a\",\"a\":[3, 2, 1]}}}");

        var documentA = createDocument(definition, contentA).resultingDocument().get();
        var documentB = createDocument(definition, contentB).resultingDocument().get();

        assertEquals(documentA.version(), documentB.version());
    }

    @Test
    public void shouldNotHaveAnEqualVersionInCaseOfArrayReorder() {
        var contentA = new JsonDocumentContent("{\"obj_1\":{\"obj_2\":{\"q_1\":\"a\",\"a_1\":\"a\",\"a\":[1, 2, 3]}}}");
        var contentB = new JsonDocumentContent("{\"obj_1\":{\"obj_2\":{\"q_1\":\"a\",\"a_1\":\"a\",\"a\":[3, 2, 1]}}}");

        var documentA = createDocument(definition, contentA).resultingDocument().get();
        var documentB = createDocument(definition, contentB).resultingDocument().get();

        assertNotEquals(documentA.version(), documentB.version());
    }
}