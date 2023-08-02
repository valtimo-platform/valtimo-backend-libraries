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

package com.ritense.note.autoconfigure

import com.ritense.authorization.AuthorizationService
import com.ritense.document.domain.impl.JsonSchemaDocument
import com.ritense.document.service.DocumentService
import com.ritense.note.repository.NoteDocumentMapper
import com.ritense.note.repository.NoteRepository
import com.ritense.note.repository.NoteSpecificationFactory
import com.ritense.note.security.config.NoteHttpSecurityConfigurer
import com.ritense.note.service.NoteService
import com.ritense.note.web.rest.NoteResource
import com.ritense.valtimo.contract.authentication.UserManagementService
import com.ritense.valtimo.contract.database.QueryDialectHelper
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Lazy
import org.springframework.core.annotation.Order
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@Configuration
@EnableJpaRepositories(basePackages = ["com.ritense.note.repository"])
@EntityScan("com.ritense.note.domain")
class NoteAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(NoteService::class)
    fun noteService(
        noteRepository: NoteRepository,
        userManagementService: UserManagementService,
        applicationEventPublisher: ApplicationEventPublisher,
        authorizationService: AuthorizationService
    ): NoteService {
        return NoteService(
            noteRepository,
            userManagementService,
            applicationEventPublisher,
            authorizationService
        )
    }

    @Bean
    @ConditionalOnMissingBean(NoteResource::class)
    fun noteResource(
        noteService: NoteService
    ): NoteResource {
        return NoteResource(
            noteService
        )
    }

    @Order(301)
    @Bean
    @ConditionalOnMissingBean(NoteHttpSecurityConfigurer::class)
    fun noteHttpSecurityConfigurer(): NoteHttpSecurityConfigurer {
        return NoteHttpSecurityConfigurer()
    }

    @Bean
    fun noteDocumentMapper(
        @Lazy documentService: DocumentService<JsonSchemaDocument>,
    ): NoteDocumentMapper {
        return NoteDocumentMapper(documentService)
    }

    @Bean
    fun noteSpecificationFactory(
        @Lazy noteService: NoteService,
        queryDialectHelper: QueryDialectHelper
    ): NoteSpecificationFactory {
        return NoteSpecificationFactory(noteService, queryDialectHelper)
    }
}
