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

package com.ritense.formflow.handler

import com.ritense.formflow.expression.ExpressionProcessorFactoryHolder
import com.ritense.formflow.expression.spel.SpelExpressionProcessorFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationContext
import org.springframework.context.event.EventListener

class ApplicationReadyEventHandler(
    private val applicationContext:ApplicationContext
) {
    @EventListener(ApplicationReadyEvent::class)
    fun onApplicationReady() {
        val expressionProcessorFactory = SpelExpressionProcessorFactory()

        ExpressionProcessorFactoryHolder.setInstance(expressionProcessorFactory, applicationContext)
    }
}