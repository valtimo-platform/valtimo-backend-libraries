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

package com.ritense.document.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ritense.document.domain.search.SearchConfigurationDto;
import com.ritense.document.exception.SearchConfigurationDeploymentException;
import com.ritense.document.exception.SearchFieldConfigurationDeploymentException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

@Transactional
public class SearchConfigurationDeploymentService {

    private static final String SEARCH_SCHEMA_PATH = "classpath:config/search/schema/search.schema.json";
    private static final String SEARCH_CONFIGURATIONS_PATH = "classpath:config/search/*.json";
    private static final Logger logger = LoggerFactory.getLogger(SearchConfigurationDeploymentService.class);

    private final ResourceLoader resourceLoader;
    private final SearchFieldService searchFieldService;
    private final ObjectMapper objectMapper;

    public SearchConfigurationDeploymentService(ResourceLoader resourceLoader, SearchFieldService searchFieldService, ObjectMapper objectMapper) {
        this.resourceLoader = resourceLoader;
        this.searchFieldService = searchFieldService;
        this.objectMapper = objectMapper;
    }

    @Transactional
    @EventListener(ApplicationReadyEvent.class)
    public void deployAll() {
        logger.info("Deploy all search configurations");
        try {
            for (var resource : loadSearchConfigurationsResources()) {
                deploy(resource);
            }
        } catch (Exception e) {
            throw new SearchConfigurationDeploymentException(e);
        }
    }

    private void deploy(String documentDefinitionName, String searchConfigurationJson) throws IOException {
        validate(searchConfigurationJson);

        var searchConfiguration = objectMapper.readValue(searchConfigurationJson, SearchConfigurationDto.class);

        try {
            searchFieldService.createSearchConfiguration(documentDefinitionName, searchConfiguration.toEntity(documentDefinitionName));
            logger.info("Deployed search configuration for document - {}", documentDefinitionName);
        } catch (Exception e) {
            throw new SearchFieldConfigurationDeploymentException(documentDefinitionName, e);
        }
    }

    private void deploy(Resource searchResource) throws IOException {
        if (searchResource.getFilename() != null) {
            var fileName = Objects.requireNonNull(searchResource.getFilename());
            var documentDefinitionName = fileName.substring(0, fileName.lastIndexOf('.'));
            var searchConfigurationJson = StreamUtils.copyToString(searchResource.getInputStream(), StandardCharsets.UTF_8);
            deploy(documentDefinitionName, searchConfigurationJson);
        }
    }

    private void validate(String searchJson) throws IOException {
        var configurationJsonObject = new JSONObject(new JSONTokener(searchJson));

        var schema = SchemaLoader.load(new JSONObject(new JSONTokener(loadSearchSchemaResource().getInputStream())));
        schema.validate(configurationJsonObject);
    }

    private Resource loadSearchSchemaResource() {
        return ResourcePatternUtils.getResourcePatternResolver(resourceLoader).getResource(SEARCH_SCHEMA_PATH);
    }

    private Resource[] loadSearchConfigurationsResources() throws IOException {
        return ResourcePatternUtils.getResourcePatternResolver(resourceLoader).getResources(SEARCH_CONFIGURATIONS_PATH);
    }
}
