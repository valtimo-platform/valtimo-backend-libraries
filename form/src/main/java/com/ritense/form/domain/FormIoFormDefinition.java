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

import static com.ritense.valtimo.contract.utils.AssertionConcern.assertArgumentLength;
import static com.ritense.valtimo.contract.utils.AssertionConcern.assertArgumentNotNull;
import static com.ritense.valtimo.contract.utils.AssertionConcern.assertStateTrue;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.ritense.form.autoconfigure.FormAutoConfiguration;
import com.ritense.form.domain.event.FormRegisteredEvent;
import com.ritense.form.domain.exception.FormDefinitionParsingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import kotlin.Pair;
import org.hibernate.annotations.Type;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.AbstractAggregateRoot;
import org.springframework.data.domain.Persistable;
import org.springframework.web.util.HtmlUtils;

@Entity
@Table(name = "form_io_form_definition")
public class FormIoFormDefinition extends AbstractAggregateRoot<FormIoFormDefinition>
        implements FormDefinition, Persistable<UUID> {

    private static final Logger logger = LoggerFactory.getLogger(FormIoFormDefinition.class);
    public static final String JSON_POINTER_DELIMITER = "/";

    @Deprecated(since = "11.0", forRemoval = true)
    public static final String JSON_PATH_DELIMITER = JSON_POINTER_DELIMITER;
    public static final String PROPERTY_KEY = "key";
    public static final String COMPONENTS_KEY = "components";
    public static final String DEFAULT_VALUE_FIELD = "defaultValue";
    public static final String PROCESS_VAR_PREFIX = "pv";
    public static final String EXTERNAL_FORM_FIELD_TYPE_SEPARATOR = ":";
    public static final String LEGACY_EXTERNAL_FORM_FIELD_TYPE_SEPARATOR = ".";
    public static final String DISABLED_KEY = "disabled";
    public static final String PREFILL_KEY = "prefill";

    @Id
    @Column(name = "id", updatable = false)
    private UUID id;

    @Column(name = "name", columnDefinition = "VARCHAR(255)")
    private String name;

    @Column(name = "form_definition", columnDefinition = "json")
    @Type(type = "com.vladmihalcea.hibernate.type.json.JsonType")
    private String formDefinition;

    @Column(name = "read_only", columnDefinition = "BIT")
    private Boolean readOnly = false;

    @Transient
    private JsonNode workingCopy = null;

    @Transient
    private boolean isNew = false;

    @Transient
    private boolean isWriting = false;

    public FormIoFormDefinition(
            final UUID id,
            final String name,
            final String formDefinition,
            final Boolean isReadOnly
    ) {
        assertArgumentNotNull(id, "id is required");
        assertArgumentNotNull(name, "name is required");
        assertArgumentLength(name, 255, "name max length is 255");
        assertArgumentNotNull(formDefinition, "formDefinition is required");
        this.id = id;
        this.name = name;
        setFormDefinition(formDefinition);
        setReadOnly(isReadOnly);
        this.isNew = true;
        registerEvent(new FormRegisteredEvent(id, name));
    }

    protected FormIoFormDefinition() {
    }

    public void setReadOnly(Boolean value) {
        if (value != null) {
            this.readOnly = value;
        }
    }

    @Override
    public void changeName(final String name) {
        assertArgumentNotNull(name, "name is required");
        assertStateTrue(readOnly.equals(false) || this.isWriting, "Cannot modify a readonly form");
        if (!this.name.equals(name)) {
            this.name = name;
        }
    }

    @Override
    public void changeDefinition(final String definition) {
        assertArgumentNotNull(definition, "definition is required");
        assertStateTrue(readOnly.equals(false) || this.isWriting, "Cannot modify a readonly form");
        if (!this.formDefinition.equals(definition)) {
            setFormDefinition(definition);
        }
    }

    public void isWriting() {
        this.isWriting = true;
    }

    public void doneWriting() {
        this.isWriting = false;
    }

    @Override
    public FormIoFormDefinition preFill(final JsonNode content) {
        FormIoFormDefinition.getInputFields(asJson()).stream()
                .filter(this::shouldPrefillField)
                .forEach(field -> fill(field, content));

        return this;
    }

    @Override
    public void preFill(@NotNull Map<String, ?> valueMap) {
        FormIoFormDefinition.getInputFields(asJson()).stream()
                .filter(this::shouldPrefillField)
                .forEach(fieldNode -> {
                    String fieldKey = getFieldKey(fieldNode);
                    Object value = valueMap.get(fieldKey);
                    if(value != null) {
                        JsonNode valueNode = Mapper.INSTANCE.get().valueToTree(value);
                        fieldNode.set(DEFAULT_VALUE_FIELD, htmlEscape(valueNode));
                    }
                });
    }

    public FormDefinition preFillWith(final String prefix, final Map<String, Object> variableMap) {
        final ObjectNode rootNode = JsonNodeFactory.instance.objectNode();
        final ObjectNode objectNode = JsonNodeFactory.instance.objectNode();
        variableMap.forEach((fieldName, value) -> objectNode.set(fieldName, Mapper.INSTANCE.get().valueToTree(value)));
        rootNode.set(prefix, objectNode);
        return preFill(rootNode);
    }

    public List<String> extractProcessVarNames() {
        return FormIoFormDefinition.getInputFields(asJson()).stream()
                .map(this::getProcessVar)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(ContentItem::getName)
                .toList();
    }

    public List<String> getInputKeysForPrefill() {
        return FormIoFormDefinition.getInputFields(asJson()).stream()
                .filter(this::shouldPrefillField)
                .map(this::getFieldKey)
                .toList();
    }

    public Map<String, List<ExternalContentItem>> buildExternalFormFieldsMap() {
        return buildExternalFormFieldsMapFiltered(null);
    }

    public Map<String, List<ExternalContentItem>> buildExternalFormFieldsMapForSubmission() {
        return buildExternalFormFieldsMapFiltered(this::shouldNotIgnoreField);
    }

    private Map<String, List<ExternalContentItem>> buildExternalFormFieldsMapFiltered(
            @Nullable Predicate<ObjectNode> predicate
    ) {
        var map = new HashMap<String, List<ExternalContentItem>>();
        final JsonNode formDefinitionNode = asJson();
        FormIoFormDefinition.getInputFields(formDefinitionNode)
                .stream()
                .filter(field -> predicate == null || predicate.test(field))
                .forEach(field -> getExternalFormField(field)
                        .ifPresent(externalContentItem ->
                                map.computeIfAbsent(
                                        externalContentItem.externalFormFieldType.toLowerCase(),
                                        externalFormFieldType -> new ArrayList<>()
                                ).add(externalContentItem)
                        )
                );
        return map;
    }

    public Map<String, Object> extractProcessVars(JsonNode formData) {
        final Map<String, Object> processVarFormData = new HashMap<>();
        final JsonNode formDefinitionNode = asJson();
        final List<ObjectNode> inputFields = FormIoFormDefinition.getInputFields(formDefinitionNode);
        inputFields
                .stream()
                .filter(this::shouldNotIgnoreField)
                .forEach(field -> getProcessVar(field)
                        .ifPresent(contentItem -> getValueBy(formData, contentItem.getJsonPointer())
                                .ifPresent(valueNode -> processVarFormData.put(contentItem.getName(),
                                        extractNodeValue(valueNode)
                                ))
                        ));
        return processVarFormData;
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public JsonNode getFormDefinition() {
        return asJson();
    }

    @Override
    public boolean isReadOnly() {
        return readOnly;
    }

    public JsonNode asJson() {
        if (this.workingCopy == null) {
            try {
                this.workingCopy = Mapper.INSTANCE.get().readTree(formDefinition);
            } catch (Exception e) {
                throw new FormDefinitionParsingException(e);
            }
        }
        return this.workingCopy;
    }

    public Optional<ContentItem> getDocumentContentVar(JsonNode field) {
        if (isDocumentContentVar(field)) {
            String key = getFieldKey(field);
            if (!key.isEmpty() && !key.startsWith(PROCESS_VAR_PREFIX)) {
                String jsonPointerExpr = key.replace(".", "/");
                jsonPointerExpr = JSON_POINTER_DELIMITER + jsonPointerExpr;
                String propertyName = jsonPointerExpr;
                return buildJsonPointer(jsonPointerExpr).flatMap(
                        jsonPointer -> Optional.of(new ContentItem(propertyName, jsonPointer)));
            } else {
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

    public List<ObjectNode> getDocumentMappedFields() {
        return getDocumentMappedFieldsFiltered(null);
    }

    public List<ObjectNode> getDocumentMappedFieldsForSubmission() {
        return getDocumentMappedFieldsFiltered(this::shouldNotIgnoreField);
    }

    private List<ObjectNode> getDocumentMappedFieldsFiltered(@Nullable Predicate<JsonNode> predicate) {
        final List<ObjectNode> inputFields = new LinkedList<>();
        List<ArrayNode> components = getComponents(this.asJson());
        components.forEach(componentsNode -> componentsNode.forEach(fieldNode -> {
            if (predicate == null || predicate.test(fieldNode) && (isDocumentContentVar(fieldNode))) {
                inputFields.add((ObjectNode) fieldNode);
            }
        }));
        return Collections.unmodifiableList(inputFields);
    }

    public static List<ObjectNode> getInputFields(JsonNode formDefinition) {
        final List<ObjectNode> inputFields = new LinkedList<>();
        List<ArrayNode> components = getComponents(formDefinition);
        components.forEach(componentsNode -> componentsNode.forEach(fieldNode -> {
            if ((isInputComponent(fieldNode) || isTextFieldComponent(fieldNode)) && !isButtonTypeComponent(fieldNode)) {
                inputFields.add((ObjectNode) fieldNode);
            }
        }));
        return Collections.unmodifiableList(inputFields);
    }

    private void setFormDefinition(String formDefinition) {
        try {
            this.workingCopy = Mapper.INSTANCE.get().readTree(formDefinition);
        } catch (Exception e) {
            throw new IllegalArgumentException("The formDefinition argument could not be parsed as JSON.", e);
        }
        this.formDefinition = formDefinition;
    }

    private boolean shouldNotIgnoreField(JsonNode fieldNode) {
        return !(
                FormAutoConfiguration.isIgnoreDisabledFields()
                        && fieldNode.has(DISABLED_KEY)
                        && fieldNode.get(DISABLED_KEY).asBoolean()
        );
    }

    private boolean shouldPrefillField(JsonNode fieldNode) {
        return !fieldNode.has(DEFAULT_VALUE_FIELD) && (
                !fieldNode.has(PREFILL_KEY) || fieldNode.get(PREFILL_KEY).asBoolean()
        );
    }

    private void fill(ObjectNode field, JsonNode content) {
        assertArgumentNotNull(field, "field is required");
        assertArgumentNotNull(content, "content is required");
        getContentItem(field)
                .flatMap(
                        contentItem -> getValueBy(content, contentItem.getJsonPointer())
                ).ifPresent(
                        valueNode -> field.set(DEFAULT_VALUE_FIELD, htmlEscape(valueNode))
                );
    }

    private Optional<? extends ContentItem> getContentItem(ObjectNode node) {
        if (isDocumentContentVar(node)) {
            return getDocumentContentVar(node);
        } else if (isProcessVar(node)) {
            return getProcessVar(node);
        } else if (isExternalFormField(node)) {
            return getExternalFormField(node);
        }
        return Optional.empty();
    }

    private JsonNode htmlEscape(JsonNode input) {
        if (input.isTextual()) {
            String escapedContent = HtmlUtils.htmlEscape(input.textValue(), StandardCharsets.UTF_8.name());
            return new TextNode(escapedContent);
        }
        return input;
    }

    private Optional<JsonPointer> buildJsonPointer(String jsonPointerExpression) {
        try {
            return Optional.of(JsonPointer.valueOf(jsonPointerExpression));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    private Optional<ContentItem> getProcessVar(JsonNode field) {
        if (isProcessVar(field)) {
            String jsonPointerExpr = getFieldKey(field).replace(".", "/");
            String processVarName = jsonPointerExpr.substring(
                    PROCESS_VAR_PREFIX.length() + 1); //example pv.varName -> gets varName
            jsonPointerExpr = JSON_POINTER_DELIMITER + jsonPointerExpr;
            return buildJsonPointer(jsonPointerExpr).flatMap(
                    jsonPointer -> Optional.of(new ContentItem(processVarName, jsonPointer)));
        }
        return Optional.empty();
    }

    private boolean isProcessVar(JsonNode field) {
        if (!field.has(PROPERTY_KEY)) {
            return false;
        }
        return getFieldKey(field).startsWith(PROCESS_VAR_PREFIX);
    }

    private boolean isDocumentContentVar(JsonNode field) {
        if (!field.has(PROPERTY_KEY)) {
            return false;
        }
        String key = getFieldKey(field).toUpperCase();
        if (getExternalFormFieldType(field).isPresent()) {
            return false;
        }
        return !key.isEmpty() && !key.startsWith(PROCESS_VAR_PREFIX.toUpperCase());
    }

    private Optional<ExternalContentItem> getExternalFormField(JsonNode field) {
        return getExternalFormFieldType(field).flatMap(externalFormFieldType -> {
            String fieldKey = getFieldKey(field);
            String propertyName = fieldKey.substring(
                    externalFormFieldType.length() + 1); // example pv:varName -> gets varName
            String jsonPointerExpr;
            String separator;

            if (fieldKey.contains(EXTERNAL_FORM_FIELD_TYPE_SEPARATOR)) {
                jsonPointerExpr = JSON_POINTER_DELIMITER + fieldKey
                        .replace("/", "~1")
                        .replace(".", "/");
                separator = EXTERNAL_FORM_FIELD_TYPE_SEPARATOR;
            } else {
                //support for legacy dot separator (pv.varName)
                jsonPointerExpr = JSON_POINTER_DELIMITER + fieldKey.replace(
                        LEGACY_EXTERNAL_FORM_FIELD_TYPE_SEPARATOR, "/");
                separator = LEGACY_EXTERNAL_FORM_FIELD_TYPE_SEPARATOR;
            }

            return buildJsonPointer(jsonPointerExpr)
                    .flatMap(jsonPointer -> Optional.of(
                                    new ExternalContentItem(
                                            propertyName,
                                            separator,
                                            jsonPointer,
                                            externalFormFieldType
                                    )
                            )
                    );
        });
    }

    private boolean isExternalFormField(JsonNode field) {
        return getExternalFormFieldType(field).isPresent();
    }

    private Optional<String> getExternalFormFieldType(JsonNode field) {
        if (!field.has(PROPERTY_KEY) && !getFieldKey(field).isEmpty()) {
            return Optional.empty();
        }
        final String key = getFieldKey(field).toUpperCase();
        // Note key can be -> "ExternalFormFieldTypeName:propertyName"
        if (!(key.contains(EXTERNAL_FORM_FIELD_TYPE_SEPARATOR)
                || key.contains(LEGACY_EXTERNAL_FORM_FIELD_TYPE_SEPARATOR))) {
            return Optional.empty();
        }

        // Get prefix up to first separator
        String prefix = key.contains(EXTERNAL_FORM_FIELD_TYPE_SEPARATOR)
                ? key.substring(0, key.indexOf(EXTERNAL_FORM_FIELD_TYPE_SEPARATOR)).toLowerCase() :
                key.substring(0, key.indexOf(LEGACY_EXTERNAL_FORM_FIELD_TYPE_SEPARATOR)).toLowerCase();

        // Check if key prefix is supported by a resolver.
        final var resolvers = FormSpringContextHelper.getFormFieldDataResolver();
        for (var entry : resolvers.entrySet()) {
            if (entry.getValue().supports(prefix)) {
                return Optional.of(prefix);
            }
        }
        return Optional.empty();
    }

    private String getFieldKey(JsonNode fieldNode) {
        return fieldNode.get(PROPERTY_KEY).asText();
    }

    private static Optional<JsonNode> getValueBy(JsonNode rootNode, JsonPointer jsonPointer) {
        final JsonNode jsonNode = rootNode.at(jsonPointer);
        if (jsonNode.isMissingNode() || jsonNode.isNull()) {
            return Optional.empty();
        }
        return Optional.of(jsonNode);
    }

    private static List<ArrayNode> getComponents(JsonNode formDefinition) {
        final var components = new ArrayList<ArrayNode>();
        if (formDefinition.isObject()
                && (formDefinition.has(COMPONENTS_KEY))
                && formDefinition.get(COMPONENTS_KEY).isArray()
        ) {
            components.add((ArrayNode) formDefinition.get(COMPONENTS_KEY));
        }
        if (formDefinition.isContainerNode()) {
            for (JsonNode arrayNode : formDefinition) {
                components.addAll(getComponents(arrayNode));
            }
        }
        return Collections.unmodifiableList(components);
    }

    private Object extractNodeValue(JsonNode node) {
        var nodeType = node.getNodeType();
        if (nodeType == JsonNodeType.STRING) {
            return node.textValue();
        } else if (nodeType == JsonNodeType.NUMBER) {
            return node.numberValue();
        } else if (nodeType == JsonNodeType.BOOLEAN) {
            return node.booleanValue();
        } else if (nodeType == JsonNodeType.ARRAY) {
            List<String> values = new ArrayList<>();
            node.forEach(childNode -> values.add(childNode.textValue()));
            return values;
        } else {
            logger.warn("Submitted form field value to be stored in process variables is of an unsupported type");
            return null;
        }
    }

    private static boolean isButtonTypeComponent(JsonNode jsonNode) {
        return jsonNode.has("type")
                && jsonNode.get("type").textValue().equalsIgnoreCase("button");
    }

    private static boolean isInputComponent(JsonNode jsonNode) {
        return jsonNode.has("input")
                && jsonNode.get("input").booleanValue()
                && jsonNode.has(PROPERTY_KEY);
    }

    private static boolean isTextFieldComponent(JsonNode jsonNode) {
        return jsonNode.has("type")
                && jsonNode.get("type").textValue().equalsIgnoreCase("textfield")
                && jsonNode.has(PROPERTY_KEY);
    }

    @Override
    @JsonIgnore
    public boolean isNew() {
        return isNew;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof FormIoFormDefinition that)) {
            return false;
        }

        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    public static class ContentItem {
        private final String name;
        private final JsonPointer jsonPointer;

        public ContentItem(String name, JsonPointer jsonPointer) {
            this.name = name;
            this.jsonPointer = jsonPointer;
        }

        public String getName() {
            return name;
        }

        public JsonPointer getJsonPointer() {
            return jsonPointer;
        }

    }

    public static class ExternalContentItem extends ContentItem {

        private final String externalFormFieldType;
        private final String separator;

        public ExternalContentItem(
                String name,
                JsonPointer jsonPointer,
                String externalFormFieldType
        ) {
            this(name, LEGACY_EXTERNAL_FORM_FIELD_TYPE_SEPARATOR, jsonPointer, externalFormFieldType);
        }

        public ExternalContentItem(
                String name,
                String separator,
                JsonPointer jsonPointer,
                String externalFormFieldType
        ) {
            super(name, jsonPointer);
            this.externalFormFieldType = externalFormFieldType;
            this.separator = separator;
        }

        public String getSeparator() {
            return separator;
        }
    }

}
