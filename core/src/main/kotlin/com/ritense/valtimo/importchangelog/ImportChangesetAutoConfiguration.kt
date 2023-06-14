/*
 * Copyright 2015-2023 Ritense BV, the Netherlands.
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

package com.ritense.valtimo.importchangelog

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.ritense.valtimo.contract.importchangelog.ChangesetDeployer
import com.ritense.valtimo.importchangelog.ImportChangelogDeploymentService
import com.ritense.valtimo.importchangelog.repository.ImportChangesetRepository
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ResourceLoader
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@EnableJpaRepositories(basePackageClasses = [ImportChangesetRepository::class])
@EntityScan("com.ritense.valtimo.importchangelog.domain")
@Configuration
class ImportChangesetAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(ImportChangelogDeploymentService::class)
    fun importChangelogDeploymentService(
        resourceLoader: ResourceLoader,
        importChangesetRepository: ImportChangesetRepository,
        objectMapper: ObjectMapper,
        changesetDeployers: List<ChangesetDeployer>
    ): ImportChangelogDeploymentService {
        return ImportChangelogDeploymentService(
            resourceLoader,
            importChangesetRepository,
            objectMapper,
            changesetDeployers
        )
    }

    @Bean
    @ConditionalOnMissingBean(ObjectMapper::class)
    fun objectMapper(): ObjectMapper {
        return jacksonObjectMapper()
    }

}