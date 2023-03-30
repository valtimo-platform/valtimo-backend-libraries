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

package com.ritense.processdocument.domain.delegate;

import com.ritense.document.domain.impl.JsonDocumentContent;
import com.ritense.document.domain.impl.JsonSchemaDocument;
import com.ritense.document.domain.impl.JsonSchemaDocumentDefinition;
import com.ritense.document.domain.impl.JsonSchemaDocumentId;
import com.ritense.document.service.DocumentSequenceGeneratorService;
import com.ritense.document.service.impl.JsonSchemaDocumentService;
import com.ritense.processdocument.BaseTest;
import com.ritense.processdocument.domain.impl.delegate.DocumentVariableDelegateImpl;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.community.mockito.delegate.DelegateExecutionFake;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DocumentVariableDelegateImplTest extends BaseTest {

    private static final String STREET_NAME = "street";
    private static final double HOUSE_NUMBER = 3.0;
    private static final boolean NO = false;

    private JsonSchemaDocumentDefinition definition;
    private DocumentSequenceGeneratorService documentSequenceGeneratorService;
    private JsonSchemaDocumentService documentService;
    private DocumentVariableDelegate documentVariableDelegate;
    private DelegateExecution delegateExecutionFake;

    @BeforeEach
    public void setUp() {
        definition = definition();
        documentSequenceGeneratorService = mock(DocumentSequenceGeneratorService.class);
        when(documentSequenceGeneratorService.next(any())).thenReturn(1L);

        documentService = mock(JsonSchemaDocumentService.class);
        documentVariableDelegate = new DocumentVariableDelegateImpl(documentService);
        delegateExecutionFake = new DelegateExecutionFake("id").withProcessBusinessKey("56f29315-c581-4c26-9b70-8bc818e8c86e");
    }

    @Test
    public void findStringValueByJsonPointer() {
        Optional<JsonSchemaDocument> jsonSchemaDocument = documentOptional();
        when(documentService.findBy(any(JsonSchemaDocumentId.class))).thenReturn(jsonSchemaDocument);

        String value = (String) documentVariableDelegate.findValueByJsonPointer("/applicant/street", delegateExecutionFake);
        assertEquals(STREET_NAME, value);
    }

    @Test
    public void findNumberValueByJsonPointer() {
        Optional<JsonSchemaDocument> jsonSchemaDocument = documentOptional();
        when(documentService.findBy(any(JsonSchemaDocumentId.class))).thenReturn(jsonSchemaDocument);

        double value = (double) documentVariableDelegate.findValueByJsonPointer("/applicant/number", delegateExecutionFake);
        assertEquals(HOUSE_NUMBER, value);
    }

    @Test
    public void findBooleanValueByJsonPointer() {
        Optional<JsonSchemaDocument> jsonSchemaDocument = documentOptional();
        when(documentService.findBy(any(JsonSchemaDocumentId.class))).thenReturn(jsonSchemaDocument);

        boolean value = (boolean) documentVariableDelegate.findValueByJsonPointer("/applicant/prettyHouse", delegateExecutionFake);
        assertEquals(NO, value);
    }

    @Test
    public void findCollectionByJsonPointer() {
        Optional<JsonSchemaDocument> jsonSchemaDocument = documentOptional();
        when(documentService.findBy(any(JsonSchemaDocumentId.class))).thenReturn(jsonSchemaDocument);

        List<?> value = (List<?>) documentVariableDelegate.findValueByJsonPointer("/cars", delegateExecutionFake);
        assertEquals(2, value.size());
        assertInstanceOf(Map.class, value.get(0));
        Map<String, Object> firstCar = (Map<String, Object>)value.get(0);
        assertEquals("volvo", firstCar.get("mark"));
        assertEquals(1991, firstCar.get("year"));
    }

    @Test
    public void findObjectByJsonPointer() {
        Optional<JsonSchemaDocument> jsonSchemaDocument = documentOptional();
        when(documentService.findBy(any(JsonSchemaDocumentId.class))).thenReturn(jsonSchemaDocument);

        Map<?, ?> value = (Map<?, ?>) documentVariableDelegate.findValueByJsonPointer("/applicant", delegateExecutionFake);
        assertEquals(Double.valueOf(HOUSE_NUMBER).intValue(), value.get("number"));
        assertEquals(STREET_NAME, value.get("street"));
        assertEquals(NO, value.get("prettyHouse"));
    }

    @Test
    public void incorrectPathShouldNotFindValue() {
        Optional<JsonSchemaDocument> jsonSchemaDocument = documentOptional();
        when(documentService.findBy(any(JsonSchemaDocumentId.class))).thenReturn(jsonSchemaDocument);

        assertThrows(NoSuchElementException.class, () ->
            documentVariableDelegate.findValueByJsonPointer("/street", delegateExecutionFake));
    }

    @Test
    public void incorrectPathShouldReturnDefaultValue() {
        Optional<JsonSchemaDocument> jsonSchemaDocument = documentOptional();
        when(documentService.findBy(any(JsonSchemaDocumentId.class))).thenReturn(jsonSchemaDocument);

        final String DEFAULT_VALUE = "DEFAULT_VALUE";
        Object value = documentVariableDelegate.findValueByJsonPointerOrDefault(
            "/street", delegateExecutionFake, DEFAULT_VALUE);

        assertEquals(DEFAULT_VALUE, value);

    }

    private Optional<JsonSchemaDocument> documentOptional() {
        return JsonSchemaDocument.create(
            definition,
            new JsonDocumentContent("{\"applicant\": " +
                "{\"street\": \"" + STREET_NAME + "\"," +
                    "\"number\": " + HOUSE_NUMBER + "," +
                    "\"prettyHouse\": " + NO + "}," +
                "\"cars\":[ \n" +
                    "{ \"mark\":\"volvo\", \"year\": 1991 }," +
                    "{ \"mark\":\"audi\", \"year\": 2016 }" +
                    "]}"
            ),
            "USERNAME",
            documentSequenceGeneratorService,
            null
        ).resultingDocument();
    }

}