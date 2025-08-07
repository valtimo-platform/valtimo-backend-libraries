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

package com.ritense.form.autodeployment;

import static com.ritense.logging.LoggingContextKt.withLoggingContext;
import static java.nio.charset.StandardCharsets.UTF_8;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ritense.form.domain.FormDefinition;
import com.ritense.form.domain.event.FormsAutoDeploymentFinishedEvent;
import com.ritense.form.domain.request.CreateFormDefinitionRequest;
import com.ritense.form.repository.FormDefinitionRepository;
import com.ritense.form.service.FormDefinitionService;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import com.ritense.logging.LoggableResource;
import com.ritense.valtimo.contract.case_.CaseDefinitionId;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternUtils;

public class FormDefinitionDeploymentService {

    private static final Logger logger = LoggerFactory.getLogger(FormDefinitionDeploymentService.class);
    public static final String PATH = "classpath*:config/form/*.json";
    private final ResourceLoader resourceLoader;
    private final FormDefinitionService formDefinitionService;
    private final FormDefinitionRepository formDefinitionRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final ObjectMapper objectMapper;

    public FormDefinitionDeploymentService(
        ResourceLoader resourceLoader, FormDefinitionService formDefinitionService,
        FormDefinitionRepository formDefinitionRepository, ApplicationEventPublisher applicationEventPublisher,
        ObjectMapper objectMapper
    ) {
        this.resourceLoader = resourceLoader;
        this.formDefinitionService = formDefinitionService;
        this.formDefinitionRepository = formDefinitionRepository;
        this.applicationEventPublisher = applicationEventPublisher;
        this.objectMapper = objectMapper;
    }

    public Optional<FormDefinition> deploy(
        @LoggableResource("formDefinitionName") String name,
        String formDefinitionAsString,
        CaseDefinitionId caseDefinitionId,
        boolean readOnly
    ) throws JsonProcessingException {
        var rawFormDefinition = getJson(formDefinitionAsString);
        var optionalFormDefinition = formDefinitionRepository.findByNameAndCaseDefinitionId(name, caseDefinitionId);
        if (optionalFormDefinition.isPresent()) {
            var existingFormDefinition = optionalFormDefinition.get();
            if (!rawFormDefinition.equals(existingFormDefinition.getFormDefinition())) {
                var formDefinition = formDefinitionService.modifyFormDefinition(
                    existingFormDefinition.getId(),
                    name,
                    rawFormDefinition.toString(),
                    readOnly
                );
                logger.info(
                    "Modified existing form definition {} for case definition {}", name, caseDefinitionId.toString()
                );
                return Optional.of(formDefinition);
            }
        } else {
            var formDefinition = formDefinitionService.createFormDefinition(
                caseDefinitionId,
                new CreateFormDefinitionRequest(
                    name,
                    rawFormDefinition.toString(),
                    readOnly
                )
            );
            logger.info("Deployed form definition {} for case definition {}", name, caseDefinitionId.toString());
            return Optional.of(formDefinition);
        }

        return Optional.empty();
    }

    private JsonNode getJson(String rawJson) throws JsonProcessingException {
        return objectMapper.readTree(rawJson);
    }

}