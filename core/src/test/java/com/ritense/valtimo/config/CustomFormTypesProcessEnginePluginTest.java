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

package com.ritense.valtimo.config;

import com.ritense.valtimo.domain.formfields.ChoiceFieldFormFieldType;
import com.ritense.valtimo.domain.formfields.FileUploadFormFieldType;
import com.ritense.valtimo.domain.formfields.TextAreaFormFieldType;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.form.type.AbstractFormFieldType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CustomFormTypesProcessEnginePluginTest {

    CustomFormTypesProcessEnginePlugin customFormTypesProcessEnginePlugin = new CustomFormTypesProcessEnginePlugin();
    ProcessEngineConfigurationImpl processEngineConfiguration = mock(ProcessEngineConfigurationImpl.class);
    List<AbstractFormFieldType> formTypes;

    @BeforeEach
    void setUp() {
        formTypes = new ArrayList();
        when(processEngineConfiguration.getCustomFormTypes()).thenReturn(formTypes);
    }

    @Test
    void shouldAddFileUploadFormFieldTypeOnPreInit() {
        customFormTypesProcessEnginePlugin.preInit(processEngineConfiguration);
        assertThat(formTypes, hasItem(isA(FileUploadFormFieldType.class)));
    }

    @Test
    void shouldAddTextAreaFormFieldTypeOnPreInit() {
        customFormTypesProcessEnginePlugin.preInit(processEngineConfiguration);
        assertThat(formTypes, hasItem(isA(TextAreaFormFieldType.class)));
    }

    @Test
    void shouldAddChoiceFieldFormFieldTypeOnPreInit() {
        customFormTypesProcessEnginePlugin.preInit(processEngineConfiguration);
        assertThat(formTypes, hasItem(isA(ChoiceFieldFormFieldType.class)));
    }

}