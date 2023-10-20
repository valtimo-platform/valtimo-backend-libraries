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

package com.ritense.valtimo.service;

import com.ritense.valtimo.BaseIntegrationTest;
import com.ritense.valtimo.exception.FileExtensionNotSupportedException;
import com.ritense.valtimo.exception.NoFileExtensionFoundException;
import com.ritense.valtimo.exception.ProcessNotDeployableException;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.repository.DecisionDefinition;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

@Transactional
class CamundaProcessServiceIntTest extends BaseIntegrationTest {

    @Value("classpath:examples/bpmn/*.xml")
    Resource[] bpmn;
    @Value("classpath:examples/dmn/*.xml")
    Resource[] dmn;
    @Value("classpath:examples/test/*")
    Resource[] test;

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private CamundaProcessService camundaProcessService;

    @Test
    void shouldDeployNewProcess() throws IOException, FileExtensionNotSupportedException, NoFileExtensionFoundException, ProcessNotDeployableException {
        List<Resource> processes = List.of(bpmn);
            camundaProcessService.deploy(
                "aProcessName.bpmn",
                new ByteArrayInputStream(processes.stream().filter(process -> Objects.equals(process.getFilename(), "shouldDeploy.xml"))
                    .findFirst().orElseGet(() -> new ByteArrayResource(new byte[]{})).getInputStream().readAllBytes())
            );
        List<ProcessDefinition> definitions = camundaProcessService.getDeployedDefinitions();
        Assertions.assertTrue(definitions.stream().anyMatch(processDefinition -> processDefinition.getKey().equals("deployedProcess")));
    }

    @Test
    void shouldDeployNewDmn() throws IOException, FileExtensionNotSupportedException, NoFileExtensionFoundException, ProcessNotDeployableException {
        List<Resource> tables = List.of(dmn);
            camundaProcessService.deploy(
                "aDmnName.dmn",
                new ByteArrayInputStream(tables.stream().filter(table -> Objects.equals(table.getFilename(), "sampleDecisionTable.xml"))
                    .findFirst().orElseGet(() -> new ByteArrayResource(new byte[]{})).getInputStream().readAllBytes())
            );
        List<DecisionDefinition> definitions = repositoryService.createDecisionDefinitionQuery().list();
        Assertions.assertTrue(definitions.stream().anyMatch(decisionDefinition -> decisionDefinition.getKey().equals("Evenementenvergunning-risico")));
    }

    @Test
    void shouldNotDeployFileWithInvalidExtension() {
        List<Resource> testFiles = List.of(test);
        String textFileName = "aTextFile.txt";
        Assertions.assertThrows(FileExtensionNotSupportedException.class,
            () ->
                    camundaProcessService.deploy(
                        textFileName,
                        new ByteArrayInputStream(testFiles.stream().filter(testFile -> Objects.equals(testFile.getFilename(), "sampleTextFile.txt"))
                            .findFirst().orElseGet(() -> new ByteArrayResource(new byte[]{})).getInputStream().readAllBytes())
                    )
            );
        List<DecisionDefinition> dmnDefinitions = repositoryService.createDecisionDefinitionQuery().list();
        List<ProcessDefinition> bpmnDefinitions = camundaProcessService.getDeployedDefinitions();
        Assertions.assertFalse(dmnDefinitions.stream().anyMatch(dmnDefinition -> dmnDefinition.getResourceName().equals(textFileName)));
        Assertions.assertFalse(bpmnDefinitions.stream().anyMatch(bpmnDefinition -> bpmnDefinition.getResourceName().equals(textFileName)));
    }

    @Test
    void shouldNotDeployFileWithoutExtension() throws IOException {
        List<Resource> testFiles = List.of(test);
        String sampleFileName = "aFileName";
        Assertions.assertThrows(NoFileExtensionFoundException.class,
            () ->
                    camundaProcessService.deploy(
                        sampleFileName,
                        new ByteArrayInputStream(testFiles.stream().filter(testFile -> Objects.equals(testFile.getFilename(), "sampleTestFile"))
                            .findFirst().orElseGet(() -> new ByteArrayResource(new byte[]{})).getInputStream().readAllBytes())

                    )
            );
        List<DecisionDefinition> dmnDefinitions = repositoryService.createDecisionDefinitionQuery().list();
        List<ProcessDefinition> bpmnDefinitions = camundaProcessService.getDeployedDefinitions();
        Assertions.assertFalse(dmnDefinitions.stream().anyMatch(dmnDefinition -> dmnDefinition.getResourceName().equals(sampleFileName)));
        Assertions.assertFalse(bpmnDefinitions.stream().anyMatch(bpmnDefinition -> bpmnDefinition.getResourceName().equals(sampleFileName)));
    }

    @Test
    void shouldNotDeployNewSystemProcess() {
        List<Resource> processes = List.of(bpmn);
        Assertions.assertThrows(ProcessNotDeployableException.class, () -> camundaProcessService.deploy(
                "aProcessName.bpmn",
                new ByteArrayInputStream(processes.stream().filter(process -> Objects.equals(process.getFilename(), "shouldNotDeploy.xml"))
                        .findFirst().orElseGet(() -> new ByteArrayResource(new byte[]{})).getInputStream().readAllBytes())
        ));
        List<ProcessDefinition> definitions = camundaProcessService.getDeployedDefinitions();
        Assertions.assertFalse(definitions.stream().anyMatch(processDefinition -> processDefinition.getKey().equals("firstProcess")));
        Assertions.assertFalse(definitions.stream().anyMatch(processDefinition -> processDefinition.getKey().equals("secondProcess")));
    }

    @Test
    void shouldNotUpdateExistingSystemProcess() throws IOException {
        List<Resource> processes = List.of(bpmn);
        var systemProcessModel = Bpmn.readModelFromStream(new ByteArrayInputStream(processes.stream().filter(process -> Objects.equals(process.getFilename(), "systemProcess.xml"))
                .findFirst().orElseGet(() -> new ByteArrayResource(new byte[]{})).getInputStream().readAllBytes()));
        repositoryService.createDeployment().addModelInstance("systemProcess.bpmn", systemProcessModel).deploy();
        List<ProcessDefinition> definitions = camundaProcessService.getDeployedDefinitions();
        Assertions.assertTrue(definitions.stream().anyMatch(processDefinition -> processDefinition.getKey().equals("secondProcess")));

        Assertions.assertThrows(ProcessNotDeployableException.class, () -> camundaProcessService.deploy(
                "aProcessName.bpmn",
                new ByteArrayInputStream(processes.stream().filter(process -> Objects.equals(process.getFilename(), "shouldNotDeploy.xml"))
                        .findFirst().orElseGet(() -> new ByteArrayResource(new byte[]{})).getInputStream().readAllBytes())
        ));
        Assertions.assertFalse(definitions.stream().anyMatch(processDefinition -> processDefinition.getKey().equals("firstProcess")));
        Assertions.assertTrue(definitions.stream().anyMatch(processDefinition -> processDefinition.getKey().equals("secondProcess")));
    }
}
