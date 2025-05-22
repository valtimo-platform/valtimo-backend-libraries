/*
 * Copyright 2015-2024 Ritense BV, the Netherlands.
 *
 *  Licensed under EUPL, Version 1.2 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" basis,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.ritense.processlink.url.autoconfigure

import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.authorization.ValtimoAuthorizationService
import com.ritense.document.service.impl.JsonSchemaDocumentService
import com.ritense.processdocument.service.ProcessDocumentAssociationService
import com.ritense.processdocument.service.ProcessDocumentService
import com.ritense.processlink.service.ProcessLinkService
import com.ritense.processlink.url.configuration.URLProcessLinkSecurityConfigurer
import com.ritense.processlink.url.domain.URLVariables
import com.ritense.processlink.url.mapper.URLProcessLinkMapper
import com.ritense.processlink.url.service.URLProcessLinkActivityHandler
import com.ritense.processlink.url.service.URLProcessLinkService
import com.ritense.processlink.url.service.URLSupportedProcessLinksHandler
import com.ritense.processlink.url.web.rest.URLProcessLinkResource
import com.ritense.valtimo.camunda.service.CamundaRepositoryService
import com.ritense.valtimo.contract.config.LiquibaseMasterChangeLogLocation
import com.ritense.valtimo.service.CamundaTaskService
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order

@Configuration
@EntityScan("com.ritense.processlink.url.domain")
class ProcessLinkUrlAutoConfiguration {

    @Order(org.springframework.core.Ordered.HIGHEST_PRECEDENCE + 10)
    @Bean
    @ConditionalOnMissingBean(name = ["urlProcessLinkLiquibaseMasterChangeLogLocation"])
    fun urlProcessLinkLiquibaseMasterChangeLogLocation(): LiquibaseMasterChangeLogLocation {
        return LiquibaseMasterChangeLogLocation("config/liquibase/process-link-url-master.xml")
    }

    @Bean
    @ConditionalOnMissingBean(URLProcessLinkMapper::class)
    fun urlProcessLinkMapper(
        objectMapper: ObjectMapper
    ) = URLProcessLinkMapper(
        objectMapper
    )

    @Bean
    @Order(40)
    @ConditionalOnMissingBean(URLSupportedProcessLinksHandler::class)
    fun urlSupportedProcessLinksHandler() = URLSupportedProcessLinksHandler()

    @Bean
    @ConditionalOnMissingBean(URLProcessLinkActivityHandler::class)
    fun urlProcessLinkActivityHandler() = URLProcessLinkActivityHandler()

    @Bean
    @ConditionalOnMissingBean(URLProcessLinkResource::class)
    fun urlProcessLinkResource(
        urlProcessLinkService: URLProcessLinkService
    ) = URLProcessLinkResource(
        urlProcessLinkService
    )

    @Bean
    @ConditionalOnMissingBean(URLProcessLinkService::class)
    fun urlProcessLinkService(
        processLinkService: ProcessLinkService,
        documentService: JsonSchemaDocumentService,
        processDocumentAssociationService: ProcessDocumentAssociationService,
        processDocumentService: ProcessDocumentService,
        repositoryService: CamundaRepositoryService,
        applicationEventPublisher: ApplicationEventPublisher,
        objectMapper: ObjectMapper,
        URLVariables: URLVariables,
        camundaTaskService: CamundaTaskService,
        authorizationService: ValtimoAuthorizationService
    ) = URLProcessLinkService(
        processLinkService,
        documentService,
        processDocumentAssociationService,
        processDocumentService,
        repositoryService,
        objectMapper,
        URLVariables,
        camundaTaskService,
        authorizationService
    )

    @Bean
    @ConditionalOnMissingBean(URLProcessLinkSecurityConfigurer::class)
    @Order(430)
    fun urlProcessLinkSecurityConfigurer() = URLProcessLinkSecurityConfigurer()

}