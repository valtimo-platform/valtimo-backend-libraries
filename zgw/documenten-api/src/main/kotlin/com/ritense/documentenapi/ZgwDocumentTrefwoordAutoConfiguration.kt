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

package com.ritense.documentenapi

import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.documentenapi.deployment.ZgwDocumentTrefwoordDeploymentService
import com.ritense.documentenapi.repository.ZgwDocumentTrefwoordRepository
import com.ritense.documentenapi.service.ZgwDocumentTrefwoordExporter
import com.ritense.documentenapi.service.ZgwDocumentTrefwoordImporter
import com.ritense.documentenapi.service.ZgwDocumentTrefwoordService
import com.ritense.documentenapi.web.rest.ZgwDocumentTrefwoordResource
import com.ritense.valtimo.changelog.service.ChangelogDeployer
import com.ritense.valtimo.changelog.service.ChangelogService
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Bean

@AutoConfiguration
@EntityScan(basePackages = ["com.ritense.documentenapi.domain"])
class ZgwDocumentTrefwoordAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(ZgwDocumentTrefwoordDeploymentService::class)
    fun zgwDocumentTrefwoordDeploymentService(
        objectMapper: ObjectMapper,
        zgwDocumentTrefwoordRepository: ZgwDocumentTrefwoordRepository,
        zgwDocumentTrefwoordService: ZgwDocumentTrefwoordService,
        changelogService: ChangelogService,
        @Value("\${valtimo.changelog.zgw-document-trefwoord.clear-tables:false}") clearTables: Boolean
    ) = ZgwDocumentTrefwoordDeploymentService(
        objectMapper,
        zgwDocumentTrefwoordRepository,
        zgwDocumentTrefwoordService,
        changelogService,
        clearTables
    )

    @Bean
    @ConditionalOnMissingBean(ZgwDocumentTrefwoordService::class)
    fun zgwDocumentTrefwoordService(
        zgwDocumentTrefwoordRepository: ZgwDocumentTrefwoordRepository
    ) = ZgwDocumentTrefwoordService(
        zgwDocumentTrefwoordRepository
    )

    @Bean
    @ConditionalOnMissingBean(ZgwDocumentTrefwoordResource::class)
    fun zgwDocumentTrefwoordResource(
        zgwDocumentTrefwoordService: ZgwDocumentTrefwoordService
    ) = ZgwDocumentTrefwoordResource(
        zgwDocumentTrefwoordService
    )

    @Bean
    @ConditionalOnMissingBean(ZgwDocumentTrefwoordExporter::class)
    fun zgwDocumentTrefwoordExporter(
        objectMapper: ObjectMapper,
        zgwDocumentTrefwoordService: ZgwDocumentTrefwoordService,
    ) = ZgwDocumentTrefwoordExporter(
        objectMapper,
        zgwDocumentTrefwoordService
    )

    @Bean
    @ConditionalOnMissingBean(ZgwDocumentTrefwoordImporter::class)
    fun zgwDocumentTrefwoordImporter(
        zgwDocumentTrefwoordDeploymentService: ZgwDocumentTrefwoordDeploymentService,
        changelogDeployer: ChangelogDeployer,
    ) = ZgwDocumentTrefwoordImporter(
        zgwDocumentTrefwoordDeploymentService,
        changelogDeployer
    )

}
