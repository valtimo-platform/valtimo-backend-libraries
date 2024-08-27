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

package com.ritense.logging.testimpl

import com.ritense.logging.ResourceLoggerContext
import mu.KLogger
import mu.KotlinLogging

class LogResource() {

    val id = 123

    fun logSomething() {
        ResourceLoggerContext.withResource(this::class.java, "some-key") {
            outputMessage()
        }
    }

    fun logNothing() {
        ResourceLoggerContext.withResource(OtherInnerClass::class.java, "should-not-be-logged") {
            //we don't want to log anything here to validate the key is removed from the MDC
        }
    }

    fun logSomethingWithTwoLevelsOfMetaData() {
        ResourceLoggerContext.withResource(InnerClass::class.java, "second-key") {
            logSomething()
        }
    }

    fun logSomethingWithSiblingResource() {
        logNothing()
        logSomething()
    }

    fun outputMessage() {
        logger.info { "This is a message" }
    }

    class InnerClass
    class OtherInnerClass

    private companion object {
        val logger: KLogger = KotlinLogging.logger {}
    }
}