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

package com.ritense.outbox.rabbitmq.config

import com.ritense.outbox.config.condition.ConditionalOnOutboxEnabled
import com.ritense.outbox.publisher.MessagePublisher
import com.ritense.outbox.rabbitmq.RabbitMessagePublisher
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean


@AutoConfiguration
@ConditionalOnOutboxEnabled
@EnableConfigurationProperties(RabbitOutboxConfigurationProperties::class)
class RabbitOutboxAutoconfiguration {

    @Bean
    @ConditionalOnMissingBean(MessagePublisher::class)
    fun outboxPublisher(rabbitTemplate: RabbitTemplate, configurationProperties: RabbitOutboxConfigurationProperties): MessagePublisher {
        return RabbitMessagePublisher(
            rabbitTemplate,
            configurationProperties.routingKey,
            configurationProperties.deliveryTimeout,
            configurationProperties.exchange
        )
    }
}