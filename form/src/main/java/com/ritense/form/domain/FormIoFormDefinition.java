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

package com.ritense.form.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ritense.form.domain.event.FormRegisteredEvent;
import com.ritense.valtimo.contract.form.ExternalFormFieldType;
import org.hibernate.annotations.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.AbstractAggregateRoot;
import org.springframework.data.domain.Persistable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import static com.ritense.valtimo.contract.utils.AssertionConcern.assertArgumentLength;
import static com.ritense.valtimo.contract.utils.AssertionConcern.assertArgumentNotNull;
import static com.ritense.valtimo.contract.utils.AssertionConcern.assertStateTrue;

@Entity
@Table(name = "form_io_form_definition")
public class FormIoFormDefinition extends AbstractAggregateRoot<FormIoFormDefinition>
    implements FormDefinition, Persistable<UUID> {

    private static final Logger logger = LoggerFactory.getLogger(FormIoFormDefinition.class);
    public static final String PROPERTY_KEY = "key";
    public static final String COMPONENTS_KEY = "components";
    public static final String DEFAULT_VALUE_FIELD = "defaultValue";
    public static final String PROCESS_VAR_PREFIX = "pv";

    @Id
    @Column(name = "id", columnDefinition = "BINARY(16)", updatable = false)
    private UUID id;

    @Column(name = "name", columnDefinition = "VARCHAR(255)")
    private String name;

    @Column(name = "form_definition", columnDefinition = "json")
    @Type(type = "com.vladmihalcea.hibernate.type.json.JsonStringType")
    private String formDefinition;

    @Column(name = "read_only", columnDefinition = "BIT")
    private Boolean readOnly = false;

    @Transient
    private transient JsonNode workingCopy = null;

    @Transient
    private transient boolean isNew = false;

    @Transient
    private transient boolean isWriting = false;

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

    private FormIoFormDefinition() {
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
        final JsonNode formDefinition = asJson();
        final List<ObjectNode> inputFields = FormIoFormDefinition.getInputFields(formDefinition);
        inputFields.forEach(field -> fill(field, content));
        return this;
    }

    public FormDefinition preFillWith(final String prefix, final Map<String, Object> variableMap) {
        final ObjectNode rootNode = JsonNodeFactory.instance.objectNode();
        final ObjectNode objectNode = JsonNodeFactory.instance.objectNode();
        variableMap.forEach((fieldName, value) -> objectNode.set(fieldName, Mapper.INSTANCE.get().valueToTree(value)));
        rootNode.set(prefix, objectNode);
        return preFill(rootNode);
    }

    public List<String> extractProcessVarNames() {
        final List<String> processVarNames = new ArrayList<>();
        final JsonNode formDefinition = asJson();
        final List<ObjectNode> inputFields = FormIoFormDefinition.getInputFields(formDefinition);
        inputFields.forEach(field -> getProcessVar(field).ifPresent(contentItem -> processVarNames.add(contentItem.getName())));
        return Collections.unmodifiableList(processVarNames);
    }

    public Map<ExternalFormFieldType, List<ExternalContentItem>> buildExternalFormFieldsMap() {
        var map = new HashMap<ExternalFormFieldType, List<ExternalContentItem>>();
        final JsonNode formDefinition = asJson();
        final List<ObjectNode> inputFields = FormIoFormDefinition.getInputFields(formDefinition);

        inputFields.forEach(field -> getExternalFormField(field).ifPresent(externalContentItem ->
            map.computeIfAbsent(externalContentItem.externalFormFieldType, externalFormFieldType -> new ArrayList<>()).add(externalContentItem))
        );
        return map;
    }

    public Map<String, Object> extractProcessVars(JsonNode formData) {
        final Map<String, Object> processVarFormData = new HashMap<>();
        final JsonNode formDefinition = asJson();
        final List<ObjectNode> inputFields = FormIoFormDefinition.getInputFields(formDefinition);
        inputFields.forEach(field -> getProcessVar(field)
            .ifPresent(contentItem -> getValueBy(formData, contentItem.getJsonPointer())
                .ifPresent(valueNode -> processVarFormData.put(contentItem.getName(), extractNodeValue(valueNode)))
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
                throw new RuntimeException(e);
            }
        }
        return this.workingCopy;
    }

    public Optional<ContentItem> getDocumentContentVar(JsonNode field) {
        if (isDocumentContentVar(field)) {
            String key = field.get(PROPERTY_KEY).asText();
            if (!key.isEmpty() && !key.startsWith(PROCESS_VAR_PREFIX)) {
                String jsonPath = field.get(PROPERTY_KEY).asText().replace(".", "/");
                jsonPath = "/" + jsonPath;
                String name = jsonPath;
                return buildJsonPointer(jsonPath).flatMap(jsonPointer -> Optional.of(new ContentItem(name, jsonPointer)));
            } else {
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

    public List<ObjectNode> getDocumentMappedFields() {
        final List<ObjectNode> inputFields = new LinkedList<>();
        List<ArrayNode> components = getComponents(this.asJson());
        components.forEach(componentsNode -> componentsNode.forEach(fieldNode -> {
            if ((isDocumentContentVar(fieldNode))) {
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

    private void fill(ObjectNode field, JsonNode content) {
        assertArgumentNotNull(field, "field is required");
        assertArgumentNotNull(content, "content is required");
        if (isDocumentContentVar(field)) {
            getDocumentContentVar(field).flatMap(contentItem -> getValueBy(content, contentItem.getJsonPointer()))
                .ifPresent(valueNode -> field.set(DEFAULT_VALUE_FIELD, valueNode));
        } else if (isProcessVar(field)) {
            getProcessVar(field).flatMap(contentItem -> getValueBy(content, contentItem.getJsonPointer()))
                .ifPresent(valueNode -> field.set(DEFAULT_VALUE_FIELD, valueNode));
        } else if (isExternalFormField(field)) {
            getExternalFormField(field).flatMap(externalContentItem ->
                getValueBy(content, externalContentItem.getJsonPointer())).ifPresent(valueNode -> field.set(DEFAULT_VALUE_FIELD, valueNode)
            );
        }
    }

    private Optional<JsonPointer> buildJsonPointer(String jsonPath) {
        try {
            return Optional.of(JsonPointer.valueOf(jsonPath));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    private Optional<ContentItem> getProcessVar(JsonNode field) {
        if (isProcessVar(field)) {
            String jsonPath = field.get(PROPERTY_KEY).asText().replace(".", "/");
            String name = jsonPath.substring(PROCESS_VAR_PREFIX.length() + 1);//example pv.varName -> gets varName
            jsonPath = "/" + jsonPath;
            return buildJsonPointer(jsonPath).flatMap(jsonPointer -> Optional.of(new ContentItem(name, jsonPointer)));
        }
        return Optional.empty();
    }

    private boolean isProcessVar(JsonNode field) {
        if (!field.has(PROPERTY_KEY)) {
            return false;
        }
        return field.get(PROPERTY_KEY).asText().startsWith(PROCESS_VAR_PREFIX);
    }

    private boolean isDocumentContentVar(JsonNode field) {
        if (!field.has(PROPERTY_KEY)) {
            return false;
        }
        String key = field.get(PROPERTY_KEY).asText().toUpperCase();
        for (ExternalFormFieldType externalFormFieldType : ExternalFormFieldType.values()) {
            if (key.startsWith(externalFormFieldType.name().toUpperCase())) {
                return false;
            }
        }

        return !key.isEmpty() && !key.startsWith(PROCESS_VAR_PREFIX.toUpperCase());
    }

    private Optional<ExternalContentItem> getExternalFormField(JsonNode field) {
        return getExternalFormFieldType(field).flatMap(externalFormFieldType -> {
            String jsonPath = field.get(PROPERTY_KEY).asText().replace(".", "/");
            String name = jsonPath.substring(externalFormFieldType.name().length() + 1);//example pv.varName -> gets varName
            jsonPath = "/" + jsonPath;
            return buildJsonPointer(jsonPath).flatMap(jsonPointer -> Optional.of(new ExternalContentItem(name, jsonPointer, externalFormFieldType)));
        });
    }

    private boolean isExternalFormField(JsonNode field) {
        if (!field.has(PROPERTY_KEY) && !field.get(PROPERTY_KEY).asText().isEmpty()) {
            return false;
        }
        var propertyValue = field.get(PROPERTY_KEY).asText().toUpperCase();
        for (ExternalFormFieldType externalFormFieldType : ExternalFormFieldType.values()) {
            if (propertyValue.startsWith(externalFormFieldType.name().toUpperCase())) {
                return true;
            }
        }
        return false;
    }

    private Optional<ExternalFormFieldType> getExternalFormFieldType(JsonNode field) {
        var propertyValue = field.get(PROPERTY_KEY).asText().toUpperCase();
        for (ExternalFormFieldType externalFormFieldType : ExternalFormFieldType.values()) {
            if (propertyValue.startsWith(externalFormFieldType.name().toUpperCase())) {
                return Optional.of(externalFormFieldType);
            }
        }
        return Optional.empty();
    }

    private static Optional<JsonNode> getValueBy(JsonNode rootNode, JsonPointer jsonPointer) {
        final JsonNode jsonNode = rootNode.at(jsonPointer);
        if (jsonNode.isMissingNode() || jsonNode.isNull()) {
            return Optional.empty();
        }
        return Optional.of(jsonNode);
    }

    private static List<ArrayNode> getComponents(JsonNode formDefinition) {
        final List<ArrayNode> components = new LinkedList<>();
        addComponents(components, formDefinition);
        return Collections.unmodifiableList(components);
    }

    private Object extractNodeValue(JsonNode node) {
        if (node.isValueNode()) {
            return node.textValue();
        } else if (node.isArray()) {
            List<String> values = new ArrayList<>();
            node.forEach(childNode -> values.add(childNode.textValue()));
            return values;
        } else {
            logger.warn("Submitted form field value to be stored in process variables is of an unsupported type");
            return null;
        }
    }

    private static void addComponents(List<ArrayNode> components, JsonNode formDefinition) {
        if (formDefinition.isObject()
            && (formDefinition.has(COMPONENTS_KEY))
            && formDefinition.get(COMPONENTS_KEY).isArray()
        ) {
            components.add((ArrayNode) formDefinition.get(COMPONENTS_KEY));
        }
        if (formDefinition.isContainerNode()) {
            for (JsonNode arrayNode : formDefinition) {
                addComponents(components, arrayNode);
            }
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
        if (!(o instanceof FormIoFormDefinition)) {
            return false;
        }

        FormIoFormDefinition that = (FormIoFormDefinition) o;

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

        private final ExternalFormFieldType externalFormFieldType;

        public ExternalContentItem(String name, JsonPointer jsonPointer, ExternalFormFieldType externalFormFieldType) {
            super(name, jsonPointer);
            this.externalFormFieldType = externalFormFieldType;
        }

    }

}