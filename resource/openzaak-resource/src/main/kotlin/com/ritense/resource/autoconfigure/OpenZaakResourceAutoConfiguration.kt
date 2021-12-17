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

package com.ritense.resource.autoconfigure

import com.ritense.openzaak.service.DocumentenService
import com.ritense.resource.listener.DocumentRelatedFileSubmittedEventListenerImpl
import com.ritense.resource.repository.OpenZaakResourceRepository
import com.ritense.resource.service.OpenZaakService
import com.ritense.resource.web.rest.OpenZaakResource
import com.ritense.resource.web.rest.OpenZaakUploadResource
import com.ritense.resource.web.rest.ResourceResource
import javax.servlet.http.HttpServletRequest
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@Configuration
@EnableJpaRepositories(basePackages = ["com.ritense.resource.repository"])
@EntityScan("com.ritense.resource.domain")
class OpenZaakResourceAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(OpenZaakService::class)
    fun openZaakService(
        documentenService: DocumentenService,
        openZaakResourceRepository: OpenZaakResourceRepository,
        request: HttpServletRequest
    ): OpenZaakService {
        return OpenZaakService(documentenService, openZaakResourceRepository, request)
    }

    @Bean
    @ConditionalOnMissingBean(ResourceResource::class)
    fun openZaakResource(openZaakService: OpenZaakService): ResourceResource {
        return OpenZaakResource(openZaakService)
    }

    @Bean
    fun openZaakUploadResource(openZaakService: OpenZaakService): OpenZaakUploadResource {
        return OpenZaakUploadResource(openZaakService)
    }

    @Bean
    @ConditionalOnMissingBean(DocumentRelatedFileSubmittedEventListenerImpl::class)
    fun documentRelatedFileSubmittedEventListenerImpl(
        openZaakService: OpenZaakService,
        documentenService: DocumentenService
    ): DocumentRelatedFileSubmittedEventListenerImpl {
        return DocumentRelatedFileSubmittedEventListenerImpl(openZaakService, documentenService)
    }
}