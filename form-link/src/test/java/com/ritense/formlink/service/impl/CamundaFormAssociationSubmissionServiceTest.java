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
import com.ritense.document.domain.impl.JsonDocumentContent;
import com.ritense.document.domain.impl.JsonSchemaDocument;
import com.ritense.document.service.impl.JsonSchemaDocumentService;
import com.ritense.form.domain.FormIoFormDefinition;
import com.ritense.form.service.impl.FormIoFormDefinitionService;
import com.ritense.formlink.BaseTest;
import com.ritense.formlink.domain.impl.formassociation.CamundaFormAssociation;
import com.ritense.formlink.service.SubmissionTransformerService;
import com.ritense.formlink.service.impl.result.FormSubmissionResultFailed;
import com.ritense.formlink.service.impl.result.FormSubmissionResultSucceeded;
import com.ritense.formlink.service.result.FormSubmissionResult;
import com.ritense.processdocument.service.impl.CamundaProcessJsonSchemaDocumentAssociationService;
import com.ritense.processdocument.service.impl.CamundaProcessJsonSchemaDocumentService;
import com.ritense.processdocument.service.impl.result.ModifyDocumentAndCompleteTaskResultSucceeded;
import com.ritense.valtimo.service.CamundaTaskService;
import com.ritense.valtimo.task.publictask.PublicTaskTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CamundaFormAssociationSubmissionServiceTest extends BaseTest {

    private FormIoFormDefinition formDefinition;
    private CamundaFormAssociation formAssociation;
    private CamundaFormAssociationSubmissionService formAssociationSubmissionService;
    private FormIoFormDefinitionService formDefinitionService;
    private JsonSchemaDocumentService documentService;
    private CamundaProcessJsonSchemaDocumentAssociationService processDocumentAssociationService;
    private CamundaFormAssociationService formAssociationService;
    private CamundaProcessJsonSchemaDocumentService processDocumentService;
    private PublicTaskTokenService publicTaskTokenService;
    private CamundaTaskService camundaTaskService;
    private SubmissionTransformerService submissionTransformerService;
    private ApplicationEventPublisher applicationEventPublisher;

    @BeforeEach
    public void beforeEach() {
        formDefinitionService = mock(FormIoFormDefinitionService.class);
        documentService = mock(JsonSchemaDocumentService.class);
        processDocumentAssociationService = mock(CamundaProcessJsonSchemaDocumentAssociationService.class);
        formAssociationService = mock(CamundaFormAssociationService.class);
        processDocumentService = mock(CamundaProcessJsonSchemaDocumentService.class);
        publicTaskTokenService = mock(PublicTaskTokenService.class);
        camundaTaskService = mock(CamundaTaskService.class);
        submissionTransformerService = mock(FormIoJsonPatchSubmissionTransformerService.class);
        applicationEventPublisher = mock(ApplicationEventPublisher.class);

        formAssociationSubmissionService = new CamundaFormAssociationSubmissionService(
            formDefinitionService,
            documentService,
            processDocumentAssociationService,
            formAssociationService,
            processDocumentService,
            publicTaskTokenService,
            camundaTaskService,
            submissionTransformerService,
            applicationEventPublisher
        );
    }

    @Test
    public void shouldNotHandleSubmission() {
        String processDefinitionKey = "myProcessKey";
        String formLinkId = "myFormLinkId";
        String documentId = UUID.randomUUID().toString();
        String taskInstanceId = "123";
        ObjectNode formData = JsonNodeFactory.instance.objectNode();
        formData.put("name", "value");

        final FormSubmissionResult formSubmissionResult = formAssociationSubmissionService
            .handleSubmission(processDefinitionKey, formLinkId, documentId, taskInstanceId, formData);

        assertThat(formSubmissionResult).isInstanceOf(FormSubmissionResultFailed.class);
        assertThat(formSubmissionResult.errors()).isNotEmpty();
    }

    @Test
    public void shouldHandleSubmission() throws IOException {
        //Given
        String processDefinitionKey = "myProcessKey";
        String formLinkId = "myFormLinkId";
        String documentId = UUID.randomUUID().toString();
        String taskInstanceId = "123";
        ObjectNode formData = formData();

        formAssociation = processFormAssociation(UUID.randomUUID(), UUID.randomUUID()).getFormAssociations().iterator().next();
        when(formAssociationService.getFormAssociationByFormLinkId(any(), any())).thenReturn(Optional.of(formAssociation));

        when(processDocumentAssociationService.findProcessDocumentDefinition(any(), anyLong()))
            .thenReturn(Optional.of(processDocumentDefinition("aName")));

        formDefinition = formDefinitionOf("user-task");
        when(formDefinitionService.getFormDefinitionById(any())).thenReturn(Optional.of(formDefinition));

        final var jsonDocumentContent = JsonDocumentContent.build(formData);
        final Optional<JsonSchemaDocument> document = Optional.of(createDocument(jsonDocumentContent));
        when(documentService.findBy(any())).thenReturn(document);
        when(processDocumentService.dispatch(any())).thenReturn(new ModifyDocumentAndCompleteTaskResultSucceeded(document.orElseThrow()));

        //When
        final FormSubmissionResult formSubmissionResult = formAssociationSubmissionService
            .handleSubmission(processDefinitionKey, formLinkId, documentId, taskInstanceId, formData);

        //Then
        assertThat(formSubmissionResult).isInstanceOf(FormSubmissionResultSucceeded.class);
        assertThat(formSubmissionResult.errors()).isEmpty();
    }

    private ObjectNode formData() {
        ObjectNode formData = JsonNodeFactory.instance.objectNode();
        formData.put("voornaam", "jan");

        //Number
        formData.put("number", 123);

        //Checkbox
        formData.put("checkbox", true);

        //Select Boxes
        ObjectNode dataSelectBoxes = JsonNodeFactory.instance.objectNode();
        dataSelectBoxes.put("option1", "true");
        formData.set("selectBoxes", dataSelectBoxes);

        //Date / Time
        formData.put("dateTime", "2020-09-24T12:00:00+02:00");

        //Tags
        formData.put("tags", "tag1,tag2,tag3");

        //Phone Number
        formData.put("phoneNumber", "(020) 697-8255");

        //Url
        formData.put("url", "http://www.nu.nl");

        //Email
        formData.put("email", "a@a.com");

        //Radio
        formData.put("radio", "radio1");

        //Password
        formData.put("password", "password");

        //Upload
        var bijlageArray = JsonNodeFactory.instance.arrayNode();
        ObjectNode file = JsonNodeFactory.instance.objectNode();

        file.put("storage", "url");
        file.put("name", "test-736b4bfc-5ed0-4fac-a9c2-89629ccbe451.rtf");
        file.put("url", "https://console.test.valtimo.nl/api/form-file?baseUrl=http%3A%2F%2Flocalhost%3A4200&project=&form=/test-736b4bfc-5ed0-4fac-a9c2-89629ccbe451.rtf");
        file.put("size", 391);
        file.put("type", "text/rtf");
        ObjectNode data = JsonNodeFactory.instance.objectNode();
        data.put("key", "test-736b4bfc-5ed0-4fac-a9c2-89629ccbe451.rtf");
        data.put("baseUrl", "http://localhost:4200");
        data.put("project", "");
        data.put("form", "");
        file.set("data", data);
        file.put("originalName", "test.rtf");
        bijlageArray.add(file);
        formData.set("bijlagen", bijlageArray);
        return formData;
    }

}