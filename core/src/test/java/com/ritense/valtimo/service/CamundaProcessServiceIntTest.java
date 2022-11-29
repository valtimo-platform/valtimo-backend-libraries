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

package com.ritense.valtimo.service;

import com.ritense.valtimo.BaseIntegrationTest;
import com.ritense.valtimo.exception.ProcessNotUpdatableException;
import org.camunda.bpm.engine.RepositoryService;
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

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private CamundaProcessService camundaProcessService;

    @Test
    void shouldDeployNewProcess() throws IOException, ProcessNotUpdatableException {
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
    void shouldNotDeployNewSystemProcess() {
        List<Resource> processes = List.of(bpmn);
        Assertions.assertThrows(ProcessNotUpdatableException.class, () -> camundaProcessService.deploy(
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

        Assertions.assertThrows(ProcessNotUpdatableException.class, () -> camundaProcessService.deploy(
                "aProcessName.bpmn",
                new ByteArrayInputStream(processes.stream().filter(process -> Objects.equals(process.getFilename(), "shouldNotDeploy.xml"))
                        .findFirst().orElseGet(() -> new ByteArrayResource(new byte[]{})).getInputStream().readAllBytes())
        ));
        Assertions.assertFalse(definitions.stream().anyMatch(processDefinition -> processDefinition.getKey().equals("firstProcess")));
        Assertions.assertTrue(definitions.stream().anyMatch(processDefinition -> processDefinition.getKey().equals("secondProcess")));

    }
}
