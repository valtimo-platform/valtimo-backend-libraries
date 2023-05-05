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

package com.ritense.formlink.autodeployment;

import com.ritense.form.service.impl.FormIoFormDefinitionService;
import com.ritense.formlink.BaseTest;
import com.ritense.formlink.domain.impl.formassociation.FormAssociationType;
import com.ritense.formlink.domain.request.FormLinkRequest;
import com.ritense.formlink.service.impl.CamundaFormAssociationService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FormLinkDeploymentServiceTest extends BaseTest {

    private FormLinkDeploymentService formLinkDeploymentService;
    private ResourceLoader resourceLoader;
    private CamundaFormAssociationService formAssociationService;
    private FormIoFormDefinitionService formDefinitionService;

    @Test
    void deployAllFromResourceFiles() throws IOException {
        resourceLoader = new DefaultResourceLoader();
        formAssociationService = mock(CamundaFormAssociationService.class);
        formDefinitionService = mock(FormIoFormDefinitionService.class);

        formLinkDeploymentService = new FormLinkDeploymentService(
            resourceLoader,
            formAssociationService,
            formDefinitionService
        );

        var formDefinition = formDefinitionOf("form-example");
        when(formDefinitionService.getFormDefinitionByName("form-example")).thenReturn(Optional.of(formDefinition));

        // When
        formLinkDeploymentService.deployAllFromResourceFiles();

        // Then
        var formLinkRequestArgumentCaptor = ArgumentCaptor.forClass(FormLinkRequest.class);

        verify(formAssociationService).upsertFormAssociation(eq("bezwaar"), formLinkRequestArgumentCaptor.capture());

        assertThat(formLinkRequestArgumentCaptor.getValue().getType()).isEqualTo(FormAssociationType.USER_TASK);
        assertThat(formLinkRequestArgumentCaptor.getValue().getId()).isEqualTo("someId");
        assertThat(formLinkRequestArgumentCaptor.getValue().getFormId()).isEqualTo(formDefinition.getId());
    }

}
