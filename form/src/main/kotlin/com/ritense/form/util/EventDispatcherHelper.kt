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

package com.ritense.form.util

import com.ritense.form.domain.FormSpringContextHelper
import com.ritense.outbox.ValtimoOutboxService
import com.ritense.outbox.config.condition.OnOutboxEnabledCondition.Companion.PROPERTY_NAME
import com.ritense.outbox.domain.BaseEvent
import com.ritense.valtimo.contract.domain.AggregateRoot
import mu.KotlinLogging

class EventDispatcherHelper {

    companion object {

        fun dispatchEvents(aggregateRoot: AggregateRoot<BaseEvent>) {
            if (assertOutboxEnabled()) {
                val outboxService = FormSpringContextHelper.getBean(ValtimoOutboxService::class.java)
                aggregateRoot.domainEvents()
                    .forEach { domainEvent ->
                        logger.trace { "Dispatch domain event $domainEvent" }
                        outboxService.send { domainEvent }
                    }
                aggregateRoot.clearDomainEvents()
            }
        }

        private fun assertOutboxEnabled(): Boolean {
            val isEnabled = FormSpringContextHelper.applicationContext.environment.getProperty(
                PROPERTY_NAME,
                "false"
            ).toBoolean()
            if (!isEnabled) {
                logger.warn { "Skipping dispatchEvents because outbox is disabled" }
            }
            return isEnabled
        }

        private val logger = KotlinLogging.logger {}
    }
}