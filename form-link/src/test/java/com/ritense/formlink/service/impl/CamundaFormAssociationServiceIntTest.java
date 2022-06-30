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

import com.ritense.form.domain.FormDefinition;
import com.ritense.form.service.impl.FormIoFormDefinitionService;
import com.ritense.formlink.BaseIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("integration")
@Transactional
public class CamundaFormAssociationServiceIntTest extends BaseIntegrationTest {

    @Inject
    public CamundaFormAssociationService formAssociationService;

    @Inject
    public FormIoFormDefinitionService formDefinitionService;

    private FormDefinition formDefinition;

    @BeforeEach
    public void beforeEach() throws IOException {
        formDefinition = formDefinitionService.createFormDefinition(createFormDefinitionRequest());
    }

    @Test
    public void shouldCreateFormAssociation() {
        final var createFormAssociationRequest = createUserTaskFormAssociationRequest(formDefinition.getId());

        final var formAssociationSaved = formAssociationService.createFormAssociation(createFormAssociationRequest);

        final var formAssociation = formAssociationService.getFormAssociationById(
            createFormAssociationRequest.getProcessDefinitionKey(),
            formAssociationSaved.getId()
        ).orElseThrow();

        assertThat(formAssociation).isNotNull();
        assertThat(formAssociation.getFormLink().getFormId()).isEqualTo(createFormAssociationRequest.getFormLinkRequest().getFormId());
        assertThat(formAssociation.getFormLink().getId()).isEqualTo(createFormAssociationRequest.getFormLinkRequest().getId());
    }

    @Test
    public void shouldGetFormAssociation() {
        final var createFormAssociationRequest = createUserTaskFormAssociationRequest(formDefinition.getId());

        final var savedFormAssociation = formAssociationService.createFormAssociation(createFormAssociationRequest);

        final var formAssociation = formAssociationService
            .getFormAssociationById(PROCESS_DEFINITION_KEY, savedFormAssociation.getId()).orElseThrow();

        assertThat(formAssociation).isNotNull();
        assertThat(formAssociation.getFormLink().getFormId()).isEqualTo(createFormAssociationRequest.getFormLinkRequest().getFormId());
        assertThat(formAssociation.getFormLink().getId()).isEqualTo(createFormAssociationRequest.getFormLinkRequest().getId());
    }

    @Test
    public void shouldGetUserTaskFormAssociation() {
        final var createFormAssociationRequest = createUserTaskFormAssociationRequest(formDefinition.getId());

        final var savedFormAssociation = formAssociationService.createFormAssociation(createFormAssociationRequest);

        final var formAssociation = formAssociationService
            .getFormAssociationById(PROCESS_DEFINITION_KEY, savedFormAssociation.getId()).orElseThrow();

        assertThat(formAssociation).isNotNull();
        assertThat(formAssociation.getFormLink().getFormId()).isEqualTo(createFormAssociationRequest.getFormLinkRequest().getFormId());
        assertThat(formAssociation.getFormLink().getId()).isEqualTo(createFormAssociationRequest.getFormLinkRequest().getId());
    }

    @Test
    public void shouldModifyFormAssociation() throws IOException {
        final var createFormAssociationRequest = createUserTaskFormAssociationRequest(formDefinition.getId());

        final var savedFormAssociation = formAssociationService.createFormAssociation(createFormAssociationRequest);

        final var secondFormDefinition = formDefinitionService.createFormDefinition(createFormDefinitionRequest());
        final var modifyFormAssociationRequest = modifyFormAssociationRequest(savedFormAssociation.getId(), secondFormDefinition.getId(), true);

        formAssociationService.modifyFormAssociation(modifyFormAssociationRequest);

        final var formAssociation = formAssociationService
            .getFormAssociationByFormLinkId(PROCESS_DEFINITION_KEY, modifyFormAssociationRequest.getFormLinkRequest().getId()).orElseThrow();

        assertThat(formAssociation).isNotNull();
        assertThat(formAssociation.getFormLink().getId()).isEqualTo(createFormAssociationRequest.getFormLinkRequest().getId());
        assertThat(formAssociation.getFormLink().getFormId()).isEqualTo(modifyFormAssociationRequest.getFormLinkRequest().getFormId());
        assertThat(formAssociation.getFormLink().getFormId()).isNotEqualTo(createFormAssociationRequest.getFormLinkRequest().getFormId());
    }

    @Test
    public void shouldGetStartEventFormAssociation() {
        final var request = createFormAssociationRequestWithStartEvent(formDefinition.getId());

        final var savedFormAssociation = formAssociationService.createFormAssociation(request);

        final var formDefinition = formAssociationService
            .getStartEventFormDefinition(PROCESS_DEFINITION_KEY).orElseThrow();

        assertThat(formDefinition).isNotNull();
        assertThat(formDefinition.get("formAssociation")).isNotNull();

    }

    @Test
    public void shouldCreateUserFormAssociation() {
        final var request = createUserTaskFormAssociationRequest(formDefinition.getId());

        final var savedFormAssociation = formAssociationService.createFormAssociation(
            request.getProcessDefinitionKey(),
            "myForm",
            request.getFormLinkRequest().getId(),
            request.getFormLinkRequest().getType()
        );

        final var formDefinition = formAssociationService
            .getFormDefinitionByFormLinkId(PROCESS_DEFINITION_KEY, request.getFormLinkRequest().getId())
            .orElseThrow();

        assertThat(formDefinition).isNotNull();
        assertThat(formDefinition.get("formAssociation")).isNotNull();
    }

}