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

package com.ritense.form.service.impl;

import com.ritense.form.BaseIntegrationTest;
import com.ritense.form.domain.request.CreateFormDefinitionRequest;
import com.ritense.form.domain.request.ModifyFormDefinitionRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import javax.transaction.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Tag("integration")
@Transactional
public class FormIoFormDefinitionServiceIntTest extends BaseIntegrationTest {

    private FormIoFormDefinitionService formIoFormDefinitionService;

    @BeforeEach
    public void setUp() {
        formIoFormDefinitionService = new FormIoFormDefinitionService(formDefinitionRepository);
    }

    @Test
    public void shouldCreateFormDefinition() {
        var request = new CreateFormDefinitionRequest(DEFAULT_FORM_DEFINITION_NAME, "{}", false);
        var formDefinition = formIoFormDefinitionService.createFormDefinition(request);

        assertThat(formDefinition.getName()).isEqualTo(DEFAULT_FORM_DEFINITION_NAME);
        assertThat(formDefinition.getFormDefinition().toString()).isEqualTo("{}");
        assertThat(formDefinition.isReadOnly()).isFalse();
    }

    @Test
    public void shouldCreateReadonlyFormDefinition() {
        var request = new CreateFormDefinitionRequest(DEFAULT_FORM_DEFINITION_NAME, "{}", true);
        var formDefinition = formIoFormDefinitionService.createFormDefinition(request);
        assertThat(formDefinition.isReadOnly()).isTrue();
    }

    @Test
    public void shouldModifyFormDefinition() {
        var request = new CreateFormDefinitionRequest(DEFAULT_FORM_DEFINITION_NAME, "{}", false);
        var formDefinition = formIoFormDefinitionService.createFormDefinition(request);

        assertThat(formDefinition.isReadOnly()).isFalse();

        var modifyRequest = new ModifyFormDefinitionRequest(
            formDefinition.getId(),
            formDefinition.getName(),
            formDefinition.getFormDefinition().toString()
        );
        var formDefinitionModified = formIoFormDefinitionService.modifyFormDefinition(modifyRequest);
        assertThat(formDefinitionModified.getName()).isEqualTo(formDefinition.getName());
        assertThat(formDefinitionModified.getFormDefinition()).isEqualTo(formDefinition.getFormDefinition());
    }

    @Test
    public void shouldNotModifyFormDefinition() {
        var request = new CreateFormDefinitionRequest(DEFAULT_FORM_DEFINITION_NAME, "{}", true);
        var formDefinition = formIoFormDefinitionService.createFormDefinition(request);

        assertThat(formDefinition.isReadOnly()).isTrue();

        var modifyRequest = new ModifyFormDefinitionRequest(
            formDefinition.getId(),
            formDefinition.getName(),
            formDefinition.getFormDefinition().toString()
        );
        assertThrows(IllegalStateException.class, () -> formIoFormDefinitionService.modifyFormDefinition(modifyRequest));
    }

    @Test
    public void shouldModifyFormDefinitionSystem() {
        var request = new CreateFormDefinitionRequest(DEFAULT_FORM_DEFINITION_NAME, "{}", true);
        var formDefinition = formIoFormDefinitionService.createFormDefinition(request);

        assertThat(formDefinition.isReadOnly()).isTrue();
        var formDefinitionModified = formIoFormDefinitionService.modifyFormDefinition(
            formDefinition.getId(),
            formDefinition.getName(),
            formDefinition.getFormDefinition().toString(),
            true
        );

        assertThat(formDefinitionModified.getName()).isEqualTo(formDefinition.getName());
        assertThat(formDefinitionModified.getFormDefinition()).isEqualTo(formDefinition.getFormDefinition());
    }

}