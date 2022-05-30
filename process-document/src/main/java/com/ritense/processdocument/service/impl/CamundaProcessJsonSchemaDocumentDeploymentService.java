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
import com.ritense.document.service.DocumentDefinitionService;
import com.ritense.processdocument.domain.config.ProcessDocumentLinkConfigItem;
import com.ritense.processdocument.domain.impl.CamundaProcessDefinitionKey;
import com.ritense.processdocument.domain.impl.request.ProcessDocumentDefinitionRequest;
import com.ritense.processdocument.service.ProcessDocumentAssociationService;
import com.ritense.processdocument.service.ProcessDocumentDeploymentService;
import com.ritense.valtimo.domain.contexts.ContextProcess;
import com.ritense.valtimo.service.ContextService;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
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
    private final DocumentDefinitionService documentDefinitionService;


    public CamundaProcessJsonSchemaDocumentDeploymentService(ResourceLoader resourceLoader, ProcessDocumentAssociationService processDocumentAssociationService, ContextService contextService, DocumentDefinitionService documentDefinitionService) {
        this.resourceLoader = resourceLoader;
        this.processDocumentAssociationService = processDocumentAssociationService;
        this.contextService = contextService;
        this.documentDefinitionService = documentDefinitionService;
    }

    @EventListener(DocumentDefinitionDeployedEvent.class)
    public void deployNewProcessDocumentLinks(DocumentDefinitionDeployedEvent documentDefinitionDeployedEvent) {
        final var documentDefinitionName = documentDefinitionDeployedEvent.documentDefinition().id().name();
        final var path = getProcessDocumentLinkResourcePath(documentDefinitionName);
        try {
            final Resource resource = loadResource(path);
            if (resource.exists()) {
                final var processDocumentLinkConfigItems = getJson(IOUtils.toString(resource.getInputStream(), UTF_8));

                processDocumentLinkConfigItems.forEach(item -> createProcessDocumentLink(documentDefinitionName, item));
            }
        } catch (IOException e) {
            logger.error("Error while deploying process-document-links", e);
        }
    }

    @EventListener(ApplicationReadyEvent.class)
    public void deployChangedProcessDocumentLinks() throws IOException {
        final var resources = loadResources(getProcessDocumentLinkResourcesPath());
        for (var resource : resources) {
            try {
                final var documentDefinitionName = resource.getFilename().split("\\.")[0];
                final var processDocumentLinkConfigItems = getJson(IOUtils.toString(resource.getInputStream(), UTF_8));

                if (documentDefinitionService.findLatestByName(documentDefinitionName).isPresent()) {
                    processDocumentLinkConfigItems.forEach(item -> createProcessDocumentLink(documentDefinitionName, item));
                }
            } catch (IOException e) {
                logger.error("Error while deploying process-document-link", e);
            }
        }
    }

    private void createProcessDocumentLink(String documentDefinitionName, ProcessDocumentLinkConfigItem item) {
        final var request = new ProcessDocumentDefinitionRequest(
                item.getProcessDefinitionKey(),
                documentDefinitionName,
                item.getCanInitializeDocument(),
                item.getStartableByUser()
        );

        final var existingAssociationOpt = processDocumentAssociationService.findProcessDocumentDefinition(
                new CamundaProcessDefinitionKey(item.getProcessDefinitionKey())
        );

        if (existingAssociationOpt.isPresent()) {
            if (!item.equalsProcessDocumentDefinition(existingAssociationOpt.get())) {
                logger.info("Updating process-document-links from {}.json", documentDefinitionName);
                processDocumentAssociationService.deleteProcessDocumentDefinition(request);
                processDocumentAssociationService.createProcessDocumentDefinition(request);
            }
        } else {
            logger.info("Deploying process-document-links from {}.json", documentDefinitionName);
            processDocumentAssociationService.createProcessDocumentDefinition(request);
        }

        contextService.findAll(Pageable.unpaged()).forEach(context -> {
            context.getProcesses().removeIf(contextProcess -> contextProcess.getProcessDefinitionKey().equals(item.getProcessDefinitionKey()));
            if (Boolean.TRUE.equals(item.getProcessIsVisibleInMenu())) {
                context.addProcess(new ContextProcess(item.getProcessDefinitionKey(), true));
            }
            contextService.save(context);
        });
    }

    private String getProcessDocumentLinkResourcePath(String documentDefinitionName) {
        return "classpath:config/process-document-link/" + documentDefinitionName + ".json";
    }

    private String getProcessDocumentLinkResourcesPath() {
        return "classpath*:config/process-document-link/*.json";
    }

    private List<ProcessDocumentLinkConfigItem> getJson(String rawJson) throws JsonProcessingException {
        TypeReference<List<ProcessDocumentLinkConfigItem>> typeRef = new TypeReference<>() {
        };
        return Mapper.INSTANCE.get().readValue(rawJson, typeRef);
    }

    private Resource loadResource(String locationPattern) throws IOException {
        return ResourcePatternUtils.getResourcePatternResolver(resourceLoader).getResource(locationPattern);
    }

    private Resource[] loadResources(String locationPattern) throws IOException {
        return ResourcePatternUtils.getResourcePatternResolver(resourceLoader).getResources(locationPattern);
    }

}
