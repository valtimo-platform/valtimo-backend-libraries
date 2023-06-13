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

package com.ritense.audit.autoconfigure;

import com.ritense.audit.domain.AuditRecord;
import com.ritense.audit.domain.AuditRecordId;
import com.ritense.audit.domain.listener.AuditEventListener;
import com.ritense.audit.domain.listener.impl.AuditEventListenerImpl;
import com.ritense.audit.repository.AuditRecordRepository;
import com.ritense.audit.repository.impl.MySqlAuditRecordRepository;
import com.ritense.audit.repository.impl.PostgresAuditRecordRepository;
import com.ritense.audit.service.AuditEventProcessor;
import com.ritense.audit.service.AuditRetentionService;
import com.ritense.audit.service.AuditSearchService;
import com.ritense.audit.service.AuditService;
import com.ritense.audit.service.impl.AuditEventProcessorImpl;
import com.ritense.audit.service.impl.AuditRetentionServiceImpl;
import com.ritense.audit.service.impl.AuditSearchServiceImpl;
import com.ritense.audit.service.impl.AuditServiceImpl;
import com.ritense.valtimo.contract.database.QueryDialectHelper;
import javax.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactoryBean;

@Configuration
@EnableJpaRepositories(basePackages = "com.ritense.audit.repository.impl")
@EntityScan("com.ritense.audit.domain")
public class AuditAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(AuditEventProcessor.class)
    public AuditEventProcessor auditEventProcessor(AuditRecordRepository<AuditRecord, AuditRecordId> auditRecordRepository) {
        return new AuditEventProcessorImpl(auditRecordRepository);
    }

    @Bean
    @ConditionalOnMissingBean(AuditService.class)
    public AuditService auditService(AuditRecordRepository<AuditRecord, AuditRecordId> auditRecordRepository) {
        return new AuditServiceImpl(auditRecordRepository);
    }

    @Bean
    @ConditionalOnMissingBean(AuditRetentionService.class)
    @ConditionalOnProperty(prefix = "scheduling", name = "enabled", havingValue = "true", matchIfMissing = true)
    public AuditRetentionService auditRetentionService(
        AuditService auditService,
        @Value("${audit.record.retention:15}") long retentionInDays
    ) {
        return new AuditRetentionServiceImpl(auditService, retentionInDays);
    }

    @Bean
    @ConditionalOnMissingBean(AuditSearchService.class)
    public AuditSearchService auditSearchService(
        EntityManager entityManager,
        QueryDialectHelper queryDialectHelper
    ) {
        return new AuditSearchServiceImpl(entityManager, queryDialectHelper);
    }

    @Bean
    @ConditionalOnMissingBean(AuditEventListener.class)
    public AuditEventListener auditEventListener(AuditEventProcessor auditEventProcessor) {
        return new AuditEventListenerImpl(auditEventProcessor);
    }

    @Bean
    @ConditionalOnProperty(prefix = "valtimo", name = "database", havingValue = "postgres")
    public JpaRepositoryFactoryBean<AuditRecordRepository<AuditRecord, AuditRecordId>, AuditRecord, AuditRecordId> postgresAuditRecordRepository() {
        JpaRepositoryFactoryBean factory = new JpaRepositoryFactoryBean(PostgresAuditRecordRepository.class);
        return factory;
    }

    @Bean
    @ConditionalOnProperty(prefix = "valtimo", name = "database", havingValue = "mysql", matchIfMissing = true)
    public JpaRepositoryFactoryBean<AuditRecordRepository<AuditRecord, AuditRecordId>, AuditRecord, AuditRecordId> mysqlAuditRecordRepository() {
        JpaRepositoryFactoryBean factory = new JpaRepositoryFactoryBean(MySqlAuditRecordRepository.class);
        return factory;
    }

}