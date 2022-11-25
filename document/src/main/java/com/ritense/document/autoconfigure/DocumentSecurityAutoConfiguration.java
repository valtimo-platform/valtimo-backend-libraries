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

package com.ritense.document.autoconfigure;

import com.ritense.document.security.config.DocumentDefinitionHttpSecurityConfigurer;
import com.ritense.document.security.config.DocumentHttpSecurityConfigurer;
import com.ritense.document.security.config.DocumentSearchHttpSecurityConfigurer;
import com.ritense.document.security.config.DocumentSnapshotHttpSecurityConfigurer;
import com.ritense.document.security.config.DocumentWebSocketHttpSecurityConfigurer;
import com.ritense.document.security.config.WebSocketSecurityConfigurer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Configuration
public class DocumentSecurityAutoConfiguration {

    @Order(290)
    @Bean
    @ConditionalOnMissingBean(DocumentHttpSecurityConfigurer.class)
    public DocumentHttpSecurityConfigurer documentHttpSecurityConfigurer() {
        return new DocumentHttpSecurityConfigurer();
    }

    @Order(291)
    @Bean
    @ConditionalOnMissingBean(DocumentDefinitionHttpSecurityConfigurer.class)
    public DocumentDefinitionHttpSecurityConfigurer documentDefinitionHttpSecurityConfigurer() {
        return new DocumentDefinitionHttpSecurityConfigurer();
    }

    @Order(292)
    @Bean
    @ConditionalOnMissingBean(DocumentSearchHttpSecurityConfigurer.class)
    public DocumentSearchHttpSecurityConfigurer documentSearchHttpSecurityConfigurer() {
        return new DocumentSearchHttpSecurityConfigurer();
    }

    @Order(293)
    @Bean
    @ConditionalOnMissingBean(DocumentSnapshotHttpSecurityConfigurer.class)
    public DocumentSnapshotHttpSecurityConfigurer documentSnapshotHttpSecurityConfigurer() {
        return new DocumentSnapshotHttpSecurityConfigurer();
    }

    @Order(294)
    @Bean
    @ConditionalOnMissingBean(DocumentWebSocketHttpSecurityConfigurer.class)
    public DocumentWebSocketHttpSecurityConfigurer documentWebSocketHttpSecurityConfigurer() {
        return new DocumentWebSocketHttpSecurityConfigurer();
    }

    @Bean
    @ConditionalOnMissingBean(WebSocketSecurityConfigurer.class)
    public WebSocketSecurityConfigurer webSocketSecurityConfigurer() {
        return new WebSocketSecurityConfigurer();
    }

}
