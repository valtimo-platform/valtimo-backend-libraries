/*
 * Copyright 2015-2024 Ritense BV, the Netherlands.
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

import static org.assertj.core.api.Assertions.assertThat;

import com.ritense.authorization.AuthorizationContext;
import com.ritense.valtimo.BaseIntegrationTest;
import com.ritense.valtimo.camunda.domain.CamundaProcessDefinition;
import com.ritense.valtimo.exception.FileExtensionNotSupportedException;
import com.ritense.valtimo.exception.NoFileExtensionFoundException;
import com.ritense.valtimo.exception.ProcessNotDeployableException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.repository.DecisionDefinition;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.transaction.annotation.Transactional;

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
    void shouldDeployNewProcess() throws IOException {
        var latestDeploymentId = findLatestProcessDefinitionDeployedProcess("deployedProcess")
            .map(CamundaProcessDefinition::getDeploymentId);
        List<Resource> processes = List.of(bpmn);
        AuthorizationContext.runWithoutAuthorization(() -> {
            camundaProcessService.deploy(
                "aProcessName.bpmn",
                new ByteArrayInputStream(processes.stream().filter(process -> Objects.equals(process.getFilename(), "shouldDeploy.xml"))
                    .findFirst().orElseGet(() -> new ByteArrayResource(new byte[]{})).getInputStream().readAllBytes())
            );
            return null;
        });
        var definition = findLatestProcessDefinitionDeployedProcess("deployedProcess").orElseThrow();
        //Make sure we have deployed a new version if one already existed (autodeployment)
        latestDeploymentId.ifPresent(
            deploymentId -> assertThat(deploymentId).isNotEqualTo(definition.getDeploymentId())
        );

        try (InputStream inputStream = repositoryService.getProcessModel(definition.getId())) {
            BpmnModelInstance model = Bpmn.readModelFromStream(inputStream);
            Process processModel = model.getDefinitions().getChildElementsByType(Process.class).stream()
                .filter(process -> process.getId().equals(definition.getKey()))
                .findFirst()
                .orElseThrow();
            assertThat(processModel.isExecutable()).isTrue();
        }
    }

    @NotNull
    private Optional<CamundaProcessDefinition> findLatestProcessDefinitionDeployedProcess(String processName) {
        return AuthorizationContext
            .runWithoutAuthorization(() -> camundaProcessService.getDeployedDefinitions())
            .stream()
            .filter(processDefinition -> processDefinition.getKey().equals(processName))
            .findFirst();
    }

    @Test
    void shouldDeployNewDmn() {
        List<Resource> tables = List.of(dmn);
        AuthorizationContext.runWithoutAuthorization(() -> {
            camundaProcessService.deploy(
                "aDmnName.dmn",
                new ByteArrayInputStream(tables.stream().filter(table -> Objects.equals(table.getFilename(), "sampleDecisionTable.xml"))
                    .findFirst().orElseGet(() -> new ByteArrayResource(new byte[]{})).getInputStream().readAllBytes())
            );
            return null;
        });
        List<DecisionDefinition> definitions = repositoryService.createDecisionDefinitionQuery().list();
        Assertions.assertTrue(definitions.stream().anyMatch(decisionDefinition -> decisionDefinition.getKey().equals("Evenementenvergunning-risico")));
    }

    @Test
    void shouldNotDeployFileWithInvalidExtension() {
        List<Resource> testFiles = List.of(test);
        String textFileName = "aTextFile.txt";
        Assertions.assertThrows(FileExtensionNotSupportedException.class,
            () -> AuthorizationContext.runWithoutAuthorization(() -> {
                camundaProcessService.deploy(
                        textFileName,
                        new ByteArrayInputStream(testFiles.stream().filter(testFile -> Objects.equals(testFile.getFilename(), "sampleTextFile.txt"))
                                .findFirst().orElseGet(() -> new ByteArrayResource(new byte[]{})).getInputStream().readAllBytes())
                );
                return null;
            }
        ));
        List<DecisionDefinition> dmnDefinitions = repositoryService.createDecisionDefinitionQuery().list();
        List<CamundaProcessDefinition> bpmnDefinitions = AuthorizationContext
                .runWithoutAuthorization(() -> camundaProcessService.getDeployedDefinitions());
        Assertions.assertFalse(dmnDefinitions.stream().anyMatch(dmnDefinition -> dmnDefinition.getResourceName().equals(textFileName)));
        Assertions.assertFalse(bpmnDefinitions.stream().anyMatch(bpmnDefinition -> bpmnDefinition.getResourceName().equals(textFileName)));
    }

    @Test
    void shouldNotDeployFileWithoutExtension() {
        List<Resource> testFiles = List.of(test);
        String sampleFileName = "aFileName";
        Assertions.assertThrows(NoFileExtensionFoundException.class,
            () -> AuthorizationContext.runWithoutAuthorization(() -> {
                camundaProcessService.deploy(
                        sampleFileName,
                        new ByteArrayInputStream(testFiles.stream().filter(testFile -> Objects.equals(testFile.getFilename(), "sampleTestFile"))
                                .findFirst().orElseGet(() -> new ByteArrayResource(new byte[]{})).getInputStream().readAllBytes())
                );
                return null;
                }
            ));
        List<DecisionDefinition> dmnDefinitions = repositoryService.createDecisionDefinitionQuery().list();
        List<CamundaProcessDefinition> bpmnDefinitions = AuthorizationContext
                .runWithoutAuthorization(() -> camundaProcessService.getDeployedDefinitions());
        Assertions.assertFalse(dmnDefinitions.stream().anyMatch(dmnDefinition -> dmnDefinition.getResourceName().equals(sampleFileName)));
        Assertions.assertFalse(bpmnDefinitions.stream().anyMatch(bpmnDefinition -> bpmnDefinition.getResourceName().equals(sampleFileName)));
    }

    @Test
    void shouldNotDeployNewSystemProcess() {
        List<Resource> processes = List.of(bpmn);
        Assertions.assertThrows(ProcessNotDeployableException.class,
            () -> AuthorizationContext.runWithoutAuthorization(() -> {
                camundaProcessService.deploy(
                    "aProcessName.bpmn",
                    new ByteArrayInputStream(processes.stream().filter(process -> Objects.equals(process.getFilename(), "shouldNotDeploy.xml"))
                        .findFirst().orElseGet(() -> new ByteArrayResource(new byte[]{})).getInputStream().readAllBytes()));
                return null;
            }
        ));
        List<CamundaProcessDefinition> definitions = AuthorizationContext
            .runWithoutAuthorization(() -> camundaProcessService.getDeployedDefinitions());
        Assertions.assertFalse(definitions.stream().anyMatch(processDefinition -> processDefinition.getKey().equals("firstProcess")));
        Assertions.assertFalse(definitions.stream().anyMatch(processDefinition -> processDefinition.getKey().equals("secondProcess")));
    }

    @Test
    void shouldNotUpdateExistingSystemProcess() throws IOException {
        List<Resource> processes = List.of(bpmn);
        var systemProcessModel = Bpmn.readModelFromStream(new ByteArrayInputStream(processes.stream().filter(process -> Objects.equals(process.getFilename(), "systemProcess.xml"))
                .findFirst().orElseGet(() -> new ByteArrayResource(new byte[]{})).getInputStream().readAllBytes()));
        repositoryService.createDeployment().addModelInstance("systemProcess.bpmn", systemProcessModel).deploy();
        List<CamundaProcessDefinition> definitions = AuthorizationContext
            .runWithoutAuthorization(() -> camundaProcessService.getDeployedDefinitions());
        Assertions.assertTrue(definitions.stream().anyMatch(processDefinition -> processDefinition.getKey().equals("secondProcess")));

        Assertions.assertThrows(ProcessNotDeployableException.class,
            () -> AuthorizationContext.runWithoutAuthorization(() -> {
                camundaProcessService.deploy(
                    "aProcessName.bpmn",
                    new ByteArrayInputStream(processes.stream().filter(process -> Objects.equals(process.getFilename(), "shouldNotDeploy.xml"))
                        .findFirst().orElseGet(() -> new ByteArrayResource(new byte[]{})).getInputStream().readAllBytes()));
                return null;
            }
        ));
        Assertions.assertFalse(definitions.stream().anyMatch(processDefinition -> processDefinition.getKey().equals("firstProcess")));
        Assertions.assertTrue(definitions.stream().anyMatch(processDefinition -> processDefinition.getKey().equals("secondProcess")));
    }
}
