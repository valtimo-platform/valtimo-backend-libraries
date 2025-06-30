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
import com.ritense.valtimo.operaton.domain.OperatonProcessDefinition;
import com.ritense.valtimo.contract.case_.CaseDefinitionId;
import com.ritense.valtimo.exception.FileExtensionNotSupportedException;
import com.ritense.valtimo.exception.NoFileExtensionFoundException;
import com.ritense.valtimo.exception.ProcessNotDeployableException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.operaton.bpm.engine.RepositoryService;
import org.operaton.bpm.engine.repository.DecisionDefinition;
import org.operaton.bpm.engine.repository.ProcessDefinition;
import org.operaton.bpm.model.bpmn.Bpmn;
import org.operaton.bpm.model.bpmn.BpmnModelInstance;
import org.operaton.bpm.model.bpmn.instance.Process;
import org.operaton.bpm.model.bpmn.instance.ServiceTask;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class OperatonProcessServiceIntTest extends BaseIntegrationTest {

    @Value("classpath:examples/bpmn/*.xml")
    Resource[] bpmn;
    @Value("classpath:examples/dmn/*.xml")
    Resource[] dmn;
    @Value("classpath:examples/test/*")
    Resource[] test;

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private OperatonProcessService operatonProcessService;

    @Test
    void shouldDeployNewProcess() throws IOException {
        var latestDeploymentId = findLatestProcessDefinitionDeployedProcess("deployedProcess")
            .map(OperatonProcessDefinition::getDeploymentId);
        List<Resource> processes = List.of(bpmn);
        AuthorizationContext.runWithoutAuthorization(() -> {
            operatonProcessService.deploy(
                CaseDefinitionId.of("deployedProcess", "1.0.0"),
                "aProcessName.bpmn",
                getFileStream("shouldDeploy.xml", processes)
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
            ServiceTask serviceTask = model.getModelElementsByType(ServiceTask.class).stream()
                .findFirst()
                .orElseThrow();
            assertThat(serviceTask.getOperatonExpression()).isEqualTo("${null}");
        }
    }

    @NotNull
    private Optional<OperatonProcessDefinition> findLatestProcessDefinitionDeployedProcess(String processName) {
        return AuthorizationContext
            .runWithoutAuthorization(() -> operatonProcessService.getDeployedDefinitions())
            .stream()
            .filter(processDefinition -> processDefinition.getKey().equals(processName))
            .findFirst();
    }

    @Test
    void shouldDeployNewDmn() {
        List<Resource> tables = List.of(dmn);
        AuthorizationContext.runWithoutAuthorization(() -> {
            operatonProcessService.deploy(
                CaseDefinitionId.of("deployedProcess", "1.0.0"),
                "aDmnName.dmn",
                getFileStream("sampleDecisionTable.xml", tables)
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
                operatonProcessService.deploy(
                    CaseDefinitionId.of("deployedProcess", "1.0.0"),
                    textFileName,
                    getFileStream("sampleTextFile.txt", testFiles)
                );
                return null;
            }
        ));
        List<DecisionDefinition> dmnDefinitions = repositoryService.createDecisionDefinitionQuery().list();
        List<OperatonProcessDefinition> bpmnDefinitions = AuthorizationContext
                .runWithoutAuthorization(() -> operatonProcessService.getDeployedDefinitions());
        Assertions.assertFalse(dmnDefinitions.stream().anyMatch(dmnDefinition -> dmnDefinition.getResourceName().equals(textFileName)));
        Assertions.assertFalse(bpmnDefinitions.stream().anyMatch(bpmnDefinition -> bpmnDefinition.getResourceName().equals(textFileName)));
    }

    @Test
    void shouldNotDeployFileWithoutExtension() {
        List<Resource> testFiles = List.of(test);
        String sampleFileName = "aFileName";
        Assertions.assertThrows(NoFileExtensionFoundException.class,
            () -> AuthorizationContext.runWithoutAuthorization(() -> {
                operatonProcessService.deploy(
                    CaseDefinitionId.of("deployedProcess", "1.0.0"),
                    sampleFileName,
                    getFileStream("sampleTextFile", testFiles)
                );
                return null;
                }
            ));
        List<DecisionDefinition> dmnDefinitions = repositoryService.createDecisionDefinitionQuery().list();
        List<OperatonProcessDefinition> bpmnDefinitions = AuthorizationContext
                .runWithoutAuthorization(() -> operatonProcessService.getDeployedDefinitions());
        Assertions.assertFalse(dmnDefinitions.stream().anyMatch(dmnDefinition -> dmnDefinition.getResourceName().equals(sampleFileName)));
        Assertions.assertFalse(bpmnDefinitions.stream().anyMatch(bpmnDefinition -> bpmnDefinition.getResourceName().equals(sampleFileName)));
    }

    @Test
    void shouldNotUpdateExistingSystemProcess() throws IOException {
        List<Resource> processes = List.of(bpmn);
        var stream = getFileStream("systemProcess.xml", processes);
        var systemProcessModel = Bpmn.readModelFromStream(stream);
        repositoryService.createDeployment().addModelInstance("systemProcess.bpmn", systemProcessModel).deploy();
        List<OperatonProcessDefinition> definitions = AuthorizationContext
            .runWithoutAuthorization(() -> operatonProcessService.getDeployedDefinitions());
        Assertions.assertTrue(definitions.stream().anyMatch(processDefinition -> processDefinition.getKey().equals("secondProcess")));

        Assertions.assertThrows(ProcessNotDeployableException.class,
            () -> AuthorizationContext.runWithoutAuthorization(() -> {
                operatonProcessService.deploy(
                    CaseDefinitionId.of("deployedProcess", "1.0.0"),
                    "aProcessName.bpmn",
                    new ByteArrayInputStream(processes.stream().filter(process -> Objects.equals(process.getFilename(), "shouldNotDeploy.xml"))
                        .findFirst().orElseGet(() -> new ByteArrayResource(new byte[]{})).getInputStream().readAllBytes()));
                return null;
            }
        ));
        Assertions.assertFalse(definitions.stream().anyMatch(processDefinition -> processDefinition.getKey().equals("firstProcess")));
        Assertions.assertTrue(definitions.stream().anyMatch(processDefinition -> processDefinition.getKey().equals("secondProcess")));
    }

    @Test
    void shouldDeploySameFileForDifferentCasedefinitions() throws IOException {
        List<Resource> processes = List.of(bpmn);
        AuthorizationContext.runWithoutAuthorization(() -> {
            operatonProcessService.deploy(
                CaseDefinitionId.of("some-case-definition-1", "1.0.0"),
                "aProcessName.bpmn",
                getFileStream("shouldDeploy.xml", processes)
            );
            return null;
        });
        AuthorizationContext.runWithoutAuthorization(() -> {
            operatonProcessService.deploy(
                CaseDefinitionId.of("some-case-definition-2", "1.0.0"),
                "aProcessName.bpmn",
                getFileStream("shouldDeploy.xml", processes)
            );
            return null;
        });
    }

    @Test
    void whenDeployingSameFileShouldNotDeployAgain() throws IOException {
        List<Resource> processes = List.of(bpmn);
        AuthorizationContext.runWithoutAuthorization(() -> {
            operatonProcessService.deploy(
                CaseDefinitionId.of("some-case-definition-1", "1.0.0"),
                "aProcessName.bpmn",
                getFileStream("uniqueProcess.xml", processes)
            );
            return null;
        });
        AuthorizationContext.runWithoutAuthorization(() -> {
            operatonProcessService.deploy(
                CaseDefinitionId.of("some-case-definition-1", "1.0.0"),
                "aProcessName.bpmn",
                getFileStream("uniqueProcess.xml", processes)
            );
            return null;
        });

        List<ProcessDefinition> processDefinitions = repositoryService
            .createProcessDefinitionQuery()
            .processDefinitionKey("uniqueProcess")
            .list();

        Assertions.assertEquals(1, processDefinitions.size());
    }

    @Test
    void shouldDeploySameFileForCasedefinitionAndUnlinked() throws IOException {
        List<Resource> processes = List.of(bpmn);
        AuthorizationContext.runWithoutAuthorization(() -> {
            operatonProcessService.deploy(
                CaseDefinitionId.of("some-case-definition-1", "1.0.0"),
                "aProcessName.bpmn",
                getFileStream("shouldDeploy.xml", processes)
            );
            return null;
        });
        AuthorizationContext.runWithoutAuthorization(() -> {
            operatonProcessService.deploy(
                null,
                "aProcessName.bpmn",
                getFileStream("shouldDeploy.xml", processes)
            );
            return null;
        });
    }

    @Test
    void shouldDeployFileWithMultipleProcessDefinitions() throws IOException {
        List<Resource> processes = List.of(bpmn);
        AuthorizationContext.runWithoutAuthorization(() -> {
            operatonProcessService.deploy(
                CaseDefinitionId.of("some-case-definition-1", "1.0.0"),
                "aProcessName.bpmn",
                getFileStream("double.xml", processes)
            );
            return null;
        });
    }

    @Test
    void shouldDeployDifferentProcessesWithSameFilenameForSameCasedefinitions() throws IOException {
        List<Resource> processes = List.of(bpmn);
        AuthorizationContext.runWithoutAuthorization(() -> {
            operatonProcessService.deploy(
                CaseDefinitionId.of("some-case-definition-1", "1.0.0"),
                "aProcessName.bpmn",
                getFileStream("shouldDeploy.xml", processes)
            );
            return null;
        });
        AuthorizationContext.runWithoutAuthorization(() -> {
            operatonProcessService.deploy(
                CaseDefinitionId.of("some-case-definition-1", "1.0.0"),
                "aProcessName.bpmn",
                getFileStream("uniqueProcess.xml", processes)
            );
            return null;
        });
    }

    private ByteArrayInputStream getFileStream(String filename, List<Resource> source) throws IOException {
        return new ByteArrayInputStream(source.stream()
            .filter(resource -> Objects.equals(resource.getFilename(), filename))
            .findFirst()
            .orElseGet(() -> new ByteArrayResource(new byte[]{}))
            .getInputStream()
            .readAllBytes());
    }
}
