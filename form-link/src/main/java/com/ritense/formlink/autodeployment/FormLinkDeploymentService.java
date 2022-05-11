/*
 * Copyright 2015-2021 Ritense BV, the Netherlands.
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

package com.ritense.formlink.autodeployment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.ritense.form.domain.Mapper;
import com.ritense.form.service.FormDefinitionService;
import com.ritense.formlink.domain.request.FormLinkRequest;
import com.ritense.formlink.service.FormAssociationService;
import java.io.IOException;
import java.util.List;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternUtils;
import static java.nio.charset.StandardCharsets.UTF_8;

public class FormLinkDeploymentService {

    private static final Logger logger = LoggerFactory.getLogger(FormLinkDeploymentService.class);
    public static final String DEFAULT_PATH = "classpath*:config/formlink/*.json";
    private final ResourceLoader resourceLoader;
    private final FormAssociationService formAssociationService;
    private final FormDefinitionService formDefinitionService;

    public FormLinkDeploymentService(ResourceLoader resourceLoader, FormAssociationService formAssociationService, FormDefinitionService formDefinitionService) {
        this.resourceLoader = resourceLoader;
        this.formAssociationService = formAssociationService;
        this.formDefinitionService = formDefinitionService;
    }

    void deployAllFromResourceFiles() {
        logger.info("Deploying all form links from {}", DEFAULT_PATH);
        try {
            final Resource[] resources = loadResources(DEFAULT_PATH);
            for (Resource resource : resources) {
                final var formLinkConfigItems = getJson(IOUtils.toString(resource.getInputStream(), UTF_8));
                final var processDefinitionKeyName = FilenameUtils.removeExtension(resource.getFilename());

                formLinkConfigItems.forEach(formLinkConfigItem -> {
                    final var formDefinition = formDefinitionService.getFormDefinitionByName(
                        formLinkConfigItem.getFormName()
                    ).orElseThrow();
                    final var formLinkRequest = new FormLinkRequest(
                        formLinkConfigItem.getFormLinkElementId(),
                        formLinkConfigItem.getFormAssociationType(),
                        formDefinition.getId(),
                        formLinkConfigItem.getFormFlowName(),
                        null,
                        null
                    );
                    formAssociationService.upsertFormAssociation(
                        processDefinitionKeyName,
                        formLinkRequest
                    );
                });
            }
        } catch (IOException e) {
            logger.error("Error while deploying form-links", e);
        }
    }

    private List<FormLinkConfigItem> getJson(String rawJson) throws JsonProcessingException {
        TypeReference<List<FormLinkConfigItem>> typeRef = new TypeReference<>() {
        };
        return Mapper.INSTANCE.get().readValue(rawJson, typeRef);
    }

    private Resource[] loadResources(String locationPattern) throws IOException {
        return ResourcePatternUtils.getResourcePatternResolver(resourceLoader).getResources(locationPattern);
    }

}