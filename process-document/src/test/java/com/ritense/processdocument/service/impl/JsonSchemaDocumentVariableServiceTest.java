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

package com.ritense.processdocument.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.ritense.document.domain.impl.JsonDocumentContent;
import com.ritense.document.domain.impl.JsonSchemaDocument;
import com.ritense.document.domain.impl.JsonSchemaDocumentDefinition;
import com.ritense.document.domain.impl.JsonSchemaDocumentId;
import com.ritense.document.service.DocumentSequenceGeneratorService;
import com.ritense.document.service.impl.JsonSchemaDocumentService;
import com.ritense.document.service.impl.JsonSchemaDocumentVariableService;
import com.ritense.processdocument.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.NoSuchElementException;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JsonSchemaDocumentVariableServiceTest extends BaseTest {

    private static final String NAAM_VAN_DE_STRAAT = "straat";
    private static final Integer HOUSE_NUMBER = 3;
    private static final boolean YES = true;

    private JsonSchemaDocumentDefinition definition;
    private DocumentSequenceGeneratorService documentSequenceGeneratorService;
    private JsonSchemaDocumentService documentService;
    private JsonSchemaDocumentVariableService documentVariableService;

    @BeforeEach
    public void setUp() {
        definition = definition();
        documentSequenceGeneratorService = mock(DocumentSequenceGeneratorService.class);
        when(documentSequenceGeneratorService.next(any())).thenReturn(1L);

        documentService = mock(JsonSchemaDocumentService.class);
        documentVariableService = new JsonSchemaDocumentVariableService();
    }

    @Test
    public void getTextOrReturnEmptyStringShouldReturnText() {
        Optional<JsonSchemaDocument> jsonSchemaDocument = documentOptional();
        when(documentService.findBy(any(JsonSchemaDocumentId.class))).thenReturn(jsonSchemaDocument);

        String value = documentVariableService.getTextOrReturnEmptyString(jsonSchemaDocument.get(), "/applicant/street");
        assertEquals(NAAM_VAN_DE_STRAAT, value);
    }

    @Test
    public void getTextOrReturnEmptyStringShouldReturnEmptyString() {
        Optional<JsonSchemaDocument> jsonSchemaDocument = documentOptional();
        when(documentService.findBy(any(JsonSchemaDocumentId.class))).thenReturn(jsonSchemaDocument);

        String value = documentVariableService.getTextOrReturnEmptyString(jsonSchemaDocument.get(), "/applicant/non-existing");
        assertEquals("", value);
    }

    @Test
    public void getTextOrThrowShouldReturnText() {
        Optional<JsonSchemaDocument> jsonSchemaDocument = documentOptional();
        when(documentService.findBy(any(JsonSchemaDocumentId.class))).thenReturn(jsonSchemaDocument);

        String value = documentVariableService.getTextOrThrow(jsonSchemaDocument.get(), "/applicant/street");
        assertEquals(NAAM_VAN_DE_STRAAT, value);
    }

    @Test
    public void getTextOrThrowShouldThrow() {
        Optional<JsonSchemaDocument> jsonSchemaDocument = documentOptional();
        when(documentService.findBy(any(JsonSchemaDocumentId.class))).thenReturn(jsonSchemaDocument);

        assertThrows(NoSuchElementException.class, () -> {
            documentVariableService.getTextOrThrow(jsonSchemaDocument.get(), "/applicant/non-existing");
        });
    }

    @Test
    public void getNodeOrThrowShouldReturnNode() {
        Optional<JsonSchemaDocument> jsonSchemaDocument = documentOptional();
        when(documentService.findBy(any(JsonSchemaDocumentId.class))).thenReturn(jsonSchemaDocument);

        JsonNode value = documentVariableService.getNodeOrThrow(jsonSchemaDocument.get(), "/applicant/street");
        assertEquals(new TextNode(NAAM_VAN_DE_STRAAT), value);
    }

    @Test
    public void getNodeOrThrowShouldThrow() {
        Optional<JsonSchemaDocument> jsonSchemaDocument = documentOptional();
        when(documentService.findBy(any(JsonSchemaDocumentId.class))).thenReturn(jsonSchemaDocument);

        assertThrows(NoSuchElementException.class, () -> {
            documentVariableService.getNodeOrThrow(jsonSchemaDocument.get(), "/applicant/non-existing");
        });
    }

    @Test
    public void getIntegerOrReturnZeroShouldReturnInteger() {
        Optional<JsonSchemaDocument> jsonSchemaDocument = documentOptional();
        when(documentService.findBy(any(JsonSchemaDocumentId.class))).thenReturn(jsonSchemaDocument);

        Integer value = documentVariableService.getIntegerOrReturnZero(jsonSchemaDocument.get(), "/applicant/number");
        assertEquals(HOUSE_NUMBER, value);
    }

    @Test
    public void getIntegerOrReturnZeroShouldReturnZero() {
        Optional<JsonSchemaDocument> jsonSchemaDocument = documentOptional();
        when(documentService.findBy(any(JsonSchemaDocumentId.class))).thenReturn(jsonSchemaDocument);

        Integer value = documentVariableService.getIntegerOrReturnZero(jsonSchemaDocument.get(), "/applicant/non-existing");
        assertEquals(Integer.valueOf(0) ,value);
    }

    @Test
    public void getBooleanOrReturnFalseShouldReturnBoolean() {
        Optional<JsonSchemaDocument> jsonSchemaDocument = documentOptional();
        when(documentService.findBy(any(JsonSchemaDocumentId.class))).thenReturn(jsonSchemaDocument);

        Boolean value = documentVariableService.getBooleanOrReturnFalse(jsonSchemaDocument.get(), "/applicant/prettyHouse");
        assertEquals(Boolean.valueOf(YES), value);
    }

    @Test
    public void getBooleanOrReturnFalseShouldReturnFalse() {
        Optional<JsonSchemaDocument> jsonSchemaDocument = documentOptional();
        when(documentService.findBy(any(JsonSchemaDocumentId.class))).thenReturn(jsonSchemaDocument);

        Boolean value = documentVariableService.getBooleanOrReturnFalse(jsonSchemaDocument.get(), "/applicant/non-existing");
        assertEquals(Boolean.FALSE, value);
    }

    private Optional<JsonSchemaDocument> documentOptional() {
        return JsonSchemaDocument.create(
            definition,
            new JsonDocumentContent("{\"applicant\": {\"street\": \"" + NAAM_VAN_DE_STRAAT + "\"," +
                "\"number\": " + HOUSE_NUMBER + "," +
                "\"prettyHouse\": " + YES + "} }"
            ),
            "USERNAME",
            documentSequenceGeneratorService,
            null
        ).resultingDocument();
    }

}