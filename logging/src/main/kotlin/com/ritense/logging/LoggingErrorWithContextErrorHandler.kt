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

package com.ritense.logging

import com.ritense.valtimo.contract.annotation.AllOpen
import mu.KotlinLogging
import org.springframework.util.ErrorHandler

@AllOpen
class LoggingErrorWithContextErrorHandler : ErrorHandler {

    override fun handleError(t: Throwable) {
        withErrorLoggingContext {
            logger.error("Unexpected error occurred in scheduled task", t)
        }
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}