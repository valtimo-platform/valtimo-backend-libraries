/*
 * Copyright 2015-2022 Ritense BV, the Netherlands.
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

package com.ritense.processdocument.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.ritense.document.domain.event.DocumentDefinitionDeployedEvent;
import com.ritense.document.domain.impl.Mapper;
import com.ritense.processdocument.domain.config.ProcessDocumentLinkConfigItem;
import com.ritense.processdocument.domain.impl.CamundaProcessDefinitionKey;
import com.ritense.processdocument.domain.impl.request.ProcessDocumentDefinitionRequest;
import com.ritense.processdocument.service.ProcessDocumentAssociationService;
import com.ritense.processdocument.service.ProcessDocumentDeploymentService;
import com.ritense.valtimo.domain.contexts.ContextProcess;
import com.ritense.valtimo.service.ContextService;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

public class CamundaProcessJsonSchemaDocumentDeploymentService implements ProcessDocumentDeploymentService {
    private static final Logger logger = LoggerFactory.getLogger(CamundaProcessJsonSchemaDocumentDeploymentService.class);
    private final ResourceLoader resourceLoader;
    private final ProcessDocumentAssociationService processDocumentAssociationService;
    private final ContextService contextService;


    public CamundaProcessJsonSchemaDocumentDeploymentService(ResourceLoader resourceLoader, ProcessDocumentAssociationService processDocumentAssociationService, ContextService contextService) {
        this.resourceLoader = resourceLoader;
        this.processDocumentAssociationService = processDocumentAssociationService;
        this.contextService = contextService;
    }

    @EventListener(DocumentDefinitionDeployedEvent.class)
    public void deployAllProcessDocumentLinks(DocumentDefinitionDeployedEvent documentDefinitionDeployedEvent) {
        final var documentDefinitionName = documentDefinitionDeployedEvent.documentDefinition().id().name();
        final var path = getProcessDocumentLinkResourcePath(documentDefinitionName);
        try {
            final Resource resource = loadResource(path);
            if (resource.exists()) {
                logger.info("Deploying process-document-links from {}", path);
                final var processDocumentLinkConfigItems = getJson(IOUtils.toString(resource.getInputStream(), UTF_8));

                processDocumentLinkConfigItems.forEach(item -> {
                    final var request = new ProcessDocumentDefinitionRequest(
                            item.getProcessDefinitionKey(),
                            documentDefinitionName,
                            item.getCanInitializeDocument(),
                            item.getStartableByUser()
                    );

                    final var existingDefinitionOpt = processDocumentAssociationService.findProcessDocumentDefinition(
                            new CamundaProcessDefinitionKey(item.getProcessDefinitionKey())
                    );

                    if (existingDefinitionOpt.isPresent()) {
                        if (!item.equalsProcessDocumentDefinition(existingDefinitionOpt.get())) {
                            processDocumentAssociationService.deleteProcessDocumentDefinition(request);
                            processDocumentAssociationService.createProcessDocumentDefinition(request);
                        }
                    } else {
                        processDocumentAssociationService.createProcessDocumentDefinition(request);
                    }

                    if (Boolean.TRUE.equals(item.getProcessIsVisibleInMenu())) {
                        contextService.findAll(Pageable.unpaged()).forEach(context -> {
                            context.addProcess(new ContextProcess(item.getProcessDefinitionKey(), true));
                            contextService.save(context);
                        });
                    }
                });
            }
        } catch (IOException e) {
            logger.error("Error while deploying process-document-links", e);
        }
    }

    private String getProcessDocumentLinkResourcePath(String documentDefinitionName) {
        return "classpath:config/process-document-link/" + documentDefinitionName + ".json";
    }

    private List<ProcessDocumentLinkConfigItem> getJson(String rawJson) throws JsonProcessingException {
        TypeReference<List<ProcessDocumentLinkConfigItem>> typeRef = new TypeReference<>() {
        };
        return Mapper.INSTANCE.get().readValue(rawJson, typeRef);
    }

    private Resource loadResource(String locationPattern) throws IOException {
        return ResourcePatternUtils.getResourcePatternResolver(resourceLoader).getResource(locationPattern);
    }

}
