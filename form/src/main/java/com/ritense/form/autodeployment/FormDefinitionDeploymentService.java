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

package com.ritense.form.autodeployment;

import com.ritense.form.domain.FormDefinition;
import com.ritense.form.domain.FormIoFormDefinition;
import com.ritense.form.domain.event.FormsAutoDeploymentFinishedEvent;
import com.ritense.form.domain.request.CreateFormDefinitionRequest;
import com.ritense.form.repository.FormDefinitionRepository;
import com.ritense.form.service.FormDefinitionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class FormDefinitionDeploymentService {

    public static final String PATH = "classpath*:config/form/*.json";
    private final ResourceLoader resourceLoader;
    private final FormDefinitionService formDefinitionService;
    private final FormDefinitionRepository formDefinitionRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    void deployAllFromResourceFiles() {
        logger.info("Deploying all forms from {}", PATH);
        ArrayList<FormDefinition> formDefinitions = new ArrayList<>();
        try {
            final Resource[] resources = loadResources();
            for (Resource resource : resources) {
                if (resource.getFilename() == null) {
                    continue;
                }

                String name = getFormName(resource);
                Optional<FormIoFormDefinition> savedForm = formDefinitionRepository.findByName(name);

                if (savedForm.isPresent()) {
                    continue;
                }
                final String definition = IOUtils.toString(resource.getInputStream());
                FormDefinition formDefinition = formDefinitionService.createFormDefinition(
                    new CreateFormDefinitionRequest(
                        name,
                        definition
                    )
                );
                formDefinitions.add(formDefinition);
                logger.info("Deployed form {}", name);
            }
            applicationEventPublisher.publishEvent(new FormsAutoDeploymentFinishedEvent(formDefinitions));
        } catch (IOException e) {
            logger.error("Error deploying document schema's", e);
        }
    }

    private String getFormName(Resource resource) {
        String formName = resource.getFilename();
        if (formName != null && formName.endsWith(".json")) {
            formName = formName.substring(0, formName.length() - 5);
        }
        return formName;
    }

    private Resource[] loadResources() throws IOException {
        return ResourcePatternUtils.getResourcePatternResolver(resourceLoader).getResources(PATH);
    }

}