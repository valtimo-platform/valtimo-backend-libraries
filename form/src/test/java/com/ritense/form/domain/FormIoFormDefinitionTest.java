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

package com.ritense.form.domain;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ritense.form.BaseTest;
import com.ritense.form.autoconfigure.FormAutoConfiguration;
import com.ritense.valtimo.contract.form.DataResolvingContext;
import com.ritense.valtimo.contract.form.FormFieldDataResolver;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FormIoFormDefinitionTest extends BaseTest {

    @BeforeEach
    void setUp() {
        mockSpringContextHelper();
    }

    @Test
    public void getProcessVarsNames() throws IOException {
        final var formDefinition = formDefinitionOf("process-variables-form-example");
        final List<String> processVarsNames = formDefinition.extractProcessVarNames();

        assertThat(processVarsNames).hasSize(3);
        assertThat(processVarsNames).contains("firstName");
        assertThat(processVarsNames).contains("age");
        assertThat(processVarsNames).contains("isRetired");
    }

    @Test
    public void shouldPreFill() throws IOException {
        final var formDefinition = formDefinitionOf("process-variables-form-example");

        var content = content(Map.of("pv", Map.of(
            "firstName", "John", "age",90, "isRetired",true
        )));

        final var formDefinitionPreFilled = formDefinition.preFill(content);

        var preFilledFormComponents = formDefinitionPreFilled.getFormDefinition().get("components");
        assertThat(preFilledFormComponents.get(0).get("defaultValue").asText()).isEqualTo("John");
        assertThat(preFilledFormComponents.get(1).get("defaultValue").asInt()).isEqualTo(90);
        assertThat(preFilledFormComponents.get(2).get("defaultValue").asBoolean()).isTrue();
    }

    @Test
    public void shouldPreFillForMultiLevelKey() throws IOException {
        // Given
        Map<String, FormFieldDataResolver> resolvers = new HashMap<>();
        resolvers.put("some-bean", new FormFieldDataResolverImpl("externalPrefix"));
        ApplicationContext context = mock(ApplicationContext.class);
        new FormSpringContextHelper().setApplicationContext(context);
        when(context.getBeansOfType(FormFieldDataResolver.class)).thenReturn(resolvers);
        final var formDefinition = formDefinitionOf("process-variables-multi-level-key-form-example");

        // When
        final var jsonContent = content(Map.of("externalPrefix", Map.of("person", Map.of("firstName", "John"))));
        final var formDefinitionPreFilled = formDefinition.preFill(jsonContent);

        // Then
        assertThat(
            formDefinitionPreFilled.getFormDefinition().get("components").get(0).get("defaultValue").asText()
        ).isEqualTo("John");
    }

    @Test
    public void shouldPreFillForMultiLevelKeyWithPrefix() throws IOException {
        // Given
        Map<String, FormFieldDataResolver> resolvers = new HashMap<>();
        resolvers.put("some-bean", new FormFieldDataResolverImpl("externalPrefix"));
        ApplicationContext context = mock(ApplicationContext.class);
        new FormSpringContextHelper().setApplicationContext(context);
        when(context.getBeansOfType(FormFieldDataResolver.class)).thenReturn(resolvers);
        final var formDefinition = formDefinitionOf("process-variables-multi-level-key-form-example");

        // When
        // Map<String, Object> content = Map.of("sensor", Map.of("person", Map.of("firstName", "John")));
        Map<String, Object> content = Map.of("person", Map.of("firstName", "John"));
        final var formDefinitionPreFilled = formDefinition.preFillWith("externalPrefix", content);

        // Then
        assertThat(
            formDefinitionPreFilled.getFormDefinition().get("components").get(0).get("defaultValue").asText()
        ).isEqualTo("John");
    }

    @Test
    public void shouldEscapeHtmlAtPreFill() throws IOException {
        final var formDefinition = formDefinitionOf("process-variables-form-example");

        var content = content(Map.of("pv", Map.of("firstName", "</b>")));

        final var formDefinitionPreFilled = formDefinition.preFill(content);

        assertThat(formDefinitionPreFilled.getFormDefinition().get("components").get(0).get("defaultValue").asText()).isEqualTo("&lt;/b&gt;");
    }

    @Test
    public void shouldNotExtractProcessVarsWhenSubmissionIsEmpty() throws IOException {
        final var formDefinition = formDefinitionOf("process-variables-form-example");

        final ObjectNode formData = JsonNodeFactory.instance.objectNode();
        final ObjectNode processVarsData = JsonNodeFactory.instance.objectNode();
        processVarsData.put("firstName", "John");
        formData.set("container", processVarsData);

        final Map<String, Object> stringObjectMap = formDefinition.extractProcessVars(formData);

        assertThat(stringObjectMap).isEmpty();
    }

    @Test
    public void shouldExtractProcessVarsWithoutDisabledFieldsWhenFlagIsSet() throws IOException {
        new FormAutoConfiguration(true);

        final var formDefinition = formDefinitionOf("process-variables-form-example");

        final ObjectNode formData = JsonNodeFactory.instance.objectNode();
        final ObjectNode processVarsData = JsonNodeFactory.instance.objectNode();
        processVarsData.put("firstName", "John");
        processVarsData.put("age", 90);
        processVarsData.put("isRetired", true);
        formData.set("pv", processVarsData);

        final Map<String, Object> stringObjectMap = formDefinition.extractProcessVars(formData);

        assertThat(stringObjectMap).contains(entry("firstName", "John"));
        assertThat(stringObjectMap).contains(entry("age", 90));
        assertThat(stringObjectMap).doesNotContain(entry("isRetired", true));
    }

    @Test
    public void shouldExtractProcessVarsWithDisabledFieldsWhenFlagIsNotSet() throws IOException {
        new FormAutoConfiguration(false);

        final var formDefinition = formDefinitionOf("process-variables-form-example");

        final ObjectNode formData = JsonNodeFactory.instance.objectNode();
        final ObjectNode processVarsData = JsonNodeFactory.instance.objectNode();
        processVarsData.put("firstName", "John");
        processVarsData.put("age", 90);
        processVarsData.put("isRetired", true);
        formData.set("pv", processVarsData);

        final Map<String, Object> stringObjectMap = formDefinition.extractProcessVars(formData);

        assertThat(stringObjectMap).contains(entry("firstName", "John"));
        assertThat(stringObjectMap).contains(entry("age", 90));
        assertThat(stringObjectMap).contains(entry("isRetired", true));
    }

    @Test
    public void shouldExtractProcessVarsArrayValue() throws IOException {
        final var formDefinition = formDefinitionOf("process-variables-form-example");

        final ObjectNode formData = JsonNodeFactory.instance.objectNode();
        final ObjectNode processVarsData = JsonNodeFactory.instance.objectNode();
        final ArrayNode arrayValue = JsonNodeFactory.instance.arrayNode();
        arrayValue.add("value1");
        arrayValue.add("value2");
        processVarsData.set("firstName", arrayValue);
        formData.set("pv", processVarsData);

        final Map<String, Object> processVars = formDefinition.extractProcessVars(formData);

        List<String> values = (List<String>) processVars.get("firstName");
        assertThat(values).containsOnly("value1", "value2");
    }

    @Test
    public void shouldGetInputFieldsOnly() throws IOException {
        final var formDefinition = formDefinitionOf("form-example-nested-components");

        JsonNode definition = formDefinition.getFormDefinition();
        List<ObjectNode> components = FormIoFormDefinition.getInputFields(definition);

        assertThat(components).hasSize(7);
    }

    @Test
    public void shouldGetDocumentMappedFields() throws IOException {
        final var formDefinition = formDefinitionOf("form-example-nested-components");
        var result = formDefinition.getDocumentMappedFields();
        assertThat(result).hasSize(13);
    }

    @Test
    public void shouldNotRemoveDisabledFieldFromDocumentMappedFieldsWhenFlagIsEnabled() throws IOException {
        new FormAutoConfiguration(true);
        final var formDefinition = formDefinitionOf("form-example-nested-components");
        var result = formDefinition.getDocumentMappedFields();
        var disabledField = result.stream().filter(node -> node.get("key").asText().equals("disabledField")).findFirst();
        assertTrue(disabledField.isPresent());
        assertTrue(disabledField.get().get("disabled").asBoolean());
    }

    @Test
    public void shouldRemoveDisabledFieldFromDocumentMappedFieldsForSubmissionWhenFlagIsEnabled() throws IOException {
        new FormAutoConfiguration(true);
        final var formDefinition = formDefinitionOf("form-example-nested-components");
        var result = formDefinition.getDocumentMappedFieldsForSubmission();
        for (ObjectNode node : result) {
            assertThat(node.toString().contains("\"disabled\":true")).isFalse();
        }
    }

    @Test
    public void shouldNotRemoveDisabledFieldFromDocumentMappedFieldsWhenFlagIsDisabled() throws IOException {
        new FormAutoConfiguration(false);
        final var formDefinition = formDefinitionOf("form-example-nested-components");
        var result = formDefinition.getDocumentMappedFields();
        var disabledField = result.stream().filter(node -> node.get("key").asText().equals("disabledField")).findFirst();
        assertTrue(disabledField.isPresent());
        assertTrue(disabledField.get().get("disabled").asBoolean());
    }

    @Test
    public void shouldNotRemoveDisabledFieldFromDocumentMappedFieldsForSubmissionWhenFlagIsDisabled() throws IOException {
        new FormAutoConfiguration(false);
        final var formDefinition = formDefinitionOf("form-example-nested-components");
        var result = formDefinition.getDocumentMappedFieldsForSubmission();
        var disabledField = result.stream().filter(node -> node.get("key").asText().equals("disabledField")).findFirst();
        assertTrue(disabledField.isPresent());
        assertTrue(disabledField.get().get("disabled").asBoolean());
    }

    @Test
    public void shouldFindExternalFieldsWithFlagDisabled() throws IOException {
        new FormAutoConfiguration(false);

        final var formDefinition = formDefinitionOf("form-example-external-field");

        Map<String, FormFieldDataResolver> resolvers = new HashMap<>();
        resolvers.put("some-bean", new FormFieldDataResolverImpl("test"));
        resolvers.put("some-other-bean", new FormFieldDataResolverImpl("other"));
        ApplicationContext context = mock(ApplicationContext.class);
        new FormSpringContextHelper().setApplicationContext(context);
        when(context.getBeansOfType(FormFieldDataResolver.class)).thenReturn(resolvers);

        Map<String, List<FormIoFormDefinition.ExternalContentItem>> externalContent
            = formDefinition.buildExternalFormFieldsMap();

        assertExampleExternalField(externalContent, true);
    }

    @Test
    public void shouldFindExternalFieldsWithFlagEnabled() throws IOException {
        new FormAutoConfiguration(true);

        final var formDefinition = formDefinitionOf("form-example-external-field");

        Map<String, FormFieldDataResolver> resolvers = new HashMap<>();
        resolvers.put("some-bean", new FormFieldDataResolverImpl("test"));
        resolvers.put("some-other-bean", new FormFieldDataResolverImpl("other"));
        ApplicationContext context = mock(ApplicationContext.class);
        new FormSpringContextHelper().setApplicationContext(context);
        when(context.getBeansOfType(FormFieldDataResolver.class)).thenReturn(resolvers);

        Map<String, List<FormIoFormDefinition.ExternalContentItem>> externalContent
            = formDefinition.buildExternalFormFieldsMap();

        assertExampleExternalField(externalContent, true);
    }

    @Test
    public void shouldFindExternalFieldsForSubmissionWithFlagEnabled() throws IOException {
        new FormAutoConfiguration(true);

        final var formDefinition = formDefinitionOf("form-example-external-field");

        Map<String, FormFieldDataResolver> resolvers = new HashMap<>();
        resolvers.put("some-bean", new FormFieldDataResolverImpl("test"));
        resolvers.put("some-other-bean", new FormFieldDataResolverImpl("other"));
        ApplicationContext context = mock(ApplicationContext.class);
        new FormSpringContextHelper().setApplicationContext(context);
        when(context.getBeansOfType(FormFieldDataResolver.class)).thenReturn(resolvers);

        Map<String, List<FormIoFormDefinition.ExternalContentItem>> externalContent
            = formDefinition.buildExternalFormFieldsMapForSubmission();

        assertExampleExternalField(externalContent, false);
    }

    @Test
    public void shouldFindExternalFieldsForSubmissionWithFlagDisabled() throws IOException {
        new FormAutoConfiguration(false);

        final var formDefinition = formDefinitionOf("form-example-external-field");

        Map<String, FormFieldDataResolver> resolvers = new HashMap<>();
        resolvers.put("some-bean", new FormFieldDataResolverImpl("test"));
        resolvers.put("some-other-bean", new FormFieldDataResolverImpl("other"));
        ApplicationContext context = mock(ApplicationContext.class);
        new FormSpringContextHelper().setApplicationContext(context);
        when(context.getBeansOfType(FormFieldDataResolver.class)).thenReturn(resolvers);

        Map<String, List<FormIoFormDefinition.ExternalContentItem>> externalContent
            = formDefinition.buildExternalFormFieldsMapForSubmission();

        assertExampleExternalField(externalContent, true);
    }

    @Test
    void shouldAddDataTestIdAttributeToFormDefinition() throws IOException {
        final var formDefinition = formDefinitionOf("form-with-data-testid").asJson();
        final var firstNameNode = formDefinition.get("components").get(0);

        assertEquals("form-example-person.firstName", firstNameNode.get("attributes").get("data-testid").asText());
    }

    @Test
    void shouldNotOverrideDataTestIdAttributeIfAlreadyExist() throws IOException {
        final var formDefinition = formDefinitionOf("form-with-data-testid").asJson();
        final var lastNameNode = formDefinition.get("components").get(1);

        assertEquals("custom-testid-1234567890", lastNameNode.get("attributes").get("data-testid").asText());
    }

    private void assertExampleExternalField(
        Map<String, List<FormIoFormDefinition.ExternalContentItem>> externalContent,
        boolean shouldIncludeDisabledFields
    ) {
        assertTrue(externalContent.containsKey("other"));
        List<FormIoFormDefinition.ExternalContentItem> otherItems = externalContent.get("other");
        assertEquals(1, otherItems.size());
        assertEquals(":", otherItems.get(0).getSeparator());
        assertEquals("field", otherItems.get(0).getName());
        assertEquals(JsonPointer.valueOf("/other:field"), otherItems.get(0).getJsonPointer());

        List<FormIoFormDefinition.ExternalContentItem> testItems = externalContent.get("test");
        assertEquals(shouldIncludeDisabledFields ? 3 : 2, testItems.size());

        assertEquals(":", testItems.get(0).getSeparator());
        assertEquals("lastName", testItems.get(0).getName());
        assertEquals(JsonPointer.valueOf("/test:lastName"), testItems.get(0).getJsonPointer());

        assertEquals(".", testItems.get(1).getSeparator());
        assertEquals("firstName", testItems.get(1).getName());
        assertEquals(JsonPointer.valueOf("/test/firstName"), testItems.get(1).getJsonPointer());

        if (shouldIncludeDisabledFields) {
            assertEquals(":", testItems.get(2).getSeparator());
            assertEquals("field", testItems.get(2).getName());
            assertEquals(JsonPointer.valueOf("/test:field"), testItems.get(2).getJsonPointer());
        }
    }

    public JsonNode content(Object content) throws JsonProcessingException {
        String jsonString = Mapper.INSTANCE.get().writeValueAsString(content);
        return Mapper.INSTANCE.get().readTree(jsonString);
    }

    private static class FormFieldDataResolverImpl implements FormFieldDataResolver {

        private String prefix;

        public FormFieldDataResolverImpl(String prefix) {
            this.prefix = prefix;
        }

        @Override
        public boolean supports(String externalFormFieldType) {
            return externalFormFieldType.equals(prefix);
        }

        @Override
        public Map<String, Object> get(
            DataResolvingContext dataResolvingContext,
            String... varNames
        ) {
            Map<String, Object> results = new HashMap<>();
            Arrays.stream(varNames).forEach((name) -> results.put(name, "test"));
            return results;
        }
    }
}