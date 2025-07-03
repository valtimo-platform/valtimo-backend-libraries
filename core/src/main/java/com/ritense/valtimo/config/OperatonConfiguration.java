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

package com.ritense.valtimo.config;

import com.ritense.valtimo.operaton.ProcessDefinitionDeployedEventPublisher;
import com.ritense.valtimo.operaton.command.ValtimoSchemaOperationsCommand;
import com.ritense.valtimo.operaton.domain.OperatonVariableInstance;
import com.ritense.valtimo.operaton.repository.CustomRepositoryServiceImpl;
import com.ritense.valtimo.validator.MaxDateValidator;
import com.ritense.valtimo.validator.MinDateValidator;
import java.util.ArrayList;
import java.util.HashMap;
import org.operaton.bpm.engine.RepositoryService;
import org.operaton.bpm.engine.impl.RepositoryServiceImpl;
import org.operaton.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.operaton.bpm.engine.impl.form.validator.FormFieldValidator;
import org.operaton.bpm.engine.impl.persistence.StrongUuidGenerator;
import org.operaton.bpm.engine.variable.Variables;
import org.operaton.bpm.spring.boot.starter.configuration.OperatonProcessEngineConfiguration;

public class OperatonConfiguration implements OperatonProcessEngineConfiguration {

    private final ValtimoSchemaOperationsCommand schemaOperationsCommand;
    private final RepositoryService repositoryService;
    private final ProcessDefinitionDeployedEventPublisher processDefinitionDeployedEventPublisher;

    public OperatonConfiguration(
        final ValtimoSchemaOperationsCommand valtimoSchemaOperationsCommand,
        final CustomRepositoryServiceImpl repositoryService,
        final ProcessDefinitionDeployedEventPublisher processDefinitionDeployedEventPublisher
    ) {
        this.schemaOperationsCommand = valtimoSchemaOperationsCommand;
        this.repositoryService = repositoryService;
        this.processDefinitionDeployedEventPublisher = processDefinitionDeployedEventPublisher;
    }

    @Override
    public void preInit(ProcessEngineConfigurationImpl processEngineConfiguration) {
        processEngineConfiguration.setIdGenerator(new StrongUuidGenerator());
        processEngineConfiguration.setDefaultSerializationFormat(Variables.SerializationDataFormats.JSON.getName());

        HashMap<String, Class<? extends FormFieldValidator>> customValidators = new HashMap<>();
        customValidators.put("minDate", MinDateValidator.class);
        customValidators.put("maxDate", MaxDateValidator.class);
        processEngineConfiguration.setCustomFormFieldValidators(customValidators);

        if (processEngineConfiguration.getCustomPostDeployers() == null) {
            processEngineConfiguration.setCustomPostDeployers(new ArrayList<>());
        }
        processEngineConfiguration.getCustomPostDeployers().add(processDefinitionDeployedEventPublisher);

        //Override default
        processEngineConfiguration.setSchemaOperationsCommand(schemaOperationsCommand);
    }

    @Override
    public void postInit(ProcessEngineConfigurationImpl processEngineConfiguration) {
        RepositoryServiceImpl serviceImpl = (RepositoryServiceImpl) repositoryService;
        serviceImpl.setCommandExecutor(processEngineConfiguration.getCommandExecutorTxRequired());
        serviceImpl.setDeploymentCharset(processEngineConfiguration.getDefaultCharset());
        processEngineConfiguration.setRepositoryService(serviceImpl);
        processEngineConfiguration.getVariableSerializers().getSerializers().forEach(serializer ->
            OperatonVariableInstance.Companion.getVariableSerializers().addSerializer(serializer)
        );
    }

}
