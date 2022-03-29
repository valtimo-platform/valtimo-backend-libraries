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

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ritense.document.domain.patch.JsonPatchService;
import com.ritense.formlink.BaseTest;
import com.ritense.valtimo.contract.json.patch.JsonPatch;
import com.ritense.valtimo.contract.json.patch.operation.AddOperation;
import com.ritense.valtimo.contract.json.patch.operation.Operation;
import com.ritense.valtimo.contract.json.patch.operation.ReplaceOperation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import static org.assertj.core.api.Assertions.assertThat;

public class FormIoJsonPatchSubmissionTransformerServiceTest extends BaseTest {

    private FormIoJsonPatchSubmissionTransformerService formIoJsonPatchSubmissionTransformerService;

    @BeforeEach
    public void setUp() {
        formIoJsonPatchSubmissionTransformerService = new FormIoJsonPatchSubmissionTransformerService();
    }

    @Test
    public void prePreFillTransform() throws IOException {

        final var formDefinition = formDefinitionOf("existing-item-array-form-example");

        ObjectNode placeholders = placeholders();
        ObjectNode source = source();

        formIoJsonPatchSubmissionTransformerService.prePreFillTransform(
            formDefinition,
            placeholders,
            source
        );
        assertThat(formDefinition.asJson().at("/components/0/defaultValue").textValue()).isEqualTo("Pita bread");
    }

    @Test
    public void preProcessExistingArrayItem() throws IOException {

        final var formDefinition = formDefinitionOf("existing-item-array-form-example");

        ObjectNode submission = submission();
        ObjectNode placeholders = placeholders();
        ObjectNode source = source();

        JsonPatch jsonPatch = formIoJsonPatchSubmissionTransformerService.preSubmissionTransform(
            formDefinition,
            submission,
            placeholders,
            source
        );

        //Assert initial submission is cleaned up
        assertThat(submission.get("name")).isNullOrEmpty();
        assertThat(jsonPatch.patches().size()).isEqualTo(1);

        //Patch for source is created
        ReplaceOperation jsonPatchOperation = (ReplaceOperation) jsonPatch.patches().iterator().next();
        assertThat(jsonPatchOperation.getOperation()).isEqualTo(Operation.REPLACE.toString());
        assertThat(jsonPatchOperation.getPath()).isEqualTo("/favorites/1/name");
        assertThat(jsonPatchOperation.getValue().textValue()).isEqualTo("Focaccia");

        JsonPatchService.apply(jsonPatch, source);
        assertThat(source.at("/favorites/1/name").textValue()).isEqualTo("Focaccia");
    }

    @Test
    public void preProcessNewArrayItem() throws IOException {

        final var formDefinition = formDefinitionOf("new-item-array-form-example");

        ObjectNode submission = submission();
        ObjectNode placeholders = placeholders();
        ObjectNode source = source();

        JsonPatch jsonPatch = formIoJsonPatchSubmissionTransformerService.preSubmissionTransform(
            formDefinition,
            submission,
            placeholders,
            source
        );

        //Assert initial submission is cleaned up
        assertThat(submission.get("name")).isNullOrEmpty();
        assertThat(jsonPatch.patches().size()).isEqualTo(2);

        //Patch for source is created
        AddOperation newArrayOperation = (AddOperation) jsonPatch.patches().stream().skip(jsonPatch.patches().size() - 2).findFirst().orElseThrow();
        assertThat(newArrayOperation.getOperation()).isEqualTo(Operation.ADD.toString());
        assertThat(newArrayOperation.getPath()).isEqualTo("/favorites/2");
        assertThat(newArrayOperation.getValue().isObject()).isTrue();

        AddOperation jsonPatchOperation = (AddOperation) jsonPatch.patches().stream().skip(jsonPatch.patches().size() - 1).findFirst().orElseThrow();
        assertThat(jsonPatchOperation.getOperation()).isEqualTo(Operation.ADD.toString());
        assertThat(jsonPatchOperation.getPath()).isEqualTo("/favorites/2/name");
        assertThat(jsonPatchOperation.getValue().textValue()).isEqualTo("Focaccia");

        JsonPatchService.apply(jsonPatch, source);
        assertThat(source.at("/favorites/2/name").textValue()).isEqualTo("Focaccia");
    }

    @Test
    public void preProcessNewArrayItemCombinedPatch() throws IOException {

        final var formDefinition = formDefinitionOf("new-item-combined-array-form-example");

        ObjectNode submission = submissionNewArray();
        ObjectNode placeholders = placeholders();
        ObjectNode source = source();

        JsonPatch jsonPatch = formIoJsonPatchSubmissionTransformerService.preSubmissionTransform(
            formDefinition,
            submission,
            placeholders,
            source
        );

        //Assert initial submission is cleaned up
        assertThat(submission.get("name")).isNullOrEmpty();
        assertThat(jsonPatch.patches().size()).isEqualTo(3);

        //Patch for source is created
        AddOperation newArrayOperation = (AddOperation) jsonPatch.patches().stream().skip(jsonPatch.patches().size() - 3).findFirst().orElseThrow();
        assertThat(newArrayOperation.getOperation()).isEqualTo(Operation.ADD.toString());
        assertThat(newArrayOperation.getPath()).isEqualTo("/favorites/2");
        assertThat(newArrayOperation.getValue().isObject()).isTrue();

        AddOperation nameOperation = (AddOperation) jsonPatch.patches().stream().skip(jsonPatch.patches().size() - 2).findFirst().orElseThrow();
        assertThat(nameOperation.getOperation()).isEqualTo(Operation.ADD.toString());
        assertThat(nameOperation.getPath()).isEqualTo("/favorites/2/name");
        assertThat(nameOperation.getValue().textValue()).isEqualTo("Focaccia");

        AddOperation sizeOperation = (AddOperation) jsonPatch.patches().stream().skip(jsonPatch.patches().size() - 1).findFirst().orElseThrow();
        assertThat(sizeOperation.getOperation()).isEqualTo(Operation.ADD.toString());
        assertThat(sizeOperation.getPath()).isEqualTo("/favorites/2/size");
        assertThat(sizeOperation.getValue().textValue()).isEqualTo("big");

        JsonPatchService.apply(jsonPatch, source);
        assertThat(source.at("/favorites/2/name").textValue()).isEqualTo("Focaccia");
        assertThat(source.at("/favorites/2/size").textValue()).isEqualTo("big");
    }

    private ObjectNode submission() {
        ObjectNode submission = JsonNodeFactory.instance.objectNode();
        submission.put("name", "Focaccia");
        submission.put("size", "big");
        return submission;
    }

    private ObjectNode submissionNewArray() {
        ObjectNode submission = JsonNodeFactory.instance.objectNode();
        submission.put("_id", "3");
        submission.put("name", "Focaccia");
        submission.put("size", "big");
        return submission;
    }

    private ObjectNode placeholders() {
        ObjectNode placeholders = JsonNodeFactory.instance.objectNode();
        ObjectNode jsonNode = JsonNodeFactory.instance.objectNode();
        jsonNode.put("breadId", "2");
        placeholders.set("pv", jsonNode);
        return placeholders;
    }

    private ObjectNode source() {
        var source = JsonNodeFactory.instance.objectNode();
        var breads = JsonNodeFactory.instance.arrayNode();

        var whiteBread = JsonNodeFactory.instance.objectNode();
        whiteBread.put("_id", "1");
        whiteBread.put("name", "White bread");
        whiteBread.put("size", "medium");
        breads.add(whiteBread);

        var pitaBread = JsonNodeFactory.instance.objectNode();
        pitaBread.put("_id", "2");
        pitaBread.put("name", "Pita bread");
        pitaBread.put("size", "small");
        breads.add(pitaBread);

        source.set("favorites", breads);
        return source;
    }
}