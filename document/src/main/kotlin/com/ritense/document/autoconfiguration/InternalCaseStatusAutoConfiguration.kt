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

package com.ritense.document.autoconfiguration

import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.authorization.AuthorizationService
import com.ritense.document.deployment.InternalCaseStatusDeployer
import com.ritense.document.exporter.InternalCaseStatusExporter
import com.ritense.document.importer.InternalCaseStatusImporter
import com.ritense.document.repository.InternalCaseStatusRepository
import com.ritense.document.security.InternalCaseHttpSecurityConfigurer
import com.ritense.document.service.DocumentDefinitionService
import com.ritense.document.service.InternalCaseStatusService
import com.ritense.document.web.rest.InternalCaseStatusResource
import com.ritense.valtimo.changelog.service.ChangelogDeployer
import com.ritense.valtimo.changelog.service.ChangelogService
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.core.annotation.Order

@AutoConfiguration
class InternalCaseStatusAutoConfiguration {
    @Order(294)
    @Bean
    @ConditionalOnMissingBean(InternalCaseHttpSecurityConfigurer::class)
    fun internalCaseHttpSecurityConfigurer(): InternalCaseHttpSecurityConfigurer {
        return InternalCaseHttpSecurityConfigurer()
    }

    @Bean
    @ConditionalOnMissingBean(InternalCaseStatusResource::class)
    fun internalCaseStatusResource(
        internalCaseStatusService: InternalCaseStatusService,
    ): InternalCaseStatusResource {
        return InternalCaseStatusResource(internalCaseStatusService)
    }

    @Bean
    @ConditionalOnMissingBean(InternalCaseStatusService::class)
    fun internalCaseStatusService(
        repository: InternalCaseStatusRepository,
        documentDefinitionService: DocumentDefinitionService,
        authorizationService: AuthorizationService,
    ): InternalCaseStatusService {
        return InternalCaseStatusService(repository, documentDefinitionService, authorizationService)
    }

    @Bean
    @ConditionalOnMissingBean(InternalCaseStatusDeployer::class)
    fun internalCaseStatusDeployer(
        repository: InternalCaseStatusRepository,
        service: InternalCaseStatusService,
        objectMapper: ObjectMapper,
        changelogService: ChangelogService,
        @Value("\${valtimo.changelog.internal-case-status.clear-tables:false}") clearTables: Boolean
    ): InternalCaseStatusDeployer {
        return InternalCaseStatusDeployer(
            repository,
            service,
            objectMapper,
            changelogService,
            clearTables
        )
    }

    @Bean
    @ConditionalOnMissingBean(InternalCaseStatusExporter::class)
    fun internalCaseStatusExporter(
        objectMapper: ObjectMapper,
        service: InternalCaseStatusService,
    ): InternalCaseStatusExporter {
        return InternalCaseStatusExporter(objectMapper, service)
    }

    @Bean
    @ConditionalOnMissingBean(InternalCaseStatusImporter::class)
    fun internalCaseStatusImporter(
        internalCaseStatusDeployer: InternalCaseStatusDeployer,
        changelogDeployer: ChangelogDeployer,
    ): InternalCaseStatusImporter {
        return InternalCaseStatusImporter(internalCaseStatusDeployer, changelogDeployer)
    }
}