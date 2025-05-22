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
import com.ritense.document.deployment.CaseTagDeployer
import com.ritense.document.exporter.CaseTagExporter
import com.ritense.document.importer.CaseTagImporter
import com.ritense.document.repository.CaseTagRepository
import com.ritense.document.security.CaseTagHttpSecurityConfigurer
import com.ritense.document.service.CaseTagService
import com.ritense.document.service.DocumentDefinitionService
import com.ritense.document.web.rest.CaseTagResource
import com.ritense.valtimo.changelog.service.ChangelogDeployer
import com.ritense.valtimo.changelog.service.ChangelogService
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.core.annotation.Order


@AutoConfiguration
class CaseTagAutoConfiguration {

    @Order(295)
    @Bean
    @ConditionalOnMissingBean(CaseTagHttpSecurityConfigurer::class)
    fun caseTagHttpSecurityConfigurer(): CaseTagHttpSecurityConfigurer {
        return CaseTagHttpSecurityConfigurer()
    }

    @Bean
    @ConditionalOnMissingBean(CaseTagResource::class)
    fun caseTagResource(
        caseTagService: CaseTagService,
    ): CaseTagResource {
        return CaseTagResource(caseTagService)
    }

    @Bean
    @ConditionalOnMissingBean(CaseTagService::class)
    fun caseTagService(
        repository: CaseTagRepository,
        documentDefinitionService: DocumentDefinitionService,
        authorizationService: AuthorizationService,
    ): CaseTagService {
        return CaseTagService(repository, documentDefinitionService, authorizationService)
    }

    @Bean
    @ConditionalOnMissingBean(CaseTagDeployer::class)
    fun caseTagDeployer(
        repository: CaseTagRepository,
        service: CaseTagService,
        objectMapper: ObjectMapper,
        changelogService: ChangelogService,
        @Value("\${valtimo.changelog.case-tag.clear-tables:false}") clearTables: Boolean
    ): CaseTagDeployer {
        return CaseTagDeployer(
            repository,
            service,
            objectMapper,
            changelogService,
            clearTables
        )
    }

    @Bean
    @ConditionalOnMissingBean(CaseTagExporter::class)
    fun caseTagExporter(
        objectMapper: ObjectMapper,
        service: CaseTagService,
    ): CaseTagExporter {
        return CaseTagExporter(objectMapper, service)
    }

    @Bean
    @ConditionalOnMissingBean(CaseTagImporter::class)
    fun caseTagImporter(
        caseTagDeployer: CaseTagDeployer,
        changelogDeployer: ChangelogDeployer,
    ): CaseTagImporter {
        return CaseTagImporter(caseTagDeployer, changelogDeployer)
    }

}