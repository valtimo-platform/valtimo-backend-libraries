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

import com.ritense.authorization.AuthorizationService;
import com.ritense.document.domain.impl.JsonSchemaDocumentDefinition;
import com.ritense.document.domain.impl.listener.DocumentSnapshotCapturedEventListener;
import com.ritense.document.domain.impl.listener.DocumentSnapshotCapturedEventPublisher;
import com.ritense.document.domain.impl.listener.UndeployDocumentDefinitionEventListener;
import com.ritense.document.domain.impl.snapshot.JsonSchemaDocumentSnapshot;
import com.ritense.document.domain.snapshot.DocumentSnapshot;
import com.ritense.document.repository.DocumentSnapshotRepository;
import com.ritense.document.repository.impl.MysqlJsonSchemaDocumentSnapshotRepository;
import com.ritense.document.repository.impl.PostgresJsonSchemaDocumentSnapshotRepository;
import com.ritense.document.service.DocumentDefinitionService;
import com.ritense.document.service.DocumentSnapshotService;
import com.ritense.document.service.impl.JsonSchemaDocumentDefinitionService;
import com.ritense.document.service.impl.JsonSchemaDocumentService;
import com.ritense.document.service.impl.JsonSchemaDocumentSnapshotService;
import com.ritense.document.web.rest.DocumentSnapshotResource;
import com.ritense.document.web.rest.impl.JsonSchemaDocumentSnapshotResource;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactoryBean;

@Configuration
@ConditionalOnProperty(prefix = "valtimo.versioning", name = "enabled", havingValue = "true", matchIfMissing = true)
@AutoConfigureAfter(DocumentAutoConfiguration.class)
public class DocumentSnapshotAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(DocumentSnapshotService.class)
    public DocumentSnapshotService<JsonSchemaDocumentSnapshot> documentSnapshotService(
        final DocumentSnapshotRepository<JsonSchemaDocumentSnapshot> documentSnapshotRepository,
        final JsonSchemaDocumentService documentService,
        final JsonSchemaDocumentDefinitionService documentDefinitionService,
        final AuthorizationService authorizationService
    ) {
        return new JsonSchemaDocumentSnapshotService(documentSnapshotRepository, documentService, documentDefinitionService, authorizationService);
    }

    @Bean
    @ConditionalOnMissingBean(DocumentSnapshotResource.class)
    public DocumentSnapshotResource documentSnapshotResource(
        final DocumentSnapshotService<JsonSchemaDocumentSnapshot> documentSnapshotService,
        DocumentDefinitionService<JsonSchemaDocumentDefinition> documentDefinitionService
    ) {
        return new JsonSchemaDocumentSnapshotResource(documentSnapshotService, documentDefinitionService);
    }

    @Bean
    @ConditionalOnMissingBean(DocumentSnapshotCapturedEventPublisher.class)
    public DocumentSnapshotCapturedEventPublisher documentSnapshotEventPublisher(
        final ApplicationEventPublisher applicationEventPublisher
    ) {
        return new DocumentSnapshotCapturedEventPublisher(applicationEventPublisher);
    }

    @Bean
    @ConditionalOnMissingBean(DocumentSnapshotCapturedEventListener.class)
    public DocumentSnapshotCapturedEventListener documentSnapshotEventListener(
        final DocumentSnapshotService<JsonSchemaDocumentSnapshot> documentSnapshotService
    ) {
        return new DocumentSnapshotCapturedEventListener(documentSnapshotService);
    }

    @Bean
    @ConditionalOnMissingBean(UndeployDocumentDefinitionEventListener.class)
    public UndeployDocumentDefinitionEventListener undeployDocumentDefinitionListener(
        final DocumentSnapshotService<JsonSchemaDocumentSnapshot> documentSnapshotService
    ) {
        return new UndeployDocumentDefinitionEventListener(documentSnapshotService);
    }

    @Bean
    @ConditionalOnProperty(prefix = "valtimo", name = "database", havingValue = "postgres")
    public JpaRepositoryFactoryBean<
        DocumentSnapshotRepository<JsonSchemaDocumentSnapshot>,
        JsonSchemaDocumentSnapshot,
        DocumentSnapshot.Id
    > postgresJsonSchemaDocumentSnapshotRepository() {
        return new JpaRepositoryFactoryBean<>(PostgresJsonSchemaDocumentSnapshotRepository.class);
    }

    @Bean
    @ConditionalOnProperty(prefix = "valtimo", name = "database", havingValue = "mysql", matchIfMissing = true)
    public JpaRepositoryFactoryBean<
        DocumentSnapshotRepository<JsonSchemaDocumentSnapshot>,
        JsonSchemaDocumentSnapshot,
        DocumentSnapshot.Id
    > mysqlJsonSchemaDocumentSnapshotRepository() {
        return new JpaRepositoryFactoryBean<>(MysqlJsonSchemaDocumentSnapshotRepository.class);
    }
}
