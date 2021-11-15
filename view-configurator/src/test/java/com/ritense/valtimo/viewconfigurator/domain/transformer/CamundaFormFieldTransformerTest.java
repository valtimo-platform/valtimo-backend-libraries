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

package com.ritense.valtimo.viewconfigurator.domain.transformer;

import com.ritense.valtimo.viewconfigurator.domain.ProcessDefinitionVariable;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaFormField;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CamundaFormFieldTransformerTest {

    private CamundaFormField camundaFormField;

    @BeforeEach
    public void setUp() throws Exception {
        this.camundaFormField = mock(CamundaFormField.class);
        when(camundaFormField.getCamundaLabel()).thenReturn("test");
        when(camundaFormField.getCamundaId()).thenReturn("test");

    }

    @Test
    public void dateFormField() {
        typeFormFieldTest("date");
    }

    @Test
    public void enumFormField() {
        typeFormFieldTest("enum");
    }

    @Test
    public void stringFormField() {
        typeFormFieldTest("string");
    }

    @Test
    public void booleanFormField() {
        typeFormFieldTest("boolean");
    }

    @Test
    public void longFormField() {
        typeFormFieldTest("long");
    }

    @Test
    public void fileUploadFormField() {
        typeFormFieldTest("FileUpload");
    }

    @Test
    public void textAreaFormField() {
        unSupportedTypeFormFieldTest("TextArea");
    }

    @Test
    public void choiceFieldFormField() {
        unSupportedTypeFormFieldTest("ChoiceField");
    }

    private void unSupportedTypeFormFieldTest(String camundaType) {
        when(camundaFormField.getCamundaType()).thenReturn(camundaType);
        Optional<ProcessDefinitionVariable> processDefinitionVariable = CamundaFormFieldTransformer.transform.apply(camundaFormField);
        assertTrue(!processDefinitionVariable.isPresent());
    }

    private void typeFormFieldTest(String camundaType) {
        when(camundaFormField.getCamundaType()).thenReturn(camundaType);
        Optional<ProcessDefinitionVariable> processDefinitionVariable = CamundaFormFieldTransformer.transform.apply(camundaFormField);
        assertTrue(processDefinitionVariable.isPresent());
        assertTrue(processDefinitionVariable.get().getTypeName().equalsIgnoreCase(camundaType));
    }

}