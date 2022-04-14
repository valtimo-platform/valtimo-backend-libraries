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

package com.ritense.valtimo.viewconfigurator.service;

import com.ritense.valtimo.contract.exception.ProcessNotFoundException;
import com.ritense.valtimo.viewconfigurator.domain.ProcessDefinitionVariable;
import com.ritense.valtimo.viewconfigurator.service.impl.ProcessDefinitionVariableServiceImpl;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ProcessDefinitionVariableServiceImplTest {

    private RepositoryService repositoryService;
    private ProcessDefinitionVariableServiceImpl processDefinitionVariableServiceImpl;
    private BpmnModelInstance bpmnModelInstance;

    @BeforeEach
    public void setUp() {
        repositoryService = mock(RepositoryService.class);
        processDefinitionVariableServiceImpl = new ProcessDefinitionVariableServiceImpl(repositoryService);
        bpmnModelInstance = loadBPMNFile("camundaProcessModelTest.bpmn");
    }

    @Test
    public void modelWithNoFormData() throws Exception {
        BpmnModelInstance noFormDataBpmnModel = loadBPMNFile("camundaProcessModelTest-no-formdata.bpmn");

        when(repositoryService.getBpmnModelInstance(any())).thenReturn(noFormDataBpmnModel);
        Set<ProcessDefinitionVariable> processDefinitionVariables = processDefinitionVariableServiceImpl.extractVariables("notUsedSinceMocked");

        assertTrue(processDefinitionVariables.isEmpty());
    }

    @Test
    public void modelWithCustomFormfieldType() {
        BpmnModelInstance noFormDataBpmnModel = loadBPMNFile("camundaProcessModelTest-custom-formfieldtype.bpmn");
        when(repositoryService.getBpmnModelInstance(any())).thenReturn(noFormDataBpmnModel);
        assertThrows(IllegalStateException.class, () -> processDefinitionVariableServiceImpl.extractVariables(any()));
    }

    @Test
    public void processModelNull() {
        when(repositoryService.getBpmnModelInstance(any())).thenReturn(null);
        assertThrows(Exception.class, () -> processDefinitionVariableServiceImpl.extractVariables("notUsedSinceMocked"));
    }

    @Test
    public void processModelRetractVariables() throws Exception {
        when(repositoryService.getBpmnModelInstance(any())).thenReturn(bpmnModelInstance);
        Set<ProcessDefinitionVariable> processDefinitionVariables = processDefinitionVariableServiceImpl.extractVariables("notUsedSinceMocked");

        // 5 variables defined, but two times a variable with name (id) "Naam" in the BPMN model
        assertFalse(processDefinitionVariables.isEmpty());
        assertEquals(4, processDefinitionVariables.size());
    }

    @Test
    public void nonExistentProcessDefinition() throws Exception {
        //mocking
        when(repositoryService.getBpmnModelInstance(any())).thenReturn(bpmnModelInstance);

        when(processDefinitionVariableServiceImpl.extractVariables(any())).thenReturn(null);

        //test
        final String processDefinitionId = "mockedProcessId";

        assertThrows(ProcessNotFoundException.class, () -> processDefinitionVariableServiceImpl.extractVariables(processDefinitionId));
    }

    private BpmnModelInstance loadBPMNFile(String fileName) {
        ClassLoader loader = ProcessDefinitionVariableServiceImpl.class.getClassLoader();
        File camundaFile = new File(loader.getResource(fileName).getFile().replace("%20", " "));
        try {
            return Bpmn.readModelFromStream(new FileInputStream(camundaFile));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}