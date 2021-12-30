/*
 * Copyright 2015-2020 Ritense BV, the Netherlands.
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

package com.ritense.document.export.autoconfigure

import com.ritense.document.export.repository.PresetRepository
import com.ritense.document.export.service.PresetService
import com.ritense.document.service.DocumentDefinitionService
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@Configuration
@EnableJpaRepositories(basePackages = ["com.ritense.document.export.repository"])
@EntityScan("com.ritense.document.export.domain")
class DocumentExportAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(PresetService::class)
    fun presetService(
        presetRepository: PresetRepository,
        documentDefinitionService: DocumentDefinitionService
    ): PresetService {
        return PresetService(presetRepository, documentDefinitionService)
    }

}
