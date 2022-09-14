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

import com.ritense.form.BaseTest;
import com.ritense.form.domain.FormIoFormDefinition;
import com.ritense.form.repository.FormDefinitionRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FormIoFormDefinitionServiceTest extends BaseTest {

    private FormDefinitionRepository formDefinitionRepository;
    private FormIoFormDefinitionService formIoFormDefinitionService;

    @BeforeEach
    public void setUp() {
        formDefinitionRepository = mock(FormDefinitionRepository.class);
        formIoFormDefinitionService = new FormIoFormDefinitionService(formDefinitionRepository);
    }

    @Test
    public void shouldCallRepositoryWhenGettingFormByName() {
        FormIoFormDefinition formIoFormDefinition = mock(FormIoFormDefinition.class);
        when(formDefinitionRepository.findByName("test")).thenReturn(Optional.of(formIoFormDefinition));
        Optional<FormIoFormDefinition> formDefinition = formIoFormDefinitionService.getFormDefinitionByName("test");
        assertEquals(formIoFormDefinition, formDefinition.get());
    }

    @Test
    public void shouldCallRepositoryWhenGettingFormByNameIgnoringCase() {
        FormIoFormDefinition formIoFormDefinition = mock(FormIoFormDefinition.class);
        when(formDefinitionRepository.findByNameIgnoreCase("test")).thenReturn(Optional.of(formIoFormDefinition));
        Optional<FormIoFormDefinition> formDefinition = formIoFormDefinitionService.getFormDefinitionByNameIgnoringCase("test");
        assertEquals(formIoFormDefinition, formDefinition.get());
    }
}
