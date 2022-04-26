/*
 * Copyright 2015-2022 Ritense BV, the Netherlands.
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

package com.ritense.formflow.expression

import mu.KLogger
import mu.KotlinLogging
import org.springframework.context.ApplicationContext

class ExpressionProcessorFactoryHolder {

    companion object {
        private val logger: KLogger = KotlinLogging.logger {}
        private var expressionProcessorFactory: ExpressionProcessorFactory? = null

        fun setInstance(expressionProcessorFactory: ExpressionProcessorFactory, applicationContext: ApplicationContext) {
            expressionProcessorFactory.setFlowProcessBeans(
                applicationContext.getBeansWithAnnotation(FormFlowBean::class.java)
            )

            if(this.expressionProcessorFactory != null) {
                logger.warn { "ExpressionProcessorFactory instance was already set, this should not happen in runtime!" }
            }
            this.expressionProcessorFactory = expressionProcessorFactory
        }

        fun getinstance(): ExpressionProcessorFactory? {
            if(this.expressionProcessorFactory == null) {
                logger.warn { "ExpressionProcessorFactory instance is not available. Expressions will not be executed!" }
            }
            return expressionProcessorFactory
        }
    }
}