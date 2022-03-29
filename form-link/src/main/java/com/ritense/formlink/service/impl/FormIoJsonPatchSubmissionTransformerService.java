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

package com.ritense.formlink.service.impl;

import static com.ritense.document.domain.patch.JsonPatchFilterFlag.allowRemovalOperations;
import static com.ritense.form.domain.FormIoFormDefinition.PROPERTY_KEY;
import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ritense.document.domain.patch.JsonPatchService;
import com.ritense.form.domain.FormIoFormDefinition;
import com.ritense.formlink.service.SubmissionTransformerService;
import com.ritense.valtimo.contract.json.patch.JsonPatch;
import com.ritense.valtimo.contract.json.patch.JsonPatchBuilder;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

public class FormIoJsonPatchSubmissionTransformerService implements SubmissionTransformerService<FormIoFormDefinition> {

    private static final String CUSTOM_PROPERTIES = "properties";
    private static final String CONTAINER_KEY = "container";
    private static final String ID_KEY = "_id";
    private static final String DEFAULT_VALUE_FIELD = "defaultValue";

    @Override
    public void prePreFillTransform(FormIoFormDefinition formDefinition, JsonNode placeholders, JsonNode source) {
        final JsonNode formDefinitionData = formDefinition.getFormDefinition();
        final List<ObjectNode> inputFields = FormIoFormDefinition.getInputFields(formDefinitionData);
        final ObjectNode dataToPreFill = JsonNodeFactory.instance.objectNode();

        inputFields.forEach(field -> {
            if (field.has(CUSTOM_PROPERTIES) && !field.get(CUSTOM_PROPERTIES).isEmpty()) {
                if (field.get(CUSTOM_PROPERTIES).has(CONTAINER_KEY)
                    && !field.get(CUSTOM_PROPERTIES).get(CONTAINER_KEY).isNull()
                ) {

                    final String container = field.get(CUSTOM_PROPERTIES).get(CONTAINER_KEY).asText();
                    final String propertyName = field.get(PROPERTY_KEY).textValue();

                    if (container.contains("/{indexOf")) {

                        final String indexValueJsonPointer = getIndexValueJsonPointer(container);
                        final String id = placeholders.at(indexValueJsonPointer).textValue();

                        final JsonPointer arrayPointer = JsonPointer.compile(StringUtils.substringBefore(container, "/{"));
                        final ArrayNode list = (ArrayNode) source.at(arrayPointer);//get sources array
                        final String calculatedArrayItemIndex = lookupIndexForIdValue(list, id);

                        final JsonPointer arrayItemForSourceJsonPointer = JsonPointer.compile(
                            arrayPointer + "/" + calculatedArrayItemIndex + "/" + propertyName
                        );

                        dataToPreFill.set(propertyName, source.at(arrayItemForSourceJsonPointer));
                        ObjectNode customPropertiesObject = (ObjectNode) field.get(CUSTOM_PROPERTIES);
                        customPropertiesObject.remove(CONTAINER_KEY);
                    }
                }
            }
        });
        formDefinition.preFill(dataToPreFill);
    }

    /**
     *   FormDefinition can utilize value array operation by implementing the property section.
     *   note: In the Form IO builder this is called custom property on the API tab.
     *
     *     Adding a new array item - configuration:
     *     {
     *          "label": "Bread name",
     *          "key": "name", -{@literal >} Name of the property to ADD a value to, this should match object property name of an array item.
     *          "properties": {
     *              "container": "/favorites/-/" -{@literal >} indicating an new item should be added at the end of the array
     *          },
     *          "type": "textfield",
     *          "input": true
     *     }
     *
     *     Update existing array item - configuration:
     *     {
     *          "label": "Bread name",
     *          "key": "name",  -{@literal >} Name of the property to REPLACE its value, this should match object property name of an array item.
     *          "properties": {
     *              "container": "/favorites/{indexOf(/pv/breadId)}/" -{@literal >} indicating an existing items location to be modified
     *          },
     *          "type": "textfield",
     *          "input": true
     *     },
     *
     *    Source example:
     *    {
     *      "favorites" : [
     *          { "_id" : "1", "name": "White bread"},
     *          { "_id" : "2", "name": "Pita bread"}
     *      ]
     *    }
     *
     *    Submission payload:
     *    "data":
     *      { "name" : "Focaccia" }
     *    }
     *
     *    Placeholder:
     *    "pv":
     *      { "breadId" : "2" }
     *    }
     *
     *    Results:
     *
     *    Submission will be sanitized as so:
     *    "data": {} // removed name property because patch will hold its modification
     *
     *    New array item configuration - Patch result :
     *    [
     *      {
     *          "op" : "add",
     *          "path" : "/favorites/-/name",
     *          "value" : "Focaccia"
     *      }
     *    ]
     *
     *    Update existing array item/object configuration: - Patch result
     *    [
     *      {
     *          "op" : "replace",
     *          "path" : "/favorites/1/name",
     *          "value" : "Focaccia"
     *      }
     *    ]
     *
     *   Adding a new array item:
     *      Example: /favorites/-/name
     *      Notes:
     *      - Creates a patch ADD operation for appending (last) an item to an existing array.
     *
     *   Update existing array item/object:
     *     Example usage: /favorites/{indexOf(pv/key)}/name
     *     Notes:
     *     - This will create a REPLACE patch operation of a item to a specific index of a existing array.
     *     - pv.key = the key to use as matcher in the source document array item.
     *     - If index doesnt exist it will get the last index.
     *     - The name of the id to check is fixed '_id'.
     *
     * @param formDefinition The form definition
     * @param submission The data structure to process, will be sanitized to avoid issues.
     * @param placeholders The container to retrieve the indexOf(jsonPointerValue) input var. This value is used to match against _id.
     * @param source the Json to use for determining index value of an array.
     * @return JsonPatch a patch containing patch operations for array modifications.
     */
    @Override
    public JsonPatch preSubmissionTransform(FormIoFormDefinition formDefinition, JsonNode submission, JsonNode placeholders, JsonNode source) {
        final JsonPatchBuilder sourceJsonPatchBuilder = new JsonPatchBuilder();
        final JsonPatchBuilder submissionJsonPatchBuilder = new JsonPatchBuilder();

        final JsonNode formDefinitionData = formDefinition.getFormDefinition();
        final List<ObjectNode> inputFields = FormIoFormDefinition.getInputFields(formDefinitionData);

        inputFields.forEach(field -> {
            if (field.has(CUSTOM_PROPERTIES) && !field.get(CUSTOM_PROPERTIES).isEmpty()) {
                final String container = field.get(CUSTOM_PROPERTIES).get(CONTAINER_KEY).asText();
                final String propertyName = field.get(PROPERTY_KEY).textValue();
                final JsonNode propertyValue = submission.at("/" + propertyName);
                final JsonPointer submissionProperty = JsonPointer.valueOf("/" + propertyName);

                if (container.contains("/{indexOf")) {
                    final String indexValueJsonPointer = getIndexValueJsonPointer(container);
                    final String id = placeholders.at(indexValueJsonPointer).textValue();

                    final JsonPointer arrayPointer = JsonPointer.compile(StringUtils.substringBefore(container, "/{"));
                    final ArrayNode list = (ArrayNode) source.at(arrayPointer);//get sources array
                    final String calculatedArrayItemIndex = lookupIndexForIdValue(list, id);

                    final JsonPointer arrayItemForSourceJsonPointer = JsonPointer.compile(
                        arrayPointer + "/" + calculatedArrayItemIndex + "/" + propertyName
                    );
                    sourceJsonPatchBuilder.replace(arrayItemForSourceJsonPointer, propertyValue);

                    submissionJsonPatchBuilder.remove(submissionProperty);

                } else if (container.contains("/-/")) {
                    final JsonPointer arrayPointer = JsonPointer.compile(StringUtils.substringBefore(container, "/-"));
                    final ArrayNode list = (ArrayNode) source.at(arrayPointer);//get sources array
                    final String calculatedArrayItemIndex = String.valueOf(list.size());

                    final JsonPointer arrayItemForSourceJsonPointer = JsonPointer.compile(
                        arrayPointer + "/" + calculatedArrayItemIndex + "/" + propertyName
                    );

                    //ensure object exist in array
                    sourceJsonPatchBuilder.add(
                        JsonPointer.valueOf(arrayPointer + "/" + calculatedArrayItemIndex), JsonNodeFactory.instance.objectNode()
                    );
                    //Add actual item to its position
                    sourceJsonPatchBuilder.add(arrayItemForSourceJsonPointer, propertyValue);
                    submissionJsonPatchBuilder.remove(submissionProperty);
                }
            }
        });
        //Cleaning submission to avoid issues when running diff.
        JsonPatch submissionPatch = submissionJsonPatchBuilder.build();
        if (!submissionPatch.patches().isEmpty()) {
            JsonPatchService.apply(submissionPatch, submission, allowRemovalOperations());
        }
        return sourceJsonPatchBuilder.build();
    }

    private String getIndexValueJsonPointer(String container) {
        return StringUtils.substringBetween(container, "(", ")");
    }

    private String lookupIndexForIdValue(ArrayNode list, String id) {
        int i;
        for (i = 0; i < list.size(); i++) {
            ObjectNode item = (ObjectNode) list.get(i);
            if (item.get(ID_KEY).textValue().equalsIgnoreCase(id)) {
                break;
            }
        }
        return String.valueOf(i);
    }

}