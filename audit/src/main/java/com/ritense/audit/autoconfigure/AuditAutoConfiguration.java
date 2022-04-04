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

package com.ritense.audit.autoconfigure;

import com.ritense.audit.domain.listener.AuditEventListener;
import com.ritense.audit.domain.listener.impl.AuditEventListenerImpl;
import com.ritense.audit.repository.impl.AuditRecordImplRepository;
import com.ritense.audit.service.AuditEventProcessor;
import com.ritense.audit.service.AuditRetentionService;
import com.ritense.audit.service.AuditSearchService;
import com.ritense.audit.service.AuditService;
import com.ritense.audit.service.impl.AuditEventProcessorImpl;
import com.ritense.audit.service.impl.AuditRetentionServiceImpl;
import com.ritense.audit.service.impl.AuditSearchServiceImpl;
import com.ritense.audit.service.impl.AuditServiceImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import javax.persistence.EntityManager;

@Configuration
@EnableJpaRepositories(basePackages = "com.ritense.audit.repository.impl")
@EntityScan("com.ritense.audit.domain")
public class AuditAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(AuditEventProcessor.class)
    public AuditEventProcessor auditEventProcessor(AuditRecordImplRepository auditRecordRepository) {
        return new AuditEventProcessorImpl(auditRecordRepository);
    }

    @Bean
    @ConditionalOnMissingBean(AuditService.class)
    public AuditService auditService(AuditRecordImplRepository auditRecordRepository) {
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
    public AuditSearchService auditSearchService(EntityManager entityManager) {
        return new AuditSearchServiceImpl(entityManager);
    }

    @Bean
    @ConditionalOnMissingBean(AuditEventListener.class)
    public AuditEventListener auditEventListener(AuditEventProcessor auditEventProcessor) {
        return new AuditEventListenerImpl(auditEventProcessor);
    }

}