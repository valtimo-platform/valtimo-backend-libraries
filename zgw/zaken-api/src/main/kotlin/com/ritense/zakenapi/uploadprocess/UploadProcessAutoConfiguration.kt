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

package com.ritense.zakenapi.uploadprocess

import com.ritense.document.service.DocumentService
import com.ritense.processdocument.service.DocumentDefinitionProcessLinkService
import com.ritense.processdocument.service.ProcessDocumentService
import com.ritense.resource.service.TemporaryResourceStorageService
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order

@Configuration
class UploadProcessAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(ResourceSubmittedToDocumentEventListener::class)
    fun resourceSubmittedToDocumentEventListener(
        uploadProcessService: UploadProcessService,
    ): ResourceSubmittedToDocumentEventListener {
        return ResourceSubmittedToDocumentEventListener(
            uploadProcessService,
        )
    }

    @Bean
    @ConditionalOnMissingBean(ResourceUploadedToDocumentEventListener::class)
    fun resourceUploadedEventListener(
        resourceService: TemporaryResourceStorageService,
        uploadProcessService: UploadProcessService,
    ): ResourceUploadedToDocumentEventListener {
        return ResourceUploadedToDocumentEventListener(
            resourceService,
            uploadProcessService,
        )
    }

    @Bean
    @ConditionalOnMissingBean(UploadProcessService::class)
    fun uploadProcessService(
        documentService: DocumentService,
        processDocumentService: ProcessDocumentService,
        documentDefinitionProcessLinkService: DocumentDefinitionProcessLinkService,
    ): UploadProcessService {
        return UploadProcessService(
            documentService,
            processDocumentService,
            documentDefinitionProcessLinkService,
        )
    }

    @Bean
    @ConditionalOnMissingBean(UploadProcessResource::class)
    fun uploadProcessResource(
        documentDefinitionProcessLinkService: DocumentDefinitionProcessLinkService
    ): UploadProcessResource {
        return UploadProcessResource(documentDefinitionProcessLinkService)
    }

    @Order(360)
    @Bean
    @ConditionalOnMissingBean(UploadProcessResourceHttpSecurityConfigurer::class)
    fun uploadProcessResourceHttpSecurityConfigurer(): UploadProcessResourceHttpSecurityConfigurer {
        return UploadProcessResourceHttpSecurityConfigurer()
    }
}
