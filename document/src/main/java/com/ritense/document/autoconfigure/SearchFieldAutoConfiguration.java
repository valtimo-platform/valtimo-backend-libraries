/*
 * Copyright 2015-2023 Ritense BV, the Netherlands.
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

package com.ritense.document.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ritense.authorization.AuthorizationService;
import com.ritense.document.repository.DocumentRepository;
import com.ritense.document.repository.SearchFieldRepository;
import com.ritense.document.service.DocumentDefinitionService;
import com.ritense.document.service.DocumentSearchService;
import com.ritense.document.service.DocumentStatisticService;
import com.ritense.document.service.SearchConfigurationDeploymentService;
import com.ritense.document.service.SearchFieldService;
import com.ritense.document.web.rest.impl.SearchFieldResource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;

@Configuration
public class SearchFieldAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(SearchFieldResource.class)
    public SearchFieldResource searchFieldResource(
        SearchFieldService searchFieldService
    ) {
        return new SearchFieldResource(searchFieldService);
    }

    @Bean
    @ConditionalOnMissingBean(SearchFieldService.class)
    public SearchFieldService searchFieldService(
        SearchFieldRepository searchFieldRepository,
        DocumentDefinitionService documentDefinitionService,
        AuthorizationService authorizationService
    ) {
        return new SearchFieldService(searchFieldRepository, documentDefinitionService, authorizationService);
    }

    @Bean
    @ConditionalOnMissingBean(SearchConfigurationDeploymentService.class)
    public SearchConfigurationDeploymentService searchConfigurationDeploymentService(
        ResourceLoader resourceLoader,
        SearchFieldService searchFieldService,
        ObjectMapper objectMapper
    ) {
        return new SearchConfigurationDeploymentService(resourceLoader, searchFieldService, objectMapper);
    }

    @Bean
    @ConditionalOnMissingBean(DocumentStatisticService.class)
    public DocumentStatisticService documentStatisticService(
        DocumentDefinitionService documentDefinitionService,
        DocumentSearchService documentSearchService
    ) {
        return new DocumentStatisticService(documentDefinitionService, documentSearchService);
    }
}
